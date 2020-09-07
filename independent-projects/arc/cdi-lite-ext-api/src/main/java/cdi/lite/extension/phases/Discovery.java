package cdi.lite.extension.phases;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 1st phase of CDI Lite extension processing.
 * Allow registering additional classes that are not part of application.
 * Also allows registering custom CDI contexts.
 * <p>
 * Extension methods annotated {@code @Discovery} can define parameters of these types:
 * <ul>
 * <li>{@link cdi.lite.extension.phases.discovery.Classes}</li>
 * <li>{@link cdi.lite.extension.phases.discovery.Contexts}</li>
 * </ul>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Discovery {
}
