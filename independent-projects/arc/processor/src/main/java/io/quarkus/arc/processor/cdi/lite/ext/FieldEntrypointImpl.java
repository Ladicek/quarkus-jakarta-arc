package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.phases.enhancement.FieldConfig;
import cdi.lite.extension.phases.enhancement.FieldEntrypoint;
import java.util.List;
import java.util.function.Consumer;

class FieldEntrypointImpl implements FieldEntrypoint {
    private final List<FieldConfig<?>> fieldConfigs;

    FieldEntrypointImpl(List<FieldConfig<?>> fieldConfigs) {
        this.fieldConfigs = fieldConfigs;
    }

    @Override
    public void configure(Consumer<FieldConfig<?>> consumer) {
        for (FieldConfig<?> fieldConfig : fieldConfigs) {
            consumer.accept(fieldConfig);
        }
    }
}
