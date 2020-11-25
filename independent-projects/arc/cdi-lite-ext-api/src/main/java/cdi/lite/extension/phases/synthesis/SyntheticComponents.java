package cdi.lite.extension.phases.synthesis;

public interface SyntheticComponents {
    <T> SyntheticBeanBuilder<T> addBean(Class<T> implementationClass);

    SyntheticObserverBuilder addObserver();
}
