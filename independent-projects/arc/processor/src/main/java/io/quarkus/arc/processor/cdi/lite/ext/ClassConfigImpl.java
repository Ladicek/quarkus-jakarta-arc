package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.model.AnnotationAttribute;
import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.declarations.ClassInfo;
import cdi.lite.extension.phases.enhancement.ClassConfig;
import cdi.lite.extension.phases.enhancement.FieldConfig;
import cdi.lite.extension.phases.enhancement.MethodConfig;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

class ClassConfigImpl extends ClassInfoImpl implements ClassConfig<Object> {
    private final AllAnnotationTransformations allTransformations;

    private final AnnotationsTransformation.Classes transformations;

    ClassConfigImpl(org.jboss.jandex.IndexView jandexIndex, AllAnnotationTransformations allTransformations,
            org.jboss.jandex.ClassInfo jandexDeclaration) {
        super(jandexIndex, allTransformations.annotationOverlays, jandexDeclaration);
        this.allTransformations = allTransformations;
        this.transformations = allTransformations.classes;
    }

    @Override
    public Collection<? extends MethodConfig<Object>> constructors() {
        return jandexDeclaration.methods()
                .stream()
                .filter(MethodPredicates.IS_CONSTRUCTOR_JANDEX)
                .map(it -> new MethodConfigImpl(jandexIndex, allTransformations.methods, it))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends MethodConfig<Object>> methods() {
        return jandexDeclaration.methods()
                .stream()
                .filter(MethodPredicates.IS_METHOD_JANDEX)
                .map(it -> new MethodConfigImpl(jandexIndex, allTransformations.methods, it))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends FieldConfig<Object>> fields() {
        return jandexDeclaration.fields()
                .stream()
                .map(it -> new FieldConfigImpl(jandexIndex, allTransformations.fields, it))
                .collect(Collectors.toList());
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
