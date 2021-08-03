package javax.enterprise.inject.build.compatible.spi;

import javax.enterprise.inject.spi.Prioritized;

/**
 * Service provider interface for various services needed by {@link BuildCompatibleExtension} implementations.
 *
 * @since 4.0
 */
public interface BuildServices extends Prioritized {
    /**
     * @return the {@link AnnotationBuilderFactory} instance, never {@code null}
     */
    AnnotationBuilderFactory annotationBuilderFactory();
}
