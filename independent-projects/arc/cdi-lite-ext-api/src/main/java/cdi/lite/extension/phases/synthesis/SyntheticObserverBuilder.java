package cdi.lite.extension.phases.synthesis;

import cdi.lite.extension.model.declarations.ClassInfo;
import cdi.lite.extension.model.types.Type;
import java.lang.annotation.Annotation;
import javax.enterprise.event.TransactionPhase;

/**
 * Instances are not reusable. For each synthetic bean, new instance must be created by {@link SyntheticComponents#addObserver()}.
 */
public interface SyntheticObserverBuilder {
    // if called multiple times, last call wins
    SyntheticObserverBuilder id(String identifier);

    // if called multiple times, last call wins
    SyntheticObserverBuilder declaringClass(Class<?> beanClass);
    SyntheticObserverBuilder declaringClass(ClassInfo<?> beanClass);

    // if called multiple times, last call wins
    SyntheticObserverBuilder observedType(Class<?> observedType);
    SyntheticObserverBuilder observedType(ClassInfo<?> observedType);
    SyntheticObserverBuilder observedType(Type observedType);

    // can be called multiple times and is additive
    SyntheticObserverBuilder qualifier(Class<? extends Annotation> qualifierAnnotation);
    // TODO other variants of qualifier, to deal with annotation attributes
    // TODO methods to add multiple qualifiers at once

    // if called multiple times, last call wins
    SyntheticObserverBuilder priority(int priority);

    // if called multiple times, last call wins
    SyntheticObserverBuilder async(boolean isAsync);

    // if called multiple times, last call wins
    SyntheticObserverBuilder transactionPhase(TransactionPhase transactionPhase);

    // TODO how to define the observer implementation?
}
