package cdi.lite.extension.model.configs;

import cdi.lite.extension.model.AnnotationAttribute;
import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.declarations.ClassInfo;
import java.lang.annotation.Annotation;
import java.util.function.Predicate;

/**
 * @param <T> the configured class
 */
public interface ClassConfig<T> extends ClassInfo<T> {
    void addAnnotation(Class<? extends Annotation> clazz, AnnotationAttribute... attributes);

    void addAnnotation(ClassInfo<?> clazz, AnnotationAttribute... attributes);

    void addAnnotation(AnnotationInfo annotation);

    void removeAnnotation(Predicate<AnnotationInfo> predicate);
}
