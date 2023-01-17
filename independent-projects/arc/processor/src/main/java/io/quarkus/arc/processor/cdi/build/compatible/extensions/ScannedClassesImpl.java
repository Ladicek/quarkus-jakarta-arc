package io.quarkus.arc.processor.cdi.build.compatible.extensions;

import java.util.Set;

import jakarta.enterprise.inject.build.compatible.spi.ScannedClasses;

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
