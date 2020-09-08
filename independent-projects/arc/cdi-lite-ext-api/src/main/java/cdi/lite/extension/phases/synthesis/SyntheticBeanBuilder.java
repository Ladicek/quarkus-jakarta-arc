package cdi.lite.extension.phases.synthesis;

import cdi.lite.extension.model.AnnotationAttribute;
import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.declarations.ClassInfo;
import java.lang.annotation.Annotation;

/**
 * Instances are not reusable. For each synthetic bean, new instance must be created by {@link SyntheticComponents#addBean()}.
 */
public interface SyntheticBeanBuilder {
    // can be called multiple times and is additive
    SyntheticBeanBuilder type(Class<?> clazz);

    SyntheticBeanBuilder type(ClassInfo<?> clazz);

    SyntheticBeanBuilder typeWithTransitiveClosure(Class<?> clazz);

    SyntheticBeanBuilder typeWithTransitiveClosure(ClassInfo<?> clazz);
    // TODO methods to add multiple types at once?

    // can be called multiple times and is additive
    SyntheticBeanBuilder qualifier(Class<? extends Annotation> qualifierAnnotation, AnnotationAttribute... attributes);

    SyntheticBeanBuilder qualifier(ClassInfo<?> qualifierAnnotation, AnnotationAttribute... attributes);

    SyntheticBeanBuilder qualifier(AnnotationInfo qualifierAnnotation);

    SyntheticBeanBuilder qualifier(Annotation qualifierAnnotation);
    // TODO methods to add multiple qualifiers at once?

    // if called multiple times, last call wins
    SyntheticBeanBuilder scope(Class<? extends Annotation> scopeAnnotation);

    SyntheticBeanBuilder scope(ClassInfo<?> scopeAnnotation);

    // if called, priority is automatically 0, unless `priority` is also called
    // if called multiple times, last call wins
    SyntheticBeanBuilder alternative(boolean isAlternative);

    // if called, alternative is automatically true
    // if called multiple times, last call wins
    SyntheticBeanBuilder priority(int priority);

    // EL name (equivalent to @Named), IIUC
    // if called multiple times, last call wins
    SyntheticBeanBuilder name(String name);

    // can be called multiple times and is additive
    SyntheticBeanBuilder stereotype(Class<? extends Annotation> stereotypeAnnotation);

    SyntheticBeanBuilder stereotype(ClassInfo<?> stereotypeAnnotation);

    // TODO how to define the creation/destruction implementation?
}
