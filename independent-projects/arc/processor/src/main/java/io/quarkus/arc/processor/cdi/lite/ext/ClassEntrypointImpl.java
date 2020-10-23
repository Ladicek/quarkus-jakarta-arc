package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.phases.enhancement.ClassConfig;
import cdi.lite.extension.phases.enhancement.ClassEntrypoint;
import java.util.List;
import java.util.function.Consumer;

class ClassEntrypointImpl implements ClassEntrypoint {
    private final List<ClassConfig<?>> classConfigs;

    ClassEntrypointImpl(List<ClassConfig<?>> classConfigs) {
        this.classConfigs = classConfigs;
    }

    @Override
    public void configure(Consumer<ClassConfig<?>> consumer) {
        for (ClassConfig<?> classConfig : classConfigs) {
            consumer.accept(classConfig);
        }
    }
}
