package cdi.lite.extension.phases;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 4th phase of CDI Lite extension processing.
 * Allows custom validation.
 * <p>
 * Extension methods annotated {@code @Validation} can define parameters of these types:
 * <ul>
 *     <li>{@link cdi.lite.extension.phases.validation.Errors}</li>
 *     <li>types that allow inspecting the application
 *          (that is, {@code ClassInfo<...>}, {@code Collection<ClassInfo<...>>}, {@code Collection<MethodInfo<...>>},
 *          {@code Collection<FieldInfo<...>>}, and {@code World} and {@code Types})
 *     </li>
 * </ul>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Validation {
}
