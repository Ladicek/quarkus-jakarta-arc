package cdi.lite.extension.phases.enhancement;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Constraints the {@link cdi.lite.extension.phases.Enhancement @Enhancement} method to given types.
 * If the {@code Enhancement} method has a parameter of type {@code ClassConfig},
 * the method is called once for each given type.
 * If the {@code @Enhancement} method has a parameter of type {@code MethodConfig} or {@code FieldConfig},
 * the method is called once for each method or field of each given type.
 * <p>
 * If the {@code annotatedWith} attribute is set, only types that use given annotations are considered.
 * The annotations can appear on the type, or on any member of the type, or any parameter of any member of the type.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ExactType.List.class)
public @interface ExactType {
    Class<?> type();

    // default = any annotation, does that make sense?
    Class<? extends Annotation>[] annotatedWith() default Annotation.class;

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        ExactType[] value();
    }
}
