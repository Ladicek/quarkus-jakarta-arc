package io.quarkus.arc.tck.porting;

import jakarta.enterprise.context.spi.Context;

import org.jboss.cdi.tck.spi.Contexts;

import io.quarkus.arc.Arc;
import io.quarkus.arc.InjectableContext;
import io.quarkus.arc.ManagedContext;

public class ContextsImpl implements Contexts<Context> {
    @Override
    public void setActive(Context context) {
        ((ManagedContext) context).activate();
    }

    @Override
    public void setInactive(Context context) {
        ((ManagedContext) context).deactivate();
    }

    @Override
    public Context getRequestContext() {
        return Arc.container().requestContext();
    }

    @Override
    public Context getDependentContext() {
        return Arc.container().dependentContext();
    }

    @Override
    public void destroyContext(Context context) {
        ((InjectableContext) context).destroy();
    }
}
