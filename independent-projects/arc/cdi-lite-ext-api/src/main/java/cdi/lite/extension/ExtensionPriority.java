package cdi.lite.extension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows specifying priority of extensions.
 * <p>
 * Extensions with specified priority always precede extensions without any priority.
 * Extension with highest priority get invoked first. If two extensions have equal
 * priority, the ordering is undefined.
 * TODO should really figure out if low number = high priority or otherwise, preferrably
 *  so that it's consistent with common usages of `@Priority`
 * TODO now that extensions are not methods but service providers for BuildCompatibleExtension,
 *  should perhaps priority be assigned on the class level? or should both be possible?
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExtensionPriority {
    /**
     * The priority value.
     */
    int value();
}
