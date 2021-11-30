package io.quarkus.arc.arquillian;

import io.quarkus.arc.processor.AnnotationsTransformer;
import io.quarkus.arc.processor.BeanArchives;
import io.quarkus.arc.processor.BeanProcessor;
import io.quarkus.arc.processor.cdi.lite.ext.ExtensionsEntryPoint;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.enterprise.context.Dependent;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.CompositeIndex;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

final class Deployer {
    private final Archive<?> deploymentArchive;
    private final DeploymentDir deploymentDir;
    private final Class<?> testClass;

    Deployer(Archive<?> deploymentArchive, DeploymentDir deploymentDir, Class<?> testClass) {
        this.deploymentArchive = deploymentArchive;
        this.deploymentDir = deploymentDir;
        this.testClass = testClass;
    }

    DeploymentClassLoader deploy() throws DeploymentException {
        try {
            if (deploymentArchive instanceof WebArchive) {
                explodeWar();
            } else {
                throw new DeploymentException("Unknown archive type: " + deploymentArchive);
            }

            generate();

            return new DeploymentClassLoader(deploymentDir);
        } catch (IOException e) {
            throw new DeploymentException("Deployment failed", e);
        }
    }

    private void explodeWar() throws IOException {
        for (Map.Entry<ArchivePath, Node> entry : deploymentArchive.getContent().entrySet()) {
            Asset asset = entry.getValue().getAsset();
            if (asset == null) {
                continue;
            }

            String path = entry.getKey().get();
            if (path.startsWith("/WEB-INF/classes/")) {
                String classFile = path.replace("/WEB-INF/classes/", "");
                Path classFilePath = deploymentDir.appClasses.resolve(classFile);
                copy(asset, classFilePath);
            } else if (path.startsWith("/WEB-INF/lib/")) {
                String jarFile = path.replace("/WEB-INF/lib/", "");
                Path jarFilePath = deploymentDir.appLibraries.resolve(jarFile);
                copy(asset, jarFilePath);
            }
        }
    }

    private void copy(Asset asset, Path targetPath) throws IOException {
        Files.createDirectories(targetPath.getParent()); // make sure the directory exists
        try (InputStream in = asset.openStream()) {
            Files.copy(in, targetPath);
        }
    }

    private void generate() throws IOException {
        Indexer indexer = new Indexer();
        try (Stream<Path> appClasses = Files.walk(deploymentDir.appClasses)) {
            List<Path> classFiles = appClasses.filter(it -> it.toString().endsWith(".class")).collect(Collectors.toList());
            for (Path classFile : classFiles) {
                try (InputStream in = Files.newInputStream(classFile)) {
                    indexer.index(in);
                }
            }
        }
        IndexView beanArchiveIndex = indexer.complete();

        try (Closeable ignored = withProcessingClassLoader()) {
            ExtensionsEntryPoint cdiLiteExtensions = new ExtensionsEntryPoint();
            Set<String> additionalClasses = new HashSet<>();
            cdiLiteExtensions.runDiscovery(CompositeIndex.create(beanArchiveIndex/* , applicationIndex */), additionalClasses);
            boolean additionalClassesExist = false;
            Indexer additionalIndexer = new Indexer();
            for (String additionalClass : additionalClasses) {
                if (beanArchiveIndex.getClassByName(DotName.createSimple(additionalClass)) != null) {
                    continue;
                }
                try (InputStream in = Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream(additionalClass.replace('.', '/') + ".class")) {
                    additionalIndexer.index(in);
                    additionalClassesExist = true;
                }
            }
            if (additionalClassesExist) {
                beanArchiveIndex = CompositeIndex.create(beanArchiveIndex, additionalIndexer.complete());
            }

            BeanProcessor beanProcessor = BeanProcessor.builder()
                    .setName(deploymentDir.root.getFileName().toString())
                    .setBeanArchiveIndex(BeanArchives.buildBeanArchiveIndex(Thread.currentThread().getContextClassLoader(),
                            new ConcurrentHashMap<>(), beanArchiveIndex))
                    .setCdiLiteExtensions(cdiLiteExtensions)
                    .addAnnotationTransformer(new AnnotationsTransformer() {
                        // make the test class a bean

                        @Override
                        public boolean appliesTo(AnnotationTarget.Kind kind) {
                            return kind == AnnotationTarget.Kind.CLASS;
                        }

                        @Override
                        public void transform(TransformationContext ctx) {
                            if (ctx.getTarget().asClass().name().toString().equals(testClass.getName())) {
                                ctx.transform().add(Dependent.class).done();
                            }
                        }
                    })
                    .setOutput(resource -> {
                        switch (resource.getType()) {
                            case JAVA_CLASS:
                                resource.writeTo(deploymentDir.generatedClasses.toFile());
                                break;
                            case SERVICE_PROVIDER:
                                resource.writeTo(deploymentDir.generatedServiceProviders.toFile());
                                break;
                            default:
                                throw new IllegalArgumentException("Unknown resource type " + resource.getType());
                        }
                    })
                    .build();
            beanProcessor.process();
        }
    }

    private Closeable withProcessingClassLoader() throws IOException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(new ProcessingClassLoader(deploymentDir));
        return new Closeable() {
            @Override
            public void close() {
                Thread.currentThread().setContextClassLoader(old);
            }
        };
    }
}
