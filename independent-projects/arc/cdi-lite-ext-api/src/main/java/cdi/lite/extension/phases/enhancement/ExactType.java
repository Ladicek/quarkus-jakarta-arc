package cdi.lite.extension.phases.enhancement;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExactType {
    Class<?> type();

    Class<? extends Annotation>[] annotatedWith();
}
