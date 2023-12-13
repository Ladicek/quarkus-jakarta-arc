package io.quarkus.arc.processor;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.MethodInfo;

public class InvokerBuilder {
    final BeanInfo targetBean;
    final ClassInfo targetBeanClass;
    final MethodInfo targetMethod;

    boolean instanceLookup;
    boolean[] argumentLookups;

    InvocationTransformer instanceTransformer;
    InvocationTransformer[] argumentTransformers;
    InvocationTransformer returnValueTransformer;
    InvocationTransformer exceptionTransformer;

    InvocationTransformer invocationWrapper;

    private final BeanDeployment beanDeployment;
    private final InjectionPointModifier injectionPointTransformer;

    InvokerBuilder(BeanInfo targetBean, MethodInfo targetMethod, BeanDeployment beanDeployment,
            InjectionPointModifier injectionPointTransformer) {
        this.targetBean = targetBean;
        this.targetBeanClass = targetBean.getImplClazz();
        this.targetMethod = targetMethod;

        this.argumentTransformers = new InvocationTransformer[targetMethod.parametersCount()];
        this.argumentLookups = new boolean[targetMethod.parametersCount()];

        this.beanDeployment = beanDeployment;
        this.injectionPointTransformer = injectionPointTransformer;
    }

    public InvokerBuilder withInstanceLookup() {
        instanceLookup = true;
        return this;
    }

    public InvokerBuilder withArgumentLookup(int position) {
        if (position < 0 || position >= argumentLookups.length) {
            throw new IllegalArgumentException(); // TODO
        }

        argumentLookups[position] = true;
        return this;
    }

    public InvokerBuilder withInstanceTransformer(Class<?> clazz, String methodName) {
        if (instanceTransformer != null) {
            throw new IllegalStateException(); // TODO
        }

        this.instanceTransformer = new InvocationTransformer(InvocationTransformerKind.INSTANCE, clazz, methodName);
        return this;
    }

    public InvokerBuilder withArgumentTransformer(int position, Class<?> clazz, String methodName) {
        if (position >= argumentTransformers.length) {
            throw new IllegalArgumentException(); // TODO
        }
        if (argumentTransformers[position] != null) {
            throw new IllegalStateException(); // TODO
        }

        this.argumentTransformers[position] = new InvocationTransformer(InvocationTransformerKind.ARGUMENT, clazz, methodName);
        return this;
    }

    public InvokerBuilder withReturnValueTransformer(Class<?> clazz, String methodName) {
        if (returnValueTransformer != null) {
            throw new IllegalStateException(); // TODO
        }

        this.returnValueTransformer = new InvocationTransformer(InvocationTransformerKind.RETURN_VALUE, clazz, methodName);
        return this;
    }

    public InvokerBuilder withExceptionTransformer(Class<?> clazz, String methodName) {
        if (exceptionTransformer != null) {
            throw new IllegalStateException(); // TODO
        }

        this.exceptionTransformer = new InvocationTransformer(InvocationTransformerKind.EXCEPTION, clazz, methodName);
        return this;
    }

    public InvokerBuilder withInvocationWrapper(Class<?> clazz, String methodName) {
        if (invocationWrapper != null) {
            throw new IllegalStateException(); // TODO
        }

        this.invocationWrapper = new InvocationTransformer(InvocationTransformerKind.WRAPPER, clazz, methodName);
        return this;
    }

    public InvokerInfo build() {
        Injection argumentsInjection = Injection.forInvokerArgumentLookups(targetBean.getImplClazz(), targetMethod,
                argumentLookups, beanDeployment, injectionPointTransformer);
        InvokerInfo result = new InvokerInfo(this, argumentsInjection, beanDeployment);

        beanDeployment.addInvoker(result);
        return result;
    }
}
