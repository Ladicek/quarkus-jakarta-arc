package cdi.lite.extension.phases;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 3rd phase of CDI Lite extension processing.
 * Allows registering synthetic components (beans, observers, more?).
 * <p>
 * Extension methods annotated {@code @Synthesis} can define parameters of these types:
 * <ul>
 * <li>{@link cdi.lite.extension.phases.synthesis.SyntheticComponents}</li>
 * <li>types that allow inspecting the application
 * (that is, {@code ClassInfo<...>}, {@code Collection<ClassInfo<...>>}, {@code Collection<MethodInfo<...>>},
 * {@code Collection<FieldInfo<...>>}, and {@code World} and {@code Types})
 * </li>
 * <li>
 * types that allow inspecting the application beans
 * (that is, {@code Collection<BeanInfo<...>>}, {@code Collection<ObserverInfo<...>>})
 * (TODO more? also something similar to {@code World} for beans?)
 * </li>
 * </ul>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Synthesis {
}
