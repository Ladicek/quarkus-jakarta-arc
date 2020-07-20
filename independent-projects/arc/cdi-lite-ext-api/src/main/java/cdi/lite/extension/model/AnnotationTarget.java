package cdi.lite.extension.model;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * Annotation target is anything that can be annotated.
 * That is:
 *
 * <ul>
 * <li>a <i>declaration</i>, such as a class, method, field, etc.</li>
 * <li>a <i>type parameter</i>, such as when declaring a parameterized class, etc.</li>
 * <li>a <i>type use</i>, such as a type of a method parameter, a type of field, a type argument, etc.</li>
 * </ul>
 */
public interface AnnotationTarget {
    boolean hasAnnotation(Class<? extends Annotation> annotationType);

    AnnotationInfo annotation(Class<? extends Annotation> annotationType);

    Collection<AnnotationInfo> repeatableAnnotation(Class<? extends Annotation> annotationType);

    Collection<AnnotationInfo> annotations();
}
