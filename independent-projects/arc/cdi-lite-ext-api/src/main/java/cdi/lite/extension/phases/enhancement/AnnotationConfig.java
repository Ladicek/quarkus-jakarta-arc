package cdi.lite.extension.phases.enhancement;

import cdi.lite.extension.model.AnnotationAttribute;
import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.declarations.ClassInfo;
import java.lang.annotation.Annotation;
import java.util.function.Predicate;

public interface AnnotationConfig {
    void addAnnotation(Class<? extends Annotation> annotationType, AnnotationAttribute... attributes);

    void addAnnotation(ClassInfo<?> annotationType, AnnotationAttribute... attributes);

    void addAnnotation(AnnotationInfo annotation);

    void addAnnotation(Annotation annotation);

    void removeAnnotation(Predicate<AnnotationInfo> predicate);

    void removeAllAnnotations();
}
