package cdi.lite.extension.phases;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 4th phase of CDI Lite extension processing.
 * Allows custom validation.
 * <p>
 * Methods annotated {@code @Validation} can define parameters of these types:
 * <ul>
 *  TODO
 * </ul>
 * For advanced use cases, where this kind of queries is not powerful enough, the extension can also declare
 * a parameter of type {@link cdi.lite.extension.AppArchive AppArchive} or {@link cdi.lite.extension.AppDeployment
 * AppDeployment}.
 * <p>
 * If you need to create instances of {@link cdi.lite.extension.model.types.Type Type}, you can also declare
 * a parameter of type {@link cdi.lite.extension.Types Types}. It provides factory methods for the void type,
 * primitive types, class types, array types, parameterized types and wildcard types.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Validation {
}
