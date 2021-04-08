package io.quarkus.arc.processor.cdi.lite.ext;

import static io.quarkus.arc.processor.cdi.lite.ext.CdiLiteExtUtil.ExtensionMethodParameterType;
import static io.quarkus.arc.processor.cdi.lite.ext.CdiLiteExtUtil.Phase;

import cdi.lite.extension.beans.BeanInfo;
import cdi.lite.extension.beans.ObserverInfo;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Type;

class CdiLiteExtProcessingProcessor {
    private final CdiLiteExtUtil util;
    private final IndexView beanArchiveIndex;
    private final AllAnnotationOverlays annotationOverlays;
    private final Collection<io.quarkus.arc.processor.BeanInfo> allBeans;
    private final Collection<io.quarkus.arc.processor.ObserverInfo> allObservers;
    private final MessagesImpl messages;

    CdiLiteExtProcessingProcessor(CdiLiteExtUtil util, IndexView beanArchiveIndex, AllAnnotationOverlays annotationOverlays,
            Collection<io.quarkus.arc.processor.BeanInfo> allBeans,
            Collection<io.quarkus.arc.processor.ObserverInfo> allObservers,
            MessagesImpl messages) {
        this.util = util;
        this.beanArchiveIndex = beanArchiveIndex;
        this.annotationOverlays = annotationOverlays;
        this.allBeans = allBeans;
        this.allObservers = allObservers;
        this.messages = messages;
    }

    void run() {
        try {
            doRun();
        } catch (Exception e) {
            // TODO proper diagnostics system
            throw new RuntimeException(e);
        }
    }

    private void doRun() throws ReflectiveOperationException {
        List<org.jboss.jandex.MethodInfo> extensionMethods = util.findExtensionMethods(DotNames.PROCESSING);

        for (org.jboss.jandex.MethodInfo method : extensionMethods) {
            processExtensionMethod(method);
        }
    }

    private void processExtensionMethod(org.jboss.jandex.MethodInfo method) throws ReflectiveOperationException {
        List<org.jboss.jandex.AnnotationInstance> constraintAnnotations = constraintAnnotationsForExtensionMethod(method);

        int numParameters = method.parameters().size();
        int numQueryParameters = 0;
        List<ExtensionMethodParameterType> parameters = new ArrayList<>(numParameters);
        for (int i = 0; i < numParameters; i++) {
            org.jboss.jandex.Type parameterType = method.parameters().get(i);
            ExtensionMethodParameterType kind = ExtensionMethodParameterType.of(parameterType);
            parameters.add(kind);

            if (kind.isQuery()) {
                numQueryParameters++;
            }

            if (!kind.isAvailableIn(Phase.PROCESSING)) {
                throw new IllegalArgumentException("@Processing methods can't declare a parameter of type "
                        + parameterType + ", found at " + method + " @ " + method.declaringClass());
            }
        }

        if (numQueryParameters == 0) {
            throw new IllegalArgumentException("No parameter of type BeanInfo or ObserverInfo"
                    + " for method " + method + " @ " + method.declaringClass());
        }

        if (numQueryParameters > 1) {
            throw new IllegalArgumentException("More than 1 parameter of type BeanInfo or ObserverInfo"
                    + " for method " + method + " @ " + method.declaringClass());
        }

        if (constraintAnnotations.isEmpty()) {
            throw new IllegalArgumentException("Missing constraint annotation (@ExactType, @SubtypesOf) for method "
                    + method + " @ " + method.declaringClass());
        }

        ExtensionMethodParameterType query = parameters.stream()
                .filter(ExtensionMethodParameterType::isQuery)
                .findAny()
                .get(); // guaranteed to be there

        List<?> allValuesForQueryParameter = Collections.emptyList();
        if (query == ExtensionMethodParameterType.BEAN_INFO) {
            allValuesForQueryParameter = matchingBeans(constraintAnnotations);
        } else if (query == ExtensionMethodParameterType.OBSERVER_INFO) {
            allValuesForQueryParameter = matchingObservers(constraintAnnotations);
        }

        for (Object queryParameterValue : allValuesForQueryParameter) {
            List<Object> arguments = new ArrayList<>();
            for (ExtensionMethodParameterType parameter : parameters) {
                Object argument = parameter.isQuery()
                        ? queryParameterValue
                        : createArgumentForExtensionMethodParameter(method, parameter);
                arguments.add(argument);
            }

            util.callExtensionMethod(method, arguments);
        }
    }

