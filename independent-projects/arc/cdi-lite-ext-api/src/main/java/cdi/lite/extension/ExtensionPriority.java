package cdi.lite.extension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows specifying priority of {@link Extension}s.
 * <p>
 * Extensions with specified priority always precede extensions without any priority.
 * Extension with highest priority get invoked first. If two extensions have equal
 * priority, the ordering is undefined.
 * TODO should really figure out if low number = high priority or otherwise, preferrably
 *  so that it's consistent with common usages of `@Priority`
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExtensionPriority {
    /**
     * The priority value.
     */
    int value();
}
