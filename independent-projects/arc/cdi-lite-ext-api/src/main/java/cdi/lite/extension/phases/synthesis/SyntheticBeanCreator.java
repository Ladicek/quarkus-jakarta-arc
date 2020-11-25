package cdi.lite.extension.phases.synthesis;

import java.util.Map;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionPoint;

/**
 * Implementations must be {@code public} classes with a {@code public} zero-parameter constructor.
 *
 * @param <T> type of created instances
 */
public interface SyntheticBeanCreator<T> {
    T create(CreationalContext<T> creationalContext, InjectionPoint injectionPoint, Map<String, Object> params);
}
