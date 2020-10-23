package cdi.lite.extension.phases.enhancement;

import cdi.lite.extension.model.AnnotationAttribute;
import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.declarations.ClassInfo;
import java.lang.annotation.Annotation;
import java.util.function.Consumer;
import java.util.function.Predicate;

// TODO better name!
public interface BaseEntrypoint<T extends AnnotationConfig> {
    void configure(Consumer<T> consumer);

    default void addAnnotation(Class<? extends Annotation> annotationType, AnnotationAttribute... attributes) {
        configure(config -> config.addAnnotation(annotationType, attributes));
    }

    default void addAnnotation(ClassInfo<?> annotationType, AnnotationAttribute... attributes) {
        configure(config -> config.addAnnotation(annotationType, attributes));
    }

    default void addAnnotation(AnnotationInfo annotation) {
        configure(config -> config.addAnnotation(annotation));
    }

    default void addAnnotation(Annotation annotation) {
        configure(config -> config.addAnnotation(annotation));
    }

    default void removeAnnotation(Predicate<AnnotationInfo> predicate) {
        configure(config -> config.removeAnnotation(predicate));
    }

    default void removeAllAnnotations() {
        configure(config -> config.removeAllAnnotations());
    }
}
