package io.quarkus.arc.processor.cdi.lite.ext;

import io.quarkus.arc.processor.BeanProcessor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jboss.jandex.DotName;

public class CdiLiteExtProcessor {
    private final org.jboss.jandex.IndexView index;
    private final BeanProcessor.Builder builder;
    private final AllAnnotationTransformations allAnnotationTransformations;

    public CdiLiteExtProcessor(org.jboss.jandex.IndexView index, BeanProcessor.Builder builder) {
        this.index = index;
        this.builder = builder;
        this.allAnnotationTransformations = new AllAnnotationTransformations();
    }

    public void run() {
        try {
            doRun();
        } catch (Exception e) {
            // TODO proper diagnostics system
            throw new RuntimeException(e);
        }
    }

    private void doRun() throws ReflectiveOperationException {
        allAnnotationTransformations.register(builder);

        for (org.jboss.jandex.AnnotationInstance annotation : index.getAnnotations(DotNames.LITE_EXTENSION)) {
            org.jboss.jandex.MethodInfo method = annotation.target().asMethod();
            processExtensionMethod(method);
        }
    }

    private void processExtensionMethod(org.jboss.jandex.MethodInfo method) throws ReflectiveOperationException {
        // TODO
        //  - changes performed through the API should be visible in subsequent usages of the API
        //    (this is non-trivial to define, so ignoring that concern for now)
        //  - diagnostics

        List<Object> arguments = new ArrayList<>();
        int numParameters = method.parameters().size();
        for (int i = 0; i < numParameters; i++) {
            org.jboss.jandex.Type parameterType = method.parameters().get(i);
            ExtensionMethodParameterType kind = ExtensionMethodParameterType.of(parameterType);

            Set<DotName> requiredAnnotations = requiredAnnotationsForExtensionMethodParameter(method, i);

            Collection<org.jboss.jandex.ClassInfo> matchingClasses = matchingClassesForExtensionMethodParameter(kind,
                    parameterType, kind.isClassQuery() ? requiredAnnotations : null);

            Object argument = createArgumentForExtensionMethodParameter(kind, requiredAnnotations, matchingClasses);

            arguments.add(argument);
        }

        callExtensionMethod(method, arguments);
    }

    private enum ExtensionMethodParameterType {
        CLASS_INFO,

        COLLECTION_CLASS_INFO,
        COLLECTION_METHOD_INFO,
        COLLECTION_PARAMETER_INFO,
        COLLECTION_FIELD_INFO,

        CLASS_CONFIG,

        COLLECTION_CLASS_CONFIG,
        COLLECTION_METHOD_CONFIG,
        COLLECTION_PARAMETER_CONFIG,
        COLLECTION_FIELD_CONFIG,

        ANNOTATIONS,
        TYPES,
        WORLD,

        UNKNOWN,
        ;

        boolean isQuery() {
            return this != ANNOTATIONS
                    && this != TYPES
                    && this != WORLD
                    && this != UNKNOWN;
        }

        boolean isClassQuery() {
            return this == CLASS_INFO
                    || this == CLASS_CONFIG
                    || this == COLLECTION_CLASS_INFO
                    || this == COLLECTION_CLASS_CONFIG;
        }

        boolean isSingularQuery() {
            return this == CLASS_INFO
                    || this == CLASS_CONFIG;
        }

        static ExtensionMethodParameterType of(org.jboss.jandex.Type type) {
            if (type.kind() == org.jboss.jandex.Type.Kind.PARAMETERIZED_TYPE) {
                if (type.name().equals(DotNames.COLLECTION)) {
                    org.jboss.jandex.Type collectionElement = type.asParameterizedType().arguments().get(0);
                    if (collectionElement.kind() == org.jboss.jandex.Type.Kind.PARAMETERIZED_TYPE) {
                        if (collectionElement.name().equals(DotNames.CLASS_INFO)) {
                            return COLLECTION_CLASS_INFO;
                        } else if (collectionElement.name().equals(DotNames.METHOD_INFO)) {
                            return COLLECTION_METHOD_INFO;
                        } else if (collectionElement.name().equals(DotNames.PARAMETER_INFO)) {
                            return COLLECTION_PARAMETER_INFO;
                        } else if (collectionElement.name().equals(DotNames.FIELD_INFO)) {
                            return COLLECTION_FIELD_INFO;
                        } else if (collectionElement.name().equals(DotNames.CLASS_CONFIG)) {
                            return COLLECTION_CLASS_CONFIG;
                        } else if (collectionElement.name().equals(DotNames.METHOD_CONFIG)) {
                            return COLLECTION_METHOD_CONFIG;
                        } else if (collectionElement.name().equals(DotNames.FIELD_CONFIG)) {
                            return COLLECTION_FIELD_CONFIG;
                        }
                    }
                } else {
                    if (type.name().equals(DotNames.CLASS_INFO)) {
                        return CLASS_INFO;
                    } else if (type.name().equals(DotNames.CLASS_CONFIG)) {
                        return CLASS_CONFIG;
                    }
                }
            } else if (type.kind() == org.jboss.jandex.Type.Kind.CLASS) {
                if (type.name().equals(DotNames.ANNOTATIONS)) {
                    return ANNOTATIONS;
                } else if (type.name().equals(DotNames.TYPES)) {
                    return TYPES;
                } else if (type.name().equals(DotNames.WORLD)) {
                    return WORLD;
                }
            }

            return UNKNOWN;
        }
    }

