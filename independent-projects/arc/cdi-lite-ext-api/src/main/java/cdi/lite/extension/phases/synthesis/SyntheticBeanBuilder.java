package cdi.lite.extension.phases.synthesis;

import cdi.lite.extension.model.declarations.ClassInfo;
import java.lang.annotation.Annotation;

/**
 * Instances are not reusable. For each synthetic bean, new instance must be created by {@link SyntheticComponents#addBean()}.
 */
public interface SyntheticBeanBuilder {
    // can be called multiple times and is additive
    SyntheticBeanBuilder type(Class<?> clazz);
    SyntheticBeanBuilder type(ClassInfo<?> clazz);
    // TODO methods to add multiple types at once, or even an entire transitive closure of types

    // can be called multiple times and is additive
    SyntheticBeanBuilder qualifier(Class<? extends Annotation> qualifierAnnotation);
    // TODO other variants of qualifier, to deal with annotation attributes
    // TODO methods to add multiple qualifiers at once

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

    // TODO stereotypes?

    // TODO anything else?

    // TODO this is probably useless, in such case, one can just add annotations to the class and be done with it
    SyntheticBeanBuilder implementation(Class<?> implementationClass);
    SyntheticBeanBuilder implementation(ClassInfo<?> implementationClass);

    // TODO how to define the creation/destruction implementation?
}
