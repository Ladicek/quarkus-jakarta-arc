package cdi.lite.extension.phases;

import cdi.lite.extension.phases.enhancement.Annotations;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 2nd phase of CDI Lite extension processing.
 * Allows transforming annotations.
 * <p>
 * Extension methods annotated {@code @Enhancement} can define parameters of these types:
 * <ul>
 * <li>classes that allow inspecting the application
 * (that is, {@code ClassInfo<...>}, {@code Collection<ClassInfo<...>>}, {@code Collection<MethodInfo<...>>},
 * {@code Collection<FieldInfo<...>>}, and {@code World})
 * </li>
 * <li>classes that allow modifying the application
 * (that is, {@code ClassConfig<...>}, {@code Collection<ClassConfig<...>>}, {@code Collection<MethodConfig<...>>},
 * {@code Collection<FieldConfig<...>>}, and {@code World})
 * (TODO need to distinguish readonly {@code World} from writable {@code World}?)
 * </li>
 * <li>supplementary types: {@link Annotations}, {@link cdi.lite.extension.Types Types}</li>
 * </ul>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Enhancement {
}