    private Set<DotName> requiredAnnotationsForExtensionMethodParameter(org.jboss.jandex.MethodInfo jandexMethod,
            int parameterPosition) {
        Set<DotName> requiredAnnotations = null;

        Optional<org.jboss.jandex.AnnotationInstance> jandexAnnotation = jandexMethod.annotations(DotNames.WITH_ANNOTATIONS)
                .stream()
                .filter(it -> it.target().kind() == org.jboss.jandex.AnnotationTarget.Kind.METHOD_PARAMETER
                        && it.target().asMethodParameter().position() == parameterPosition)
                .findAny();

        if (jandexAnnotation.isPresent()) {
            org.jboss.jandex.AnnotationValue jandexAnnotationAttribute = jandexAnnotation.get().value();
            if (jandexAnnotationAttribute != null) {
                org.jboss.jandex.Type[] jandexTypes = jandexAnnotationAttribute.asClassArray();
                if (jandexTypes.length > 0) {
                    requiredAnnotations = Arrays.stream(jandexTypes)
                            .map(org.jboss.jandex.Type::asClassType)
                            .map(org.jboss.jandex.Type::name)
                            .collect(Collectors.toSet());
                }
            }
        }

        return requiredAnnotations;
    }

    // TODO the query implementations here (in matchingClassesForExtensionMethodParameter
    //  and createArgumentForExtensionMethodParameter) duplicate WorldImpl quite a bit!

    private Collection<org.jboss.jandex.ClassInfo> matchingClassesForExtensionMethodParameter(ExtensionMethodParameterType kind,
            org.jboss.jandex.Type jandexParameter, Set<DotName> requiredJandexAnnotations) {

        if (!kind.isQuery()) {
            return Collections.emptySet();
        }

        Collection<org.jboss.jandex.ClassInfo> result;

        org.jboss.jandex.Type queryHolder;
        if (kind.isSingularQuery()) {
            queryHolder = jandexParameter;
        } else {
            queryHolder = jandexParameter.asParameterizedType().arguments().get(0);
        }
        org.jboss.jandex.Type query = queryHolder.asParameterizedType().arguments().get(0);

        if (query.kind() == org.jboss.jandex.Type.Kind.WILDCARD_TYPE) {
            org.jboss.jandex.Type lowerBound = query.asWildcardType().superBound();
            org.jboss.jandex.Type upperBound = query.asWildcardType().extendsBound();
            if (lowerBound != null) {
                result = new ArrayList<>();
                DotName name = lowerBound.name();
                while (name != null) {
                    org.jboss.jandex.ClassInfo clazz = index.getClassByName(name);
                    if (clazz != null) {
                        if (hasRequiredAnnotations(clazz, requiredJandexAnnotations)) {
                            result.add(clazz);
                        }
                        name = clazz.superName();
                    } else {
                        // TODO should report an error here
                        name = null;
                    }
                }
            } else if (upperBound.name().equals(DotNames.OBJECT) && requiredJandexAnnotations != null) {
                List<org.jboss.jandex.ClassInfo> resultFinal = new ArrayList<>();
                Set<DotName> alreadyAdded = new HashSet<>();
                for (DotName requiredJandexAnnotation : requiredJandexAnnotations) {
                    index.getAnnotations(requiredJandexAnnotation)
                            .stream()
                            .filter(it -> it.target().kind() == org.jboss.jandex.AnnotationTarget.Kind.CLASS)
                            .map(it -> it.target().asClass())
                            .forEach(it -> {
                                if (!alreadyAdded.contains(it.name())) {
                                    alreadyAdded.add(it.name());
                                    resultFinal.add(it);
                                }
                            });

                }
                result = resultFinal;
            } else {
                org.jboss.jandex.ClassInfo clazz = index.getClassByName(upperBound.name());
                // if clazz is null, should report an error here
                result = Modifier.isInterface(clazz.flags())
                        ? index.getAllKnownImplementors(upperBound.name())
                        : index.getAllKnownSubclasses(upperBound.name());
                // TODO index.getAllKnown* is not reflexive; should add the original type ourselves?
                //  we do that for lower bound currently (see above)

                if (requiredJandexAnnotations != null) {
                    result = result.stream()
                            .filter(it -> hasRequiredAnnotations(it, requiredJandexAnnotations))
                            .collect(Collectors.toList());
                }
            }
        } else if (query.kind() == org.jboss.jandex.Type.Kind.CLASS) {
            result = Collections.singleton(index.getClassByName(query.asClassType().name()));
        } else {
            // TODO should report an error here (well, perhaps there are other valid cases, e.g. arrays?)
            result = Collections.emptySet();
        }

        return result;
    }