    private List<org.jboss.jandex.AnnotationInstance> constraintAnnotationsForExtensionMethod(
            org.jboss.jandex.MethodInfo jandexMethod) {
        Stream<org.jboss.jandex.AnnotationInstance> exactTypeAnnotations = jandexMethod
                .annotationsWithRepeatable(DotNames.EXACT_TYPE, beanArchiveIndex).stream();
        Stream<org.jboss.jandex.AnnotationInstance> subtypesOfAnnotations = jandexMethod
                .annotationsWithRepeatable(DotNames.SUBTYPES_OF, beanArchiveIndex).stream();
        return Stream.concat(exactTypeAnnotations, subtypesOfAnnotations)
                .filter(it -> it.target().kind() == org.jboss.jandex.AnnotationTarget.Kind.METHOD)
                .collect(Collectors.toList());
    }

    private List<BeanInfo> matchingBeans(List<org.jboss.jandex.AnnotationInstance> constraintAnnotations) {
        Set<DotName> allMatchingTypes = allTypesMatchingConstraintAnnotations(constraintAnnotations);
        return allBeans.stream()
                .filter(bean -> {
                    for (Type type : bean.getTypes()) {
                        if (allMatchingTypes.contains(type.name())) {
                            return true;
                        }
                    }
                    return false;
                })
                .map(it -> new BeanInfoImpl(beanArchiveIndex, annotationOverlays, it))
                .collect(Collectors.toList());
    }

    private List<ObserverInfo> matchingObservers(List<org.jboss.jandex.AnnotationInstance> constraintAnnotations) {
        Set<DotName> allMatchingTypes = allTypesMatchingConstraintAnnotations(constraintAnnotations);
        return allObservers.stream()
                .filter(it -> allMatchingTypes.contains(it.getObservedType().name()))
                .map(it -> new ObserverInfoImpl(beanArchiveIndex, annotationOverlays, it))
                .collect(Collectors.toList());
    }

    private Set<DotName> allTypesMatchingConstraintAnnotations(List<AnnotationInstance> constraintAnnotations) {
        Set<DotName> result = new HashSet<>();
        for (AnnotationInstance constraintAnnotation : constraintAnnotations) {
            if (DotNames.EXACT_TYPE.equals(constraintAnnotation.name())) {
                Type type = constraintAnnotation.value("type").asClass();
                result.add(type.name());
            } else if (DotNames.SUBTYPES_OF.equals(constraintAnnotation.name())) {
                Type upperBound = constraintAnnotation.value("type").asClass();
                ClassInfo clazz = beanArchiveIndex.getClassByName(upperBound.name());
                // if clazz is null, should report an error here
                Collection<ClassInfo> allSubclasses = Modifier.isInterface(clazz.flags())
                        ? beanArchiveIndex.getAllKnownImplementors(upperBound.name())
                        : beanArchiveIndex.getAllKnownSubclasses(upperBound.name());
                // TODO index.getAllKnown* is not reflexive; should add the original type ourselves?
                for (ClassInfo allSubclass : allSubclasses) {
                    result.add(allSubclass.name());
                }
            }
        }
        return result;
    }

    private Object createArgumentForExtensionMethodParameter(org.jboss.jandex.MethodInfo method,
            ExtensionMethodParameterType kind) {
        switch (kind) {
            case TYPES:
                return new TypesImpl(beanArchiveIndex, annotationOverlays);
            case MESSAGES:
                return messages;

            default:
                throw new IllegalArgumentException(kind + " parameter declared for @Processing method "
                        + method + " @ " + method.declaringClass());
        }
    }
}
