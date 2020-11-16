package io.quarkus.arc.processor.cdi.lite.ext;

import io.quarkus.arc.processor.BeanProcessor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jboss.jandex.DotName;

public class CdiLiteExtEnhancementProcessor extends CdiLiteExtProcessorBase {
    private final org.jboss.jandex.IndexView beanArchiveIndex;
    private final BeanProcessor.Builder builder;
    private final AllAnnotationOverlays annotationOverlays;
    private final AllAnnotationTransformations annotationTransformations;

    public CdiLiteExtEnhancementProcessor(org.jboss.jandex.IndexView beanArchiveIndex, BeanProcessor.Builder builder) {
        this.beanArchiveIndex = beanArchiveIndex;
        this.builder = builder;
        this.annotationOverlays = new AllAnnotationOverlays();
        this.annotationTransformations = new AllAnnotationTransformations(beanArchiveIndex, annotationOverlays);
    }

    public void run() {
        try {
            builder.addAnnotationTransformer(annotationTransformations.classes);
            builder.addAnnotationTransformer(annotationTransformations.methods);
            builder.addAnnotationTransformer(annotationTransformations.fields);

            doRun();
        } catch (Exception e) {
            // TODO proper diagnostics system
            throw new RuntimeException(e);
        } finally {
            annotationOverlays.invalidate();
            annotationTransformations.freeze();
        }
    }

    private void doRun() throws ReflectiveOperationException {
        List<org.jboss.jandex.MethodInfo> extensionMethods = findExtensionMethods(DotNames.ENHANCEMENT);

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

            if (!kind.isAvailableIn(Phase.ENHANCEMENT)) { // we don't implement anything else yet
                throw new IllegalArgumentException("@Enhancement methods can't declare a parameter of type "
                        + parameterType + ", found at " + method + " @ " + method.declaringClass());
            }
        }

        if (numQueryParameters > 1) {
            // TODO also AppArchive[Config]
            throw new IllegalArgumentException("More than 1 parameter of type ClassConfig, MethodConfig or FieldConfig"
                    + " for method " + method + " @ " + method.declaringClass());
        }

        if (numQueryParameters > 0 && constraintAnnotations.isEmpty()) {
            throw new IllegalArgumentException("Missing constraint annotation (@ExactType, @SubtypesOf) for method "
                    + method + " @ " + method.declaringClass());
        }

