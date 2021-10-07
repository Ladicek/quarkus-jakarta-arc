package io.quarkus.arc.processor.cdi.lite.ext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.inject.build.compatible.spi.BeanInfo;
import javax.enterprise.inject.build.compatible.spi.ObserverInfo;
import org.jboss.jandex.DotName;

class ExtensionPhaseRegistration extends ExtensionPhaseBase {
    private final AllAnnotationOverlays annotationOverlays;
    private final Collection<io.quarkus.arc.processor.BeanInfo> allBeans;
    private final Collection<io.quarkus.arc.processor.ObserverInfo> allObservers;

    ExtensionPhaseRegistration(ExtensionInvoker invoker, org.jboss.jandex.IndexView beanArchiveIndex, MessagesImpl messages,
            AllAnnotationOverlays annotationOverlays, Collection<io.quarkus.arc.processor.BeanInfo> allBeans,
            Collection<io.quarkus.arc.processor.ObserverInfo> allObservers) {
        super(ExtensionPhase.REGISTRATION, invoker, beanArchiveIndex, messages);
        this.annotationOverlays = annotationOverlays;
        this.allBeans = allBeans;
        this.allObservers = allObservers;
    }

    void runExtensionMethod(org.jboss.jandex.MethodInfo method) throws ReflectiveOperationException {
        int numParameters = method.parameters().size();
        int numQueryParameters = 0;
        List<ExtensionMethodParameterType> parameters = new ArrayList<>(numParameters);
        for (int i = 0; i < numParameters; i++) {
            org.jboss.jandex.Type parameterType = method.parameters().get(i);
            ExtensionMethodParameterType parameter = ExtensionMethodParameterType.of(parameterType);
            parameters.add(parameter);

            if (parameter.isQuery()) {
                numQueryParameters++;
            }

            parameter.verifyAvailable(ExtensionPhase.REGISTRATION, method);
        }

        if (numQueryParameters == 0) {
            throw new IllegalArgumentException("No parameter of type BeanInfo or ObserverInfo"
                    + " for method " + method + " @ " + method.declaringClass());
        }

        if (numQueryParameters > 1) {
            throw new IllegalArgumentException("More than 1 parameter of type BeanInfo or ObserverInfo"
                    + " for method " + method + " @ " + method.declaringClass());
        }

        ExtensionMethodParameterType query = parameters.stream()
                .filter(ExtensionMethodParameterType::isQuery)
                .findAny()
                .get(); // guaranteed to be there

        List<?> allValuesForQueryParameter = Collections.emptyList();
        if (query == ExtensionMethodParameterType.BEAN_INFO) {
            allValuesForQueryParameter = matchingBeans(method, false);
        } else if (query == ExtensionMethodParameterType.INTERCEPTOR_INFO) {
            allValuesForQueryParameter = matchingBeans(method, true);
        } else if (query == ExtensionMethodParameterType.OBSERVER_INFO) {
            allValuesForQueryParameter = matchingObservers(method);
        }

        for (Object queryParameterValue : allValuesForQueryParameter) {
            List<Object> arguments = new ArrayList<>();
            for (ExtensionMethodParameterType parameter : parameters) {
                Object argument = parameter.isQuery()
                        ? queryParameterValue
                        : argumentForExtensionMethod(parameter, method);
                arguments.add(argument);
            }

            util.callExtensionMethod(method, arguments);
        }
    }

    private Set<DotName> expectedTypes(org.jboss.jandex.MethodInfo jandexMethod) {
        org.jboss.jandex.Type[] annotationValue = jandexMethod.annotation(DotNames.REGISTRATION)
                .value("types").asClassArray();
        return Arrays.stream(annotationValue)
                .map(org.jboss.jandex.Type::name)
                .collect(Collectors.toUnmodifiableSet());
    }

    private List<BeanInfo> matchingBeans(org.jboss.jandex.MethodInfo jandexMethod, boolean onlyInterceptors) {
        Set<DotName> expectedTypes = expectedTypes(jandexMethod);
        return allBeans.stream()
                .filter(bean -> {
                    if (onlyInterceptors && !bean.isInterceptor()) {
                        return false;
                    }
                    for (org.jboss.jandex.Type type : bean.getTypes()) {
                        if (expectedTypes.contains(type.name())) {
                            return true;
                        }
                    }
                    return false;
                })
                .map(it -> BeanInfoImpl.create(index, annotationOverlays, it))
                .collect(Collectors.toUnmodifiableList());
    }

    private List<ObserverInfo> matchingObservers(org.jboss.jandex.MethodInfo jandexMethod) {
        Set<DotName> expectedTypes = expectedTypes(jandexMethod);
        // TODO observed event type may be a _subtype_ of an expected type!
        return allObservers.stream()
                .filter(it -> expectedTypes.contains(it.getObservedType().name()))
                .map(it -> new ObserverInfoImpl(index, annotationOverlays, it))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    Object argumentForExtensionMethod(ExtensionMethodParameterType type, org.jboss.jandex.MethodInfo method) {
        if (type == ExtensionMethodParameterType.TYPES) {
            return new TypesImpl(index, annotationOverlays);
        }

        return super.argumentForExtensionMethod(type, method);
    }
}
