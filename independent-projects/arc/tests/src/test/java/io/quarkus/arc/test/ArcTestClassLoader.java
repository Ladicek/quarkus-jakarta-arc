package io.quarkus.arc.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

import io.quarkus.arc.ComponentsProvider;
import io.quarkus.arc.ResourceReferenceProvider;

class ArcTestClassLoader extends ClassLoader {
    static {
        ClassLoader.registerAsParallelCapable();
    }

    private final Map<String, byte[]> transformedClasses;
    private final File componentsProviderFile;
    private final File resourceReferenceProviderFile;

    public ArcTestClassLoader(ClassLoader parent, Map<String, byte[]> transformedClasses,
            File componentsProviderFile, File resourceReferenceProviderFile) {
        super(parent);

        this.transformedClasses = transformedClasses;
        this.componentsProviderFile = componentsProviderFile;
        this.resourceReferenceProviderFile = resourceReferenceProviderFile;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> clazz = findLoadedClass(name);
            if (clazz != null) {
                return clazz;
            }

            byte[] bytecode = null;
            if (transformedClasses != null) {
                bytecode = transformedClasses.get(name);
            }
            if (bytecode == null && name.startsWith("io.quarkus.arc")) { // ArC and all tests are in io.quarkus.arc
                String path = name.replace('.', '/') + ".class";
                try (InputStream in = getParent().getResourceAsStream(path)) {
                    if (in != null) {
                        bytecode = in.readAllBytes();
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
            if (bytecode != null) {
                clazz = defineClass(name, bytecode, 0, bytecode.length);
                if (resolve) {
                    resolveClass(clazz);
                }
                return clazz;
            }

            return super.loadClass(name, resolve);
        }
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        if (componentsProviderFile != null
                && ("META-INF/services/" + ComponentsProvider.class.getName()).equals(name)) {
            return Collections.enumeration(Collections.singleton(componentsProviderFile.toURI().toURL()));
        } else if (resourceReferenceProviderFile != null
                && ("META-INF/services/" + ResourceReferenceProvider.class.getName()).equals(name)) {
            return Collections.enumeration(Collections.singleton(resourceReferenceProviderFile.toURI().toURL()));
        }
        return super.getResources(name);
    }

    public static ArcTestClassLoader inTCCL() {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        if (tccl instanceof ArcTestClassLoader) {
            return (ArcTestClassLoader) tccl;
        }
        throw new IllegalStateException(
                "TCCL is not ArcTestClassLoader, the `@RegisterExtension` field of type `ArcTestContainer` must be `static`");
    }
}
