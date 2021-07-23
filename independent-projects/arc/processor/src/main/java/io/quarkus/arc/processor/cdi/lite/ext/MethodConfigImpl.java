package io.quarkus.arc.processor.cdi.lite.ext;

import java.lang.annotation.Annotation;
import java.util.function.Predicate;
import javax.enterprise.inject.build.compatible.spi.MethodConfig;
import javax.enterprise.lang.model.AnnotationAttribute;
import javax.enterprise.lang.model.AnnotationInfo;
import javax.enterprise.lang.model.declarations.ClassInfo;

class MethodConfigImpl extends MethodInfoImpl implements MethodConfig<Object> {
    private final AnnotationsTransformation.Methods transformations;

    MethodConfigImpl(org.jboss.jandex.IndexView jandexIndex, AnnotationsTransformation.Methods transformations,
            org.jboss.jandex.MethodInfo jandexDeclaration) {
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