        if (numQueryParameters == 0) {
            List<Object> arguments = new ArrayList<>(numParameters);
            for (ExtensionMethodParameterType parameter : parameters) {
                Object argument = createArgumentForExtensionMethodParameter(parameter);
                arguments.add(argument);
            }

            callExtensionMethod(method, arguments);
        } else {
            ExtensionMethodParameterType query = parameters.stream()
                    .filter(ExtensionMethodParameterType::isQuery)
                    .findAny()
                    .get(); // guaranteed to be there

            List<org.jboss.jandex.ClassInfo> matchingClasses = matchingClassesForExtensionMethod(constraintAnnotations);
            List<Object> allValuesForQueryParameter;
            if (query == ExtensionMethodParameterType.CLASS_CONFIG) {
                allValuesForQueryParameter = matchingClasses.stream()
                        .map(it -> new ClassConfigImpl(beanArchiveIndex, annotationTransformations.classes, it))
                        .collect(Collectors.toList());
            } else if (query == ExtensionMethodParameterType.METHOD_CONFIG) {
                allValuesForQueryParameter = matchingClasses.stream()
                        .flatMap(it -> it.methods().stream())
                        .filter(MethodPredicates.IS_METHOD_OR_CONSTRUCTOR_JANDEX)
                        .map(it -> new MethodConfigImpl(beanArchiveIndex, annotationTransformations.methods, it))
                        .collect(Collectors.toList());
            } else if (query == ExtensionMethodParameterType.FIELD_CONFIG) {
                allValuesForQueryParameter = matchingClasses.stream()
                        .flatMap(it -> it.fields().stream())
                        .map(it -> new FieldConfigImpl(beanArchiveIndex, annotationTransformations.fields, it))
                        .collect(Collectors.toList());
            } else {
                // TODO internal error
                allValuesForQueryParameter = Collections.emptyList();
            }

            for (Object queryParameterValue : allValuesForQueryParameter) {
                List<Object> arguments = new ArrayList<>();
                for (ExtensionMethodParameterType parameter : parameters) {
                    Object argument = parameter.isQuery()
                            ? queryParameterValue
                            : createArgumentForExtensionMethodParameter(parameter);
                    arguments.add(argument);
                }

                callExtensionMethod(method, arguments);
            }
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

    private Set<DotName> requiredAnnotationsForConstraintAnnotation(org.jboss.jandex.AnnotationInstance constraintAnnotation) {
        if (constraintAnnotation == null) {
            return null;
        }

        org.jboss.jandex.AnnotationValue annotatedWith = constraintAnnotation.value("annotatedWith");
        if (annotatedWith != null) {
            org.jboss.jandex.Type[] types = annotatedWith.asClassArray();

            if (types.length == 1 && DotNames.ANNOTATION.equals(types[0].name())) {
                return null;
            }

            if (types.length > 0) {
                return Arrays.stream(types)
                        .map(org.jboss.jandex.Type::asClassType)
                        .map(org.jboss.jandex.Type::name)
                        .collect(Collectors.toSet());
            }
        }

        return null;
    }

    private List<org.jboss.jandex.ClassInfo> matchingClassesForExtensionMethod(
            List<org.jboss.jandex.AnnotationInstance> constraintAnnotations) {
        return constraintAnnotations.stream()
                .flatMap(constraintAnnotation -> {
                    Collection<org.jboss.jandex.ClassInfo> result;

                    if (DotNames.EXACT_TYPE.equals(constraintAnnotation.name())) {
                        org.jboss.jandex.Type jandexType = constraintAnnotation.value("type").asClass();
                        org.jboss.jandex.ClassInfo clazz = beanArchiveIndex.getClassByName(jandexType.name());
                        // if clazz is null, should report an error here
                        result = Collections.singletonList(clazz);
                    } else if (DotNames.SUBTYPES_OF.equals(constraintAnnotation.name())) {
                        org.jboss.jandex.Type upperBound = constraintAnnotation.value("type").asClass();
                        org.jboss.jandex.ClassInfo clazz = beanArchiveIndex.getClassByName(upperBound.name());
                        // if clazz is null, should report an error here
                        result = Modifier.isInterface(clazz.flags())
                                ? beanArchiveIndex.getAllKnownImplementors(upperBound.name())
                                : beanArchiveIndex.getAllKnownSubclasses(upperBound.name());
                        // TODO index.getAllKnown* is not reflexive; should add the original type ourselves?
                    } else {
                        // TODO internal error
                        result = Collections.emptyList();
                    }

                    Set<DotName> requiredAnnotations = requiredAnnotationsForConstraintAnnotation(constraintAnnotation);
                    if (requiredAnnotations != null) {
                        result = result.stream()
                                .filter(it -> it.annotations().keySet().stream().anyMatch(requiredAnnotations::contains))
                                .collect(Collectors.toList());
                    }

                    return result.stream();
                })
                .collect(Collectors.toList());
    }

    private Object createArgumentForExtensionMethodParameter(ExtensionMethodParameterType kind) {
        switch (kind) {
            case ANNOTATIONS:
                return new AnnotationsImpl(beanArchiveIndex, annotationOverlays);
            case APP_ARCHIVE:
                return new AppArchiveImpl(beanArchiveIndex, annotationTransformations);
            case APP_ARCHIVE_CONFIG:
                return new AppArchiveConfigImpl(beanArchiveIndex, annotationTransformations);
            case TYPES:
                return new TypesImpl(beanArchiveIndex, annotationOverlays);

            default:
                // TODO should report an error here
                return null;
        }
    }
}
