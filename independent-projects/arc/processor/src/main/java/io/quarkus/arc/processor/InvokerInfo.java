package io.quarkus.arc.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

public class InvokerInfo implements InjectionTargetInfo {
    private final BeanDeployment beanDeployment;

    final BeanInfo targetBean;
    final ClassInfo targetBeanClass;
    final MethodInfo method;

    final boolean instanceLookup;
    final boolean[] argumentLookups;
    final Injection argumentInjection;

    final InvocationTransformer instanceTransformer;
    final InvocationTransformer[] argumentTransformers;
    final InvocationTransformer returnValueTransformer;
    final InvocationTransformer exceptionTransformer;

    final InvocationTransformer invocationWrapper;

    final String className;
    final String wrapperClassName;

    InvokerInfo(InvokerBuilder builder, Injection argumentInjection, BeanDeployment beanDeployment) {
        assert builder.argumentTransformers.length == builder.targetMethod.parametersCount();
        assert builder.argumentLookups.length == builder.targetMethod.parametersCount();

        this.beanDeployment = beanDeployment;

        this.targetBean = builder.targetBean;
        this.targetBeanClass = builder.targetBeanClass;
        this.method = builder.targetMethod;

        this.instanceLookup = builder.instanceLookup;
        this.argumentLookups = builder.argumentLookups;
        this.argumentInjection = argumentInjection;

        this.instanceTransformer = builder.instanceTransformer;
        this.argumentTransformers = builder.argumentTransformers;
        this.returnValueTransformer = builder.returnValueTransformer;
        this.exceptionTransformer = builder.exceptionTransformer;

        this.invocationWrapper = builder.invocationWrapper;

        String hash = methodHash(builder);
        this.className = builder.targetMethod.declaringClass().name() + "_" + builder.targetMethod.name() + "_Invoker_" + hash;
        this.wrapperClassName = invocationWrapper != null
                ? builder.targetMethod.declaringClass().name() + "_" + builder.targetMethod.name() + "_InvokerWrapper_" + hash
                : null;
    }

    private static String methodHash(InvokerBuilder builder) {
        StringBuilder str = new StringBuilder();
        str.append(builder.targetBean.getIdentifier());
        str.append(builder.targetBeanClass.name());
        str.append(builder.targetMethod.declaringClass().name());
        str.append(builder.targetMethod.name());
        str.append(builder.targetMethod.returnType().name());
        for (Type parameterType : builder.targetMethod.parameterTypes()) {
            str.append(parameterType.name());
        }
        str.append(builder.instanceTransformer);
        str.append(Arrays.toString(builder.argumentTransformers));
        str.append(builder.returnValueTransformer);
        str.append(builder.exceptionTransformer);
        str.append(builder.invocationWrapper);
        str.append(builder.instanceLookup);
        str.append(Arrays.toString(builder.argumentLookups));
        return Hashes.sha1_base64(str.toString());
    }

    public String getClassName() {
        return wrapperClassName != null ? wrapperClassName : className;
    }

    @Override
    public String toString() {
        return "invoker of " + targetBeanClass.name() + "#" + method.name();
    }

    @Override
    public TargetKind kind() {
        return TargetKind.INVOKER;
    }

    @Override
    public InvokerInfo asInvoker() {
        return this;
    }

    void init(List<Throwable> errors) {
        for (InjectionPointInfo injectionPoint : argumentInjection.injectionPoints) {
            Beans.resolveInjectionPoint(beanDeployment, this, injectionPoint, errors);
        }
    }

    InjectionPointInfo getInjectionPointForArgument(int position) {
        if (argumentLookups[position]) {
            for (InjectionPointInfo injectionPoint : argumentInjection.injectionPoints) {
                if (injectionPoint.getPosition() == position) {
                    return injectionPoint;
                }
            }
        }
        return null;
    }

    List<BeanInfo> getLookedUpBeans() {
        List<BeanInfo> result = new ArrayList<>();
        if (instanceLookup) {
            result.add(targetBean);
        }
        for (int i = 0; i < argumentLookups.length; i++) {
            InjectionPointInfo injectionPoint = getInjectionPointForArgument(i);
            if (injectionPoint != null) {
                result.add(injectionPoint.getResolvedBean());
            }
        }
        return result;
    }
}
