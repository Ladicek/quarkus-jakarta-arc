package cdi.lite.extension.phases.synthesis;

import cdi.lite.extension.model.AnnotationAttribute;
import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.declarations.ClassInfo;
import cdi.lite.extension.model.types.Type;
import java.lang.annotation.Annotation;
import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;

/**
 * Instances are not reusable. For each synthetic observer, new instance
 * must be created by {@link SyntheticComponents#addObserver()}.
 */
public interface SyntheticObserverBuilder {
    /**
     * Class that, hypothetically, "declares" the synthetic observer.
     * If not called, the class declaring the extension which creates this synthetic observer is used.
     * <p>
     * Used to identify an observer when multiple synthetic observers are otherwise identical.
     * TODO can this have implementation consequences? e.g., must the class be added to the bean archive?
     */
    // if called multiple times, last call wins
    SyntheticObserverBuilder declaringClass(Class<?> declaringClass);

    SyntheticObserverBuilder declaringClass(ClassInfo<?> declaringClass);

    // if called multiple times, last call wins
    SyntheticObserverBuilder type(Class<?> type);

    SyntheticObserverBuilder type(ClassInfo<?> type);

    SyntheticObserverBuilder type(Type type);

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
    // TODO this probably doesn't make sense for synthetic observers? but Portable Extensions have it?
    SyntheticObserverBuilder reception(Reception reception);

    // if called multiple times, last call wins
    SyntheticObserverBuilder transactionPhase(TransactionPhase transactionPhase);

    // TODO params? ArC doesn't have them for observers, only for beans, do they make sense here?

    SyntheticObserverBuilder observeWith(Class<? extends SyntheticObserver<?>> syntheticObserverClass);
}
