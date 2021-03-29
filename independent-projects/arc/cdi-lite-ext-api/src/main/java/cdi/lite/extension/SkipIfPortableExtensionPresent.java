package cdi.lite.extension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If a {@link BuildCompatibleExtension} is annotated {@code @SkipIfPortablExtensionPresent},
 * it is skipped when the CDI implementation is able to execute portable extensions and
 * the specified class is present on the classpath as visible by the current thread's
 * context classloader. It is expected that the specified class will be a class of a portable
 * extension that mirrors the functionality of the annotated build compatible extension.
 * <p>
 * This allows for gradual migration from portable extensions to build compatible extensions.
 * It is expected that a build compatible extension will be packaged together with the corresponding
 * portable extension, so checking whether the class is present should always yield the correct
 * result.
 *
 * @see #value()
 */
// TODO perhaps should check if the BeanManager has an instance of the extension class?
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SkipIfPortableExtensionPresent {
    /**
     * Fully qualified class name that will be searched for in the current thread's context
     * classloader. It is expected that the value will be a class of a portable extension
     * that mirrors the functionality of the annotated build compatible extension.
     */
    String value();
}
