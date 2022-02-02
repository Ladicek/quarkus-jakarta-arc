package io.quarkus.arc.arquillian;

import io.quarkus.arc.processor.AnnotationsTransformer;
import io.quarkus.arc.processor.BeanArchives;
import io.quarkus.arc.processor.BeanDefiningAnnotation;
import io.quarkus.arc.processor.BeanProcessor;
import io.quarkus.arc.processor.cdi.lite.ext.ExtensionsEntryPoint;
import jakarta.enterprise.inject.Stereotype;
import jakarta.inject.Qualifier;
import jakarta.interceptor.InterceptorBinding;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.CompositeIndex;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

final class Deployer {
    private final Archive<?> deploymentArchive;
    private final DeploymentDir deploymentDir;
    private final Class<?> testClass;
    private final Set<String> beanArchivePaths;

    Deployer(Archive<?> deploymentArchive, DeploymentDir deploymentDir, Class<?> testClass) {
        this.deploymentArchive = deploymentArchive;
        this.deploymentDir = deploymentDir;
        this.testClass = testClass;
        this.beanArchivePaths = new HashSet<>();
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
        } catch (IOException | ClassNotFoundException e) {
            throw new DeploymentException("Deployment failed", e);
        }
    }

    private void explodeWar() throws IOException {
        // firstly, pass through all beans.xml that we can find to build knowledge of valid bean archives
        for (Map.Entry<ArchivePath, Node> entry : deploymentArchive.getContent(Filters.include(".*/beans.xml")).entrySet()) {
            if (isBeansXmlValidMarker(entry.getValue().getAsset())) {
                // replace the path
                beanArchivePaths.add(entry.getKey().get().replace("/beans.xml", "/classes"));
            }
        }

        for (Map.Entry<ArchivePath, Node> entry : deploymentArchive.getContent().entrySet()) {
            Asset asset = entry.getValue().getAsset();
            if (asset == null) {
                continue;
            }
            String path = entry.getKey().get();
            String replacement = null;
            if (path.startsWith("/WEB-INF/classes/")) {
                replacement = path.replace("/WEB-INF/classes/", "");
                Path classFilePath = deploymentDir.appClasses.resolve(replacement);
                copy(asset, classFilePath);
            } else if (path.startsWith("/WEB-INF/lib/")) {
                replacement = path.replace("/WEB-INF/lib/", "");
                Path jarFilePath = deploymentDir.appLibraries.resolve(replacement);
                copy(asset, jarFilePath);
            }
            if (replacement != null && beanArchivePaths.stream().anyMatch(s -> path.startsWith(s))) {
                Path beanArchivePath = deploymentDir.beanArchive.resolve(replacement);
                copy(asset, beanArchivePath);
            }
        }
    }

    /**
     * Trivial beans.xml parsing that only checks if the file has <beans> element and what's the value of its discovery
     * mode. If no such element if found, the file is assumed to be empty.
     *
     * @param asset Arq. Asset which represents the beans.xml file; used to open InputStream
     * @return true if this beans.xml marks a bean archive whose classes should be registered as beans, false otherwise
     */
    private boolean isBeansXmlValidMarker(Asset asset) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            Document doc = dbf.newDocumentBuilder().parse(asset.openStream());
            doc.getDocumentElement().normalize();
            NodeList beansNode = doc.getElementsByTagName("beans");
            int tagsFound = beansNode.getLength();
            if (tagsFound > 1) {
                throw new IllegalStateException("Invalid beans.xml with more than one <beans> element detected - " + asset);
            }
            if (tagsFound == 0) {
                // we consider this beans.xml file to be empty hence legitimate for CDI Lite deployment
                return true;
            }
            // we know there is exactly one
            Element e = (Element) beansNode.item(0);
            String attribute = e.getAttribute("bean-discovery-mode");
            if (attribute.isEmpty() || attribute.equals("annotated")) {
                return true;
            } else if (attribute.equals("none")) {
                // this if fine, not a bean archive, no discovery
                return false;
            } else {
                // any other value is treated as an error in this case
                throw new IllegalStateException("Illegal value of bean discovery mode in beans.xml - " + attribute);
            }
        } catch (IOException | SAXException | ParserConfigurationException e) {
            // TODO proper exception handling
            e.printStackTrace();
        }
        return false;
    }

    private void copy(Asset asset, Path targetPath) throws IOException {
        Files.createDirectories(targetPath.getParent()); // make sure the directory exists
        try (InputStream in = asset.openStream()) {
            Files.copy(in, targetPath);
        }
    }

    private void generate() throws IOException, ClassNotFoundException {
        Indexer applicationIndexer = new Indexer();
        try (Stream<Path> appClasses = Files.walk(deploymentDir.appClasses)) {
            List<Path> classFiles = appClasses.filter(it -> it.toString().endsWith(".class")).collect(Collectors.toList());
            for (Path classFile : classFiles) {
                try (InputStream in = Files.newInputStream(classFile)) {
                    applicationIndexer.index(in);
                }
            }
        }
        IndexView applicationIndex = applicationIndexer.complete();

        // for archive without beans.xml, or with discovery none, we need a different bean archive index from app index
        Indexer beanArchiveIndexer = new Indexer();
        try (Stream<Path> appClasses = Files.walk(deploymentDir.beanArchive)) {
            List<Path> classFiles = appClasses.filter(it -> it.toString().endsWith(".class")).collect(Collectors.toList());
            for (Path classFile : classFiles) {
                try (InputStream in = Files.newInputStream(classFile)) {
                    beanArchiveIndexer.index(in);
                }
            }
        }

        // tests without beans.xml will need to index test class manually as well as add all annotations
        if (beanArchivePaths.isEmpty()) {
            beanArchiveIndexer.indexClass(testClass);

            Set<Class<? extends Annotation>> annotationClasses = Set.of(Qualifier.class, InterceptorBinding.class,
                    Stereotype.class);
            for (Class<? extends Annotation> annotationClass : annotationClasses) {
                DotName dotName = DotName.createSimple(annotationClass.getName());
                for (AnnotationInstance annInstance : applicationIndex.getAnnotations(dotName)) {
                    if (annInstance.target().kind().equals(AnnotationTarget.Kind.CLASS)) {
                        beanArchiveIndexer.indexClass(Class.forName(annInstance.target().asClass().name().toString()));
                    }
                }
            }
        }

        IndexView beanArchiveIndex = beanArchiveIndexer.complete();

        try (Closeable ignored = withProcessingClassLoader()) {
            ExtensionsEntryPoint cdiLiteExtensions = new ExtensionsEntryPoint();
            Set<String> additionalClasses = new HashSet<>();
            cdiLiteExtensions.runDiscovery(CompositeIndex.create(beanArchiveIndex, applicationIndex), additionalClasses);
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
                    .setAdditionalBeanDefiningAnnotations(Set.of(new BeanDefiningAnnotation(
                            DotName.createSimple(ExtraBean.class.getName()), null)))
                    .addAnnotationTransformer(new AnnotationsTransformer() {
                        @Override
                        public boolean appliesTo(AnnotationTarget.Kind kind) {
                            return kind == AnnotationTarget.Kind.CLASS;
                        }

                        @Override
                        public void transform(TransformationContext ctx) {
                            if (ctx.getTarget().asClass().name().toString().equals(testClass.getName())) {
                                // make the test class a bean
                                ctx.transform().add(ExtraBean.class).done();
                            }
                            if (additionalClasses.contains(ctx.getTarget().asClass().name().toString())) {
                                // make all the `@Discovery`-registered classes beans
                                ctx.transform().add(ExtraBean.class).done();
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
