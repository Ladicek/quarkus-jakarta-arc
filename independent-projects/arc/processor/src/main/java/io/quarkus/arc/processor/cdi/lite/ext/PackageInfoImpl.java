package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.declarations.PackageInfo;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PackageInfoImpl that = (PackageInfoImpl) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
