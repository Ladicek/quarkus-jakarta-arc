package io.quarkus.arc.processor.cdi.lite.ext;

import jakarta.enterprise.inject.build.compatible.spi.ScannedClasses;
import java.util.Set;

class ScannedClassesImpl implements ScannedClasses {
    private final Set<String> classes;

    ScannedClassesImpl(Set<String> classes) {
        this.classes = classes;
    }

    @Override
    public void add(String className) {
        classes.add(className);
    }
}
