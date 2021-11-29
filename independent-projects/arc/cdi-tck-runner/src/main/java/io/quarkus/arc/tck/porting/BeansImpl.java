package io.quarkus.arc.tck.porting;

import io.quarkus.arc.ClientProxy;
import org.jboss.cdi.tck.spi.Beans;

public class BeansImpl implements Beans {
    @Override
    public boolean isProxy(Object o) {
        return o instanceof ClientProxy;
    }

    @Override
    public byte[] passivate(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object activate(byte[] bytes) {
        throw new UnsupportedOperationException();
    }
}
