package io.quarkus.arc.processor.cdi.lite.ext;

import java.util.Set;
import javax.enterprise.inject.build.compatible.spi.ScannedClasses;

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
