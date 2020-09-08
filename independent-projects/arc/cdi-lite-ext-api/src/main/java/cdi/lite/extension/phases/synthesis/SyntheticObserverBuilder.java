package cdi.lite.extension.phases.synthesis;

import cdi.lite.extension.model.AnnotationAttribute;
import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.declarations.ClassInfo;
import cdi.lite.extension.model.types.Type;
import java.lang.annotation.Annotation;
import javax.enterprise.event.TransactionPhase;

/**
 * Instances are not reusable. For each synthetic observer, new instance must be created by
 * {@link SyntheticComponents#addObserver()}.
 */
public interface SyntheticObserverBuilder {
    /**
     * Used to identify an observer when multiple synthetic observers are otherwise identical.
     * TODO can we remove this?
     */
    // if called multiple times, last call wins
    SyntheticObserverBuilder id(String identifier);

    /**
     * Used to identify an observer when multiple synthetic observers are otherwise identical.
     * <p>
     * If not called, the class declaring the extension method which creates this synthetic observer is used.
     * TODO this can have implementation consequences? e.g., must the class be added to the bean archive?
     */
    // if called multiple times, last call wins
    SyntheticObserverBuilder declaringClass(Class<?> beanClass);

    SyntheticObserverBuilder declaringClass(ClassInfo<?> beanClass);

    // if called multiple times, last call wins
    SyntheticObserverBuilder observedType(Class<?> observedType);

    SyntheticObserverBuilder observedType(ClassInfo<?> observedType);

    SyntheticObserverBuilder observedType(Type observedType);

    // can be called multiple times and is additive
    SyntheticObserverBuilder qualifier(Class<? extends Annotation> qualifierAnnotation, AnnotationAttribute... attributes);

    SyntheticObserverBuilder qualifier(ClassInfo<?> qualifierAnnotation, AnnotationAttribute... attributes);

    SyntheticObserverBuilder qualifier(AnnotationInfo qualifierAnnotation);

    SyntheticObserverBuilder qualifier(Annotation qualifierAnnotation);
    // TODO methods to add multiple qualifiers at once

    // if called multiple times, last call wins
    SyntheticObserverBuilder priority(int priority);

    // if called multiple times, last call wins
    SyntheticObserverBuilder async(boolean isAsync);

    // if called multiple times, last call wins
    SyntheticObserverBuilder transactionPhase(TransactionPhase transactionPhase);

    // TODO how to define the observer implementation?
}
