package cdi.lite.extension.phases;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 3rd phase of CDI Lite extension processing.
 * Allows processing registered beans and observers.
 * Note that synthetic beans and observers, registered in {@link Synthesis}, will <i>not</i> be processed.
 * <p>
 * Methods annotated {@code @Processing} must define exactly one parameter of one of these types:
 * <ul>
 * <li>{@link cdi.lite.extension.beans.BeanInfo BeanInfo}</li>
 * <li>{@link cdi.lite.extension.beans.ObserverInfo ObserverInfo}</li>
 * </ul>
 * The method must also have at least one annotation {@link cdi.lite.extension.phases.enhancement.ExactType @ExactType}
 * or {@link cdi.lite.extension.phases.enhancement.SubtypesOf @SubtypesOf}.
 * <p>
 * You can also declare a parameter of type {@link cdi.lite.extension.Messages Messages}
 * to produce log messages and validation errors.
 * <p>
 * If you need to create instances of {@link cdi.lite.extension.model.types.Type Type}, you can also declare
 * a parameter of type {@link cdi.lite.extension.Types Types}. It provides factory methods for the void type,
 * primitive types, class types, array types, parameterized types and wildcard types.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Processing {
}
