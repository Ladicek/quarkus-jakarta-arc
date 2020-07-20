package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.declarations.PackageInfo;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

class PackageInfoImpl implements PackageInfo {
    private final String name;

    PackageInfoImpl(String name) {
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    // TODO Jandex doesn't capture package annotations

    @Override
    public boolean hasAnnotation(Class<? extends Annotation> annotationType) {
        return false;
    }

    @Override
    public AnnotationInfo annotation(Class<? extends Annotation> annotationType) {
        return null;
    }

    @Override
    public List<AnnotationInfo> repeatableAnnotation(Class<? extends Annotation> annotationType) {
        return Collections.emptyList();
    }

    @Override
    public List<AnnotationInfo> annotations() {
        return Collections.emptyList();
    }
}
