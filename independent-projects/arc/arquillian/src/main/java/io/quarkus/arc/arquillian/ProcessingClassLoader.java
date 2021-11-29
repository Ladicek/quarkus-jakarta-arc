package io.quarkus.arc.arquillian;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class ProcessingClassLoader extends URLClassLoader {
    static {
        ClassLoader.registerAsParallelCapable();
    }

    private final DeploymentDir deploymentDir;

    ProcessingClassLoader(DeploymentDir deploymentDir) throws IOException {
        super(findUrls(deploymentDir));
        this.deploymentDir = deploymentDir;
    }

    private static URL[] findUrls(DeploymentDir deploymentDir) throws IOException {
        List<URL> result = new ArrayList<>();

        result.add(deploymentDir.appClasses.toUri().toURL());

        try (Stream<Path> stream = Files.walk(deploymentDir.appLibraries)) {
            List<Path> jars = stream.filter(p -> p.toString().endsWith(".jar")).collect(Collectors.toList());
            for (Path jar : jars) {
                result.add(jar.toUri().toURL());
            }
        }

        return result.toArray(new URL[0]);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> clazz = findLoadedClass(name);
            if (clazz != null) {
                return clazz;
            }

            try {
                clazz = findClass(name);
                if (resolve) {
                    resolveClass(clazz);
                }
                return clazz;
            } catch (ClassNotFoundException ignored) {
                return super.loadClass(name, resolve);
            }
        }
    }
}
