package cdi.lite.extension.phases;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 3rd phase of CDI Lite extension processing.
 * Allows registering synthetic beans and observers.
 * <p>
 * Methods annotated {@code @Synthesis} can define parameters of these types:
 * <ul>
 * <li>{@link cdi.lite.extension.AppArchive AppArchive}: to find declarations in the application</li>
 * <li>{@link cdi.lite.extension.AppDeployment AppDeployment}: to find beans and observers in the application</li>
 * <li>{@link cdi.lite.extension.Messages Messages}: to produce log messages and validation errors</li>
 * <li>{@link cdi.lite.extension.phases.synthesis.SyntheticComponents SyntheticComponents}: to add synthetic beans
 * and observers to the application</li>
 * </ul>
 * If you need to create instances of {@link cdi.lite.extension.model.types.Type Type}, you can also declare
 * a parameter of type {@link cdi.lite.extension.Types Types}. It provides factory methods for the void type,
 * primitive types, class types, array types, parameterized types and wildcard types.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Synthesis {
}
