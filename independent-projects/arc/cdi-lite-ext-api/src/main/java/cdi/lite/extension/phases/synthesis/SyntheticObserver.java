package cdi.lite.extension.phases.synthesis;

import javax.enterprise.inject.spi.EventContext;

/**
 * Implementations must be {@code public} classes with a {@code public} zero-parameter constructor.
 *
 * @param <T> type of observed event instances
 */
public interface SyntheticObserver<T> {
    void observe(EventContext<T> event);
}
