package javax.enterprise.inject.build.compatible.spi;

import javax.enterprise.lang.model.AnnotationAttribute;
import javax.enterprise.lang.model.AnnotationInfo;
import javax.enterprise.lang.model.declarations.ClassInfo;
import java.lang.annotation.Annotation;
import java.util.function.Predicate;

// TODO better name?
// TODO devise a builder-style API instead (see also Annotations)
public interface AnnotationConfig {
    void addAnnotation(Class<? extends Annotation> annotationType, AnnotationAttribute... attributes);

    void addAnnotation(ClassInfo<?> annotationType, AnnotationAttribute... attributes);

    void addAnnotation(AnnotationInfo annotation);

    void addAnnotation(Annotation annotation);

    void removeAnnotation(Predicate<AnnotationInfo> predicate);

    void removeAllAnnotations();
}
