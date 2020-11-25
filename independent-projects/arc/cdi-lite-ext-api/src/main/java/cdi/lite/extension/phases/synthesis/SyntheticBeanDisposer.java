package cdi.lite.extension.phases.synthesis;

import java.util.Map;
import javax.enterprise.context.spi.CreationalContext;

/**
 * Implementations must be {@code public} classes with a {@code public} zero-parameter constructor.
 *
 * @param <T> type of disposed instances
 */
public interface SyntheticBeanDisposer<T> {
    void dispose(T instance, CreationalContext<T> creationalContext, Map<String, Object> params);
}
