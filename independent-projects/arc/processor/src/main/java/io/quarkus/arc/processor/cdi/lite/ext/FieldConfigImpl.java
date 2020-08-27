package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.model.AnnotationAttribute;
import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.configs.FieldConfig;
import cdi.lite.extension.model.declarations.ClassInfo;
import java.lang.annotation.Annotation;
import java.util.function.Predicate;

class FieldConfigImpl extends FieldInfoImpl implements FieldConfig<Object> {
    private final AnnotationsTransformation.Fields transformations;

    FieldConfigImpl(org.jboss.jandex.IndexView jandexIndex, AnnotationsTransformation.Fields transformations,
            org.jboss.jandex.FieldInfo jandexDeclaration) {
        super(jandexIndex, transformations.annotationOverlays, jandexDeclaration);
        this.transformations = transformations;
    }

    @Override
    public void addAnnotation(Class<? extends Annotation> clazz, AnnotationAttribute... attributes) {
        transformations.addAnnotation(jandexDeclaration, clazz, attributes);
    }

    @Override
    public void addAnnotation(ClassInfo<?> clazz, AnnotationAttribute... attributes) {
        transformations.addAnnotation(jandexDeclaration, clazz, attributes);
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
