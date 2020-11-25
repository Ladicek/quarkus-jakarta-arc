package cdi.lite.extension.phases.synthesis;

import cdi.lite.extension.model.AnnotationAttribute;
import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.declarations.ClassInfo;
import cdi.lite.extension.model.types.Type;
import java.lang.annotation.Annotation;

/**
 * Instances are not reusable. For each synthetic bean, new instance
 * must be created by {@link SyntheticComponents#addBean(Class)}.
 *
 * @param <T> type of bean instances
 */
public interface SyntheticBeanBuilder<T> {
    // can be called multiple times and is additive
    SyntheticBeanBuilder<T> type(Class<?> type);

    SyntheticBeanBuilder<T> type(ClassInfo<?> type);

    SyntheticBeanBuilder<T> type(Type type);

    // can be called multiple times and is additive
    SyntheticBeanBuilder<T> qualifier(Class<? extends Annotation> qualifierAnnotation, AnnotationAttribute... attributes);

    SyntheticBeanBuilder<T> qualifier(ClassInfo<?> qualifierAnnotation, AnnotationAttribute... attributes);

    SyntheticBeanBuilder<T> qualifier(AnnotationInfo qualifierAnnotation);

    SyntheticBeanBuilder<T> qualifier(Annotation qualifierAnnotation);
    // TODO methods to add multiple qualifiers at once?

    // if called multiple times, last call wins
    SyntheticBeanBuilder<T> scope(Class<? extends Annotation> scopeAnnotation);

    // if called with `true`, priority is automatically 0, unless `priority` is also called
    // if called multiple times, last call wins
    SyntheticBeanBuilder<T> alternative(boolean isAlternative);

    // if called, alternative is automatically true
    // if called multiple times, last call wins
    SyntheticBeanBuilder<T> priority(int priority);

    // EL name (equivalent to @Named), IIUC
    // if called multiple times, last call wins
    SyntheticBeanBuilder<T> name(String name);

    // can be called multiple times and is additive
    SyntheticBeanBuilder<T> stereotype(Class<? extends Annotation> stereotypeAnnotation);

    SyntheticBeanBuilder<T> stereotype(ClassInfo<?> stereotypeAnnotation);

    // TODO how to define the creation/destruction implementation?

    SyntheticBeanBuilder<T> withParam(String key, boolean value);

    SyntheticBeanBuilder<T> withParam(String key, int value);

    SyntheticBeanBuilder<T> withParam(String key, long value);

    SyntheticBeanBuilder<T> withParam(String key, double value);

    SyntheticBeanBuilder<T> withParam(String key, String value);

    SyntheticBeanBuilder<T> withParam(String key, Class<?> value);

    SyntheticBeanBuilder<T> createWith(Class<? extends SyntheticBeanCreator<T>> creatorClass);

    SyntheticBeanBuilder<T> disposeWith(Class<? extends SyntheticBeanDisposer<T>> disposerClass);
}
