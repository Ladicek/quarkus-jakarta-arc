package io.quarkus.arc.processor.cdi.lite.ext;

import java.util.List;
import javax.enterprise.inject.build.compatible.spi.SyntheticBeanBuilder;
import javax.enterprise.inject.build.compatible.spi.SyntheticComponents;
import javax.enterprise.inject.build.compatible.spi.SyntheticObserverBuilder;
import org.jboss.jandex.DotName;

class SyntheticComponentsImpl implements SyntheticComponents {
    final List<SyntheticBeanBuilderImpl<?>> syntheticBeans;
    final List<SyntheticObserverBuilderImpl> syntheticObservers;
    final DotName extensionClass;

    SyntheticComponentsImpl(List<SyntheticBeanBuilderImpl<?>> syntheticBeans,
            List<SyntheticObserverBuilderImpl> syntheticObservers, DotName extensionClass) {
        this.syntheticBeans = syntheticBeans;
        this.syntheticObservers = syntheticObservers;
        this.extensionClass = extensionClass;
    }

    @Override
    public <T> SyntheticBeanBuilder<T> addBean(Class<T> implementationClass) {
        SyntheticBeanBuilderImpl<T> builder = new SyntheticBeanBuilderImpl<>(implementationClass);
        syntheticBeans.add(builder);
        return builder;
    }

    @Override
    public SyntheticObserverBuilder addObserver() {
        SyntheticObserverBuilderImpl builder = new SyntheticObserverBuilderImpl(extensionClass);
        syntheticObservers.add(builder);
        return builder;
    }
}
