package cdi.lite.extension;

import java.lang.annotation.Annotation;
import java.util.function.Predicate;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;

public interface TypeConfigurator<T> {
    ClassInfo type();

    void addAnnotation(Class<? extends Annotation> clazz, AnnotationValue... values);

    void removeAnnotation(Predicate<AnnotationInstance> predicate);
}
