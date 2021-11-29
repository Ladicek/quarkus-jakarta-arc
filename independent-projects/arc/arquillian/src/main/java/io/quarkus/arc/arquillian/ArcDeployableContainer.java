package io.quarkus.arc.arquillian;

import io.quarkus.arc.Arc;
import io.quarkus.arc.ArcContainer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.context.annotation.DeploymentScoped;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

public class ArcDeployableContainer implements DeployableContainer<ArcContainerConfiguration> {
    @Inject
    @DeploymentScoped
    private InstanceProducer<DeploymentDir> deploymentDir;

    @Inject
    @DeploymentScoped
    private InstanceProducer<ClassLoader> deploymentClassLoader;

    @Inject
    @DeploymentScoped
    private InstanceProducer<ArcContainer> runningArc;

    @Inject
    private Instance<TestClass> testClass;

    static Object testInstance;
    static ClassLoader old;

    @Override
    public Class<ArcContainerConfiguration> getConfigurationClass() {
        return ArcContainerConfiguration.class;
    }

    @Override
    public void setup(ArcContainerConfiguration configuration) {
    }

    @Override
    public ProtocolDescription getDefaultProtocol() {
        return new ProtocolDescription("ArC");
    }

    @Override
    public ProtocolMetaData deploy(Archive<?> archive) throws DeploymentException {
        old = Thread.currentThread().getContextClassLoader();
        if (testClass.get() == null) {
            throw new IllegalStateException("Test class not available");
        }
        Class<?> testJavaClass = testClass.get().getJavaClass();

        try {
            DeploymentDir deploymentDir = new DeploymentDir();
            this.deploymentDir.set(deploymentDir);

            DeploymentClassLoader deploymentClassLoader = new Deployer(archive, deploymentDir, testJavaClass).deploy();
            this.deploymentClassLoader.set(deploymentClassLoader);

            Thread.currentThread().setContextClassLoader(deploymentClassLoader);

            ArcContainer arcContainer = Arc.initialize();
            runningArc.set(arcContainer);

            Class<?> actualTestClass = Class.forName(testJavaClass.getName(), true, deploymentClassLoader);
            testInstance = arcContainer.select(actualTestClass).get();
        } catch (Throwable t) {
            // clone the exception into the correct class loader
            Throwable nt;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (ObjectOutputStream a = new ObjectOutputStream(out)) {
                a.writeObject(t);
                a.close();
                nt = (Throwable) new ObjectInputStream(new ByteArrayInputStream(out.toByteArray())).readObject();
            } catch (Exception e) {
                throw new DeploymentException("Unable to start ArC", t);
            }
            throw new DeploymentException("Unable to start ArC", nt);

        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }

        return new ProtocolMetaData();
    }

    @Override
    public void undeploy(Archive<?> archive) {
        try {
            ArcContainer arcContainer = runningArc.get();
            if (arcContainer != null) {
                Thread.currentThread().setContextClassLoader(deploymentClassLoader.get());
                Arc.shutdown();
            }
            testInstance = null;

            DeploymentDir deploymentDir = this.deploymentDir.get();
            if (deploymentDir != null) {
                deleteDirectory(deploymentDir.root);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    private static void deleteDirectory(Path dir) {
        try {
            Files.walkFileTree(dir, new FileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void deploy(Descriptor descriptor) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void undeploy(Descriptor descriptor) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }
}
