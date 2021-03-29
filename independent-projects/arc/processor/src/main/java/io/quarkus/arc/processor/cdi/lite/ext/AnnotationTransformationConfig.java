package io.quarkus.arc.processor.cdi.lite.ext;

import java.util.function.Predicate;

final class AnnotationTransformationConfig {
    // we may want to allow transforming annotations on annotations, but for now, let's filter them out
    // to simplify comparison with Portable Extensions
    static final Predicate<org.jboss.jandex.ClassInfo> FILTER = clazz -> !clazz.isAnnotation();
}
