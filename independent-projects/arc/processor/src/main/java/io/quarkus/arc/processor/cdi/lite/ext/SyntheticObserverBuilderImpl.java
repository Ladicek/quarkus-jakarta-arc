package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.model.AnnotationAttribute;
import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.declarations.ClassInfo;
import cdi.lite.extension.model.types.Type;
import cdi.lite.extension.phases.synthesis.SyntheticObserver;
import cdi.lite.extension.phases.synthesis.SyntheticObserverBuilder;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.spi.ObserverMethod;
import org.jboss.jandex.DotName;

class SyntheticObserverBuilderImpl implements SyntheticObserverBuilder {
    DotName declaringClass;
    org.jboss.jandex.Type type;
    Set<org.jboss.jandex.AnnotationInstance> qualifiers = new HashSet<>();
    int priority = ObserverMethod.DEFAULT_PRIORITY;
    boolean isAsync;
    Reception reception = Reception.ALWAYS;
    TransactionPhase transactionPhase = TransactionPhase.IN_PROGRESS;
    Class<? extends SyntheticObserver<?>> implementationClass;

    SyntheticObserverBuilderImpl(DotName extensionClass) {
        this.declaringClass = extensionClass;
    }

    @Override
    public SyntheticObserverBuilder declaringClass(Class<?> declaringClass) {
        this.declaringClass = DotName.createSimple(declaringClass.getName());
        return this;
    }

    @Override
    public SyntheticObserverBuilder declaringClass(ClassInfo<?> declaringClass) {
        this.declaringClass = ((ClassInfoImpl) declaringClass).jandexDeclaration.name();
        return this;
    }

    @Override
    public SyntheticObserverBuilder type(Class<?> type) {
        this.type = TypesReflection.jandexType(type);
        return this;
    }

    @Override
    public SyntheticObserverBuilder type(ClassInfo<?> type) {
        DotName className = ((ClassInfoImpl) type).jandexDeclaration.name();
        // TODO does this cover all possible cases?
        this.type = org.jboss.jandex.Type.create(className, org.jboss.jandex.Type.Kind.CLASS);
        return this;
    }

    @Override
    public SyntheticObserverBuilder type(Type type) {
        this.type = ((TypeImpl<?>) type).jandexType;
        return this;
    }

    @Override
    public SyntheticObserverBuilder qualifier(Class<? extends Annotation> qualifierAnnotation,
            AnnotationAttribute... attributes) {
        DotName annotationName = DotName.createSimple(qualifierAnnotation.getName());
        org.jboss.jandex.AnnotationValue[] annotationAttributes = Arrays.stream(attributes)
                .map(it -> ((AnnotationAttributeImpl) it).jandexAnnotationAttribute)
                .toArray(org.jboss.jandex.AnnotationValue[]::new);
        this.qualifiers.add(org.jboss.jandex.AnnotationInstance.create(annotationName, null, annotationAttributes));
        return this;
    }

    @Override
    public SyntheticObserverBuilder qualifier(ClassInfo<?> qualifierAnnotation, AnnotationAttribute... attributes) {
        DotName annotationName = ((ClassInfoImpl) qualifierAnnotation).jandexDeclaration.name();
        org.jboss.jandex.AnnotationValue[] annotationAttributes = Arrays.stream(attributes)
                .map(it -> ((AnnotationAttributeImpl) it).jandexAnnotationAttribute)
                .toArray(org.jboss.jandex.AnnotationValue[]::new);
        this.qualifiers.add(org.jboss.jandex.AnnotationInstance.create(annotationName, null, annotationAttributes));
        return this;
    }

    @Override
    public SyntheticObserverBuilder qualifier(AnnotationInfo qualifierAnnotation) {
        this.qualifiers.add(((AnnotationInfoImpl) qualifierAnnotation).jandexAnnotation);
        return this;
    }

    @Override
    public SyntheticObserverBuilder qualifier(Annotation qualifierAnnotation) {
        this.qualifiers.add(AnnotationsReflection.jandexAnnotation(qualifierAnnotation));
        return this;
    }

    @Override
    public SyntheticObserverBuilder priority(int priority) {
        this.priority = priority;
        return this;
    }

    @Override
    public SyntheticObserverBuilder async(boolean isAsync) {
        this.isAsync = isAsync;
        return this;
    }

    @Override
    public SyntheticObserverBuilder reception(Reception reception) {
        this.reception = reception;
        return this;
    }

    @Override
    public SyntheticObserverBuilder transactionPhase(TransactionPhase transactionPhase) {
        this.transactionPhase = transactionPhase;
        return this;
    }

    @Override
    public SyntheticObserverBuilder observeWith(Class<? extends SyntheticObserver<?>> syntheticObserverClass) {
        this.implementationClass = syntheticObserverClass;
        return this;
    }
}
