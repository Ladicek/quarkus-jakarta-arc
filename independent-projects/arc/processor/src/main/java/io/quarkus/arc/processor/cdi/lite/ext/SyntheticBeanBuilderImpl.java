package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.model.AnnotationAttribute;
import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.declarations.ClassInfo;
import cdi.lite.extension.model.types.Type;
import cdi.lite.extension.phases.synthesis.SyntheticBeanBuilder;
import cdi.lite.extension.phases.synthesis.SyntheticBeanCreator;
import cdi.lite.extension.phases.synthesis.SyntheticBeanDisposer;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jboss.jandex.DotName;

class SyntheticBeanBuilderImpl<T> implements SyntheticBeanBuilder<T> {
    DotName implementationClass;
    Set<org.jboss.jandex.Type> types = new HashSet<>();
    Set<org.jboss.jandex.AnnotationInstance> qualifiers = new HashSet<>();
    Class<? extends Annotation> scope;
    boolean isAlternative;
    int priority;
    String name;
    Set<DotName> stereotypes = new HashSet<>();
    Map<String, Object> params = new HashMap<>();
    Class<? extends SyntheticBeanCreator<T>> creatorClass;
    Class<? extends SyntheticBeanDisposer<T>> disposerClass;

    SyntheticBeanBuilderImpl(Class<?> implementationClass) {
        this.implementationClass = DotName.createSimple(implementationClass.getName());
    }

    @Override
    public SyntheticBeanBuilder<T> type(Class<?> type) {
        this.types.add(TypesReflection.jandexType(type));
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> type(ClassInfo<?> type) {
        DotName className = ((ClassInfoImpl) type).jandexDeclaration.name();
        // TODO does this cover all possible cases?
        org.jboss.jandex.Type jandexType = org.jboss.jandex.Type.create(className, org.jboss.jandex.Type.Kind.CLASS);
        this.types.add(jandexType);
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> type(Type type) {
        this.types.add(((TypeImpl<?>) type).jandexType);
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> qualifier(Class<? extends Annotation> qualifierAnnotation,
            AnnotationAttribute... attributes) {
        DotName annotationName = DotName.createSimple(qualifierAnnotation.getName());
        org.jboss.jandex.AnnotationValue[] annotationAttributes = Arrays.stream(attributes)
                .map(it -> ((AnnotationAttributeImpl) it).jandexAnnotationAttribute)
                .toArray(org.jboss.jandex.AnnotationValue[]::new);
        this.qualifiers.add(org.jboss.jandex.AnnotationInstance.create(annotationName, null, annotationAttributes));
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> qualifier(ClassInfo<?> qualifierAnnotation, AnnotationAttribute... attributes) {
        DotName annotationName = ((ClassInfoImpl) qualifierAnnotation).jandexDeclaration.name();
        org.jboss.jandex.AnnotationValue[] annotationAttributes = Arrays.stream(attributes)
                .map(it -> ((AnnotationAttributeImpl) it).jandexAnnotationAttribute)
                .toArray(org.jboss.jandex.AnnotationValue[]::new);
        this.qualifiers.add(org.jboss.jandex.AnnotationInstance.create(annotationName, null, annotationAttributes));
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> qualifier(AnnotationInfo qualifierAnnotation) {
        this.qualifiers.add(((AnnotationInfoImpl) qualifierAnnotation).jandexAnnotation);
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> qualifier(Annotation qualifierAnnotation) {
        this.qualifiers.add(AnnotationsReflection.jandexAnnotation(qualifierAnnotation));
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> scope(Class<? extends Annotation> scopeAnnotation) {
        this.scope = scopeAnnotation;
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> alternative(boolean isAlternative) {
        this.isAlternative = isAlternative;
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> priority(int priority) {
        this.priority = priority;
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> stereotype(Class<? extends Annotation> stereotypeAnnotation) {
        this.stereotypes.add(DotName.createSimple(stereotypeAnnotation.getName()));
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> stereotype(ClassInfo<?> stereotypeAnnotation) {
        this.stereotypes.add(((ClassInfoImpl) stereotypeAnnotation).jandexDeclaration.name());
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> withParam(String key, boolean value) {
        this.params.put(key, value);
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> withParam(String key, int value) {
        this.params.put(key, value);
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> withParam(String key, long value) {
        this.params.put(key, value);
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> withParam(String key, double value) {
        this.params.put(key, value);
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> withParam(String key, String value) {
        this.params.put(key, value);
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> withParam(String key, Class<?> value) {
        this.params.put(key, value);
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> createWith(Class<? extends SyntheticBeanCreator<T>> creatorClass) {
        this.creatorClass = creatorClass;
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> disposeWith(Class<? extends SyntheticBeanDisposer<T>> disposerClass) {
        this.disposerClass = disposerClass;
        return this;
    }
}
