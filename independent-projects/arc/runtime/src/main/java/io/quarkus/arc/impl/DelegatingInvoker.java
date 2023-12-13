package io.quarkus.arc.impl;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import jakarta.enterprise.invoke.Invoker;

// this is a workaround (hopefully temporary)
public class DelegatingInvoker<T, R> implements Invoker<T, R> {
    private final AtomicReference<Invoker<T, R>> delegate = new AtomicReference<>();
    private final Supplier<Invoker<T, R>> supplier;

    public DelegatingInvoker(Supplier<Invoker<T, R>> supplier) {
        this.supplier = supplier;
    }

    @Override
    public R invoke(T instance, Object[] arguments) throws Exception {
        Invoker<T, R> invoker = delegate.get();
        if (invoker == null) {
            delegate.compareAndSet(null, supplier.get());
            invoker = delegate.get();
        }
        return invoker.invoke(instance, arguments);
    }
}
