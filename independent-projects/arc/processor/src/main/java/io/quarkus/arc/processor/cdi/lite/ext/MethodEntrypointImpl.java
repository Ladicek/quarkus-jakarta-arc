package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.phases.enhancement.MethodConfig;
import cdi.lite.extension.phases.enhancement.MethodEntrypoint;
import java.util.List;
import java.util.function.Consumer;

class MethodEntrypointImpl implements MethodEntrypoint {
    private final List<MethodConfig<?>> methodConfigs;

    MethodEntrypointImpl(List<MethodConfig<?>> methodConfigs) {
        this.methodConfigs = methodConfigs;
    }

    @Override
    public void configure(Consumer<MethodConfig<?>> consumer) {
        for (MethodConfig<?> methodConfig : methodConfigs) {
            consumer.accept(methodConfig);
        }
    }
}
