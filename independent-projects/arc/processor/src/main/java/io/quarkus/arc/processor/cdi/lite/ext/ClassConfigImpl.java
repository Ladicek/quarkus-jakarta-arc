package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.model.AnnotationAttribute;
import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.declarations.ClassInfo;
import cdi.lite.extension.phases.enhancement.ClassConfig;
import java.lang.annotation.Annotation;
import java.util.function.Predicate;

class ClassConfigImpl extends ClassInfoImpl implements ClassConfig<Object> {
    private final AnnotationsTransformation.Classes transformations;

    ClassConfigImpl(org.jboss.jandex.IndexView jandexIndex, AnnotationsTransformation.Classes transformations,
            org.jboss.jandex.ClassInfo jandexDeclaration) {
        super(jandexIndex, transformations.annotationOverlays, jandexDeclaration);
        this.transformations = transformations;
    }

    @Override
    public void addAnnotation(Class<? extends Annotation> annotationType, AnnotationAttribute... attributes) {
        transformations.addAnnotation(jandexDeclaration, annotationType, attributes);
    }

    @Override
    public void addAnnotation(ClassInfo<?> annotationType, AnnotationAttribute... attributes) {
        transformations.addAnnotation(jandexDeclaration, annotationType, attributes);
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
        transformations.removeAnnotation(jandexDeclaration, predicate);
    }

    @Override
    public void removeAllAnnotations() {
        transformations.removeAllAnnotations(jandexDeclaration);
    }
}