    private Object createArgumentForExtensionMethodParameter(ExtensionMethodParameterType kind,
            Set<DotName> requiredAnnotations, Collection<org.jboss.jandex.ClassInfo> matchingClasses) {
        switch (kind) {
            case CLASS_INFO:
                if (matchingClasses.size() == 1) {
                    return new ClassInfoImpl(index, matchingClasses.iterator().next());
                } else {
                    // TODO should report an error here
                    return null;
                }

            case CLASS_CONFIG:
                if (matchingClasses.size() == 1) {
                    return new ClassConfigImpl(index, matchingClasses.iterator().next(),
                            allAnnotationTransformations.classes);
                } else {
                    // TODO should report an error here
                    return null;
                }

            case COLLECTION_CLASS_INFO:
                return matchingClasses.stream()
                        .map(it -> new ClassInfoImpl(index, it))
                        .collect(Collectors.toList());
            case COLLECTION_METHOD_INFO:
                return matchingClasses.stream()
                        .flatMap(it -> it.methods().stream())
                        .filter(MethodPredicates.IS_METHOD_OR_CONSTRUCTOR_JANDEX)
                        .filter(it -> hasRequiredAnnotations(it, requiredAnnotations))
                        .map(it -> new MethodInfoImpl(index, it))
                        .collect(Collectors.toList());
            case COLLECTION_PARAMETER_INFO:
                List<ParameterInfoImpl> parameterInfos = new ArrayList<>();
                matchingClasses.stream()
                        .flatMap(it -> it.methods().stream())
                        .forEach(it -> {
                            int numParameters = it.parameters().size();
                            for (int i = 0; i < numParameters; i++) {
                                if (hasRequiredAnnotations(org.jboss.jandex.MethodParameterInfo.create(it, (short) i),
                                        requiredAnnotations)) {
                                    parameterInfos.add(new ParameterInfoImpl(index, it, i));
                                }
                            }
                        });
                return parameterInfos;
            case COLLECTION_FIELD_INFO:
                return matchingClasses.stream()
                        .flatMap(it -> it.fields().stream())
                        .filter(it -> hasRequiredAnnotations(it, requiredAnnotations))
                        .map(it -> new FieldInfoImpl(index, it))
                        .collect(Collectors.toList());

            case COLLECTION_CLASS_CONFIG:
                return matchingClasses.stream()
                        .map(it -> new ClassConfigImpl(index, it, allAnnotationTransformations.classes))
                        .collect(Collectors.toList());
            case COLLECTION_METHOD_CONFIG:
                return matchingClasses.stream()
                        .flatMap(it -> it.methods().stream())
                        .filter(MethodPredicates.IS_METHOD_OR_CONSTRUCTOR_JANDEX)
                        .filter(it -> hasRequiredAnnotations(it, requiredAnnotations))
                        .map(it -> new MethodConfigImpl(index, it, allAnnotationTransformations.methods))
                        .collect(Collectors.toList());
            case COLLECTION_FIELD_CONFIG:
                return matchingClasses.stream()
                        .flatMap(it -> it.fields().stream())
                        .filter(it -> hasRequiredAnnotations(it, requiredAnnotations))
                        .map(it -> new FieldConfigImpl(index, it, allAnnotationTransformations.fields))
                        .collect(Collectors.toList());

            case ANNOTATIONS:
                return new AnnotationsImpl(index);
            case TYPES:
                return new TypesImpl(index);
            case WORLD:
                return new WorldImpl(index, allAnnotationTransformations);

            default:
                // TODO should report an error here
                return null;
        }
    }

