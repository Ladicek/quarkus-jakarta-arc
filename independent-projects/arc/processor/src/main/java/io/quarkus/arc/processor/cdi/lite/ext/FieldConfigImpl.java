package io.quarkus.arc.processor.cdi.lite.ext;

import java.lang.annotation.Annotation;
import java.util.function.Predicate;
import javax.enterprise.inject.build.compatible.spi.FieldConfig;
import javax.enterprise.lang.model.AnnotationInfo;

class FieldConfigImpl extends FieldInfoImpl implements FieldConfig<Object> {
    private final AnnotationsTransformation.Fields transformations;

    FieldConfigImpl(org.jboss.jandex.IndexView jandexIndex, AnnotationsTransformation.Fields transformations,
            org.jboss.jandex.FieldInfo jandexDeclaration) {
        super(jandexIndex, transformations.annotationOverlays, jandexDeclaration);
        this.transformations = transformations;
    }

    @Override
    public void addAnnotation(Class<? extends Annotation> annotationType) {
        transformations.addAnnotation(jandexDeclaration, annotationType);
    }

    @Override
    public void addAnnotation(AnnotationInfo annotation) {
        transformations.addAnnotation(jandexDeclaration, annotation);
    }

    @Override
    public void addAnnotation(Annotation annotation) {
        transformations.addAnnotation(jandexDeclaration, annotation);
    }

    @Override
    public void removeAnnotation(Predicate<AnnotationInfo> predicate) {
        // TODO remove cast once AnnotationInfo is no longer parameterized
        transformations.removeAnnotation(jandexDeclaration, (Predicate) predicate);
    }

    @Override
    public void removeAllAnnotations() {
        transformations.removeAllAnnotations(jandexDeclaration);
    }
}
