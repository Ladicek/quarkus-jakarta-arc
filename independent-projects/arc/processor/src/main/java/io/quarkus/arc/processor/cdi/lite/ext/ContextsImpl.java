package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.phases.discovery.ContextBuilder;
import cdi.lite.extension.phases.discovery.Contexts;
import java.util.List;

class ContextsImpl implements Contexts {
    final List<ContextBuilderImpl> contexts;

    ContextsImpl(List<ContextBuilderImpl> contexts) {
        this.contexts = contexts;
    }

    @Override
    public ContextBuilder add() {
        ContextBuilderImpl contextBuilder = new ContextBuilderImpl();
        contexts.add(contextBuilder);
        return contextBuilder;
    }

}