    private static boolean hasRequiredAnnotations(org.jboss.jandex.ClassInfo jandexClass,
            Set<DotName> requiredJandexAnnotations) {
        return areAnnotationsPresent(jandexClass.classAnnotations().stream(), requiredJandexAnnotations);
    }

    private static boolean hasRequiredAnnotations(org.jboss.jandex.MethodInfo jandexMethod,
            Set<DotName> requiredJandexAnnotations) {
        Stream<org.jboss.jandex.AnnotationInstance> jandexAnnotations = jandexMethod.annotations()
                .stream()
                .filter(it -> it.target().kind() == org.jboss.jandex.AnnotationTarget.Kind.METHOD);

        return areAnnotationsPresent(jandexAnnotations, requiredJandexAnnotations);
    }

    private static boolean hasRequiredAnnotations(org.jboss.jandex.FieldInfo jandexField,
            Set<DotName> requiredJandexAnnotations) {
        return areAnnotationsPresent(jandexField.annotations().stream(), requiredJandexAnnotations);
    }

    private static boolean hasRequiredAnnotations(org.jboss.jandex.MethodParameterInfo jandexParameter,
            Set<DotName> requiredJandexAnnotations) {
        Stream<org.jboss.jandex.AnnotationInstance> jandexAnnotations = jandexParameter.method()
                .annotations()
                .stream()
                .filter(it -> it.target().kind() == org.jboss.jandex.AnnotationTarget.Kind.METHOD_PARAMETER
                        && it.target().asMethodParameter().position() == jandexParameter.position());
        return areAnnotationsPresent(jandexAnnotations, requiredJandexAnnotations);
    }

    private static boolean areAnnotationsPresent(Stream<org.jboss.jandex.AnnotationInstance> presentAnnotations,
            Set<DotName> expectedAnnotations) {
        if (expectedAnnotations == null || expectedAnnotations.isEmpty()) {
            return true;
        }

        return presentAnnotations
                .map(org.jboss.jandex.AnnotationInstance::name)
                .anyMatch(expectedAnnotations::contains);
    }

    // ---
    // the following methods use reflection, everything else in this class is reflection-free

    private final Map<String, Class<?>> extensionClasses = new HashMap<>();
    private final Map<Class<?>, Object> extensionClassInstances = new HashMap<>();

    private Class<?> getExtensionClass(String className) {
        return extensionClasses.computeIfAbsent(className, ignored -> {
            try {
                return Class.forName(className, true, Thread.currentThread().getContextClassLoader());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private Object getExtensionClassInstance(Class<?> clazz) {
        return extensionClassInstances.computeIfAbsent(clazz, ignored -> {
            try {
                return clazz.newInstance();
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void callExtensionMethod(org.jboss.jandex.MethodInfo jandexMethod, List<Object> arguments)
            throws ReflectiveOperationException {

        Class<?>[] parameterTypes = new Class[arguments.size()];

        for (int i = 0; i < parameterTypes.length; i++) {
            Object argument = arguments.get(i);
            Class<?> argumentClass = argument.getClass();

            if (Collection.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = Collection.class;
            } else if (cdi.lite.extension.model.configs.ClassConfig.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = cdi.lite.extension.model.configs.ClassConfig.class;
            } else if (cdi.lite.extension.model.declarations.ClassInfo.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = cdi.lite.extension.model.declarations.ClassInfo.class;
            } else if (cdi.lite.extension.Annotations.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = cdi.lite.extension.Annotations.class;
            } else if (cdi.lite.extension.Types.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = cdi.lite.extension.Types.class;
            } else if (cdi.lite.extension.World.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = cdi.lite.extension.World.class;
            } else {
                // should never happen, internal error (or missing error handling) if it does
                throw new IllegalArgumentException("Unexpected extension method argument: " + argument);
            }
        }

        Class<?> extensionClass = getExtensionClass(jandexMethod.declaringClass().name().toString());
        Object extensionClassInstance = getExtensionClassInstance(extensionClass);

        Method methodReflective = extensionClass.getMethod(jandexMethod.name(), parameterTypes);
        methodReflective.invoke(extensionClassInstance, arguments.toArray());
    }
}
