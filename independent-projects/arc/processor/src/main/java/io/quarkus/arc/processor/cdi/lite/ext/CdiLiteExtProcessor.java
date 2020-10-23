package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.phases.enhancement.ClassConfig;
import cdi.lite.extension.phases.enhancement.FieldConfig;
import cdi.lite.extension.phases.enhancement.MethodConfig;
import io.quarkus.arc.processor.BeanProcessor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;

public class CdiLiteExtProcessor {
    private final org.jboss.jandex.IndexView index;
    private final BeanProcessor.Builder builder;
    private final AllAnnotationOverlays annotationOverlays;
    private final AllAnnotationTransformations annotationTransformations;

    public CdiLiteExtProcessor(org.jboss.jandex.IndexView index, BeanProcessor.Builder builder) {
        this.index = index;
        this.builder = builder;
        this.annotationOverlays = new AllAnnotationOverlays();
        this.annotationTransformations = new AllAnnotationTransformations(index, annotationOverlays);
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
        // TODO use service loader to find and instantiate BuildCompatibleExtensions
        List<org.jboss.jandex.MethodInfo> extensionMethods = index.getAllKnownImplementors(DotNames.BUILD_COMPATIBLE_EXTENSION)
                .stream()
                .flatMap(it -> it.annotations()
                        .getOrDefault(DotNames.ENHANCEMENT, Collections.emptyList())
                        .stream()
                        .filter(ann -> ann.target().kind() == org.jboss.jandex.AnnotationTarget.Kind.METHOD)
                        .map(ann -> ann.target().asMethod()))
                .sorted((m1, m2) -> {
                    if (m1 == m2) {
                        // at this particular point, two different org.jboss.jandex.MethodInfo instances are never equal
                        return 0;
                    }

                    OptionalInt p1 = getExtensionMethodPriority(m1);
                    OptionalInt p2 = getExtensionMethodPriority(m2);

                    if (p1.isPresent() && p2.isPresent()) {
                        // must _not_ return 0 if priorities are equal, because that isn't consistent
                        // with the `equals` method (see also above)
                        return p1.getAsInt() < p2.getAsInt() ? 1 : -1;
                    } else if (p1.isPresent()) {
                        return -1;
                    } else if (p2.isPresent()) {
                        return 1;
                    } else {
                        // must _not_ return 0 if both methods are missing a priority, because that isn't consistent
                        // with the `equals` method (see also above)
                        return -1;
                    }
                })
                .collect(Collectors.toList());

        for (org.jboss.jandex.MethodInfo method : extensionMethods) {
            processExtensionMethod(method);
        }
    }

    private OptionalInt getExtensionMethodPriority(org.jboss.jandex.MethodInfo method) {
        // the annotation can only be put on methods, so no need to filter out parameter annotations etc.
        org.jboss.jandex.AnnotationInstance priority = method.annotation(DotNames.EXTENSION_PRIORITY);
        if (priority != null) {
            return OptionalInt.of(priority.value().asInt());
        }
        return OptionalInt.empty();
    }

    private void processExtensionMethod(org.jboss.jandex.MethodInfo method) throws ReflectiveOperationException {
        // TODO
        //  - diagnostics

        List<Object> arguments = new ArrayList<>();
        int numParameters = method.parameters().size();
        for (int i = 0; i < numParameters; i++) {
            org.jboss.jandex.Type parameterType = method.parameters().get(i);
            ExtensionMethodParameterType kind = ExtensionMethodParameterType.of(parameterType);

            org.jboss.jandex.AnnotationInstance constraintAnnotation = constraintAnnotationForExtensionMethodParameter(method, i);
            if (kind.isQuery() && constraintAnnotation == null) {
                throw new IllegalArgumentException("Missing constraint annotation (@ExactType, @SubtypesOf) for parameter "
                        + i + " of method " + method + " @ " + method.declaringClass());
            }

            Set<DotName> requiredAnnotations = requiredAnnotationsForExtensionMethodParameter(constraintAnnotation);

            Collection<org.jboss.jandex.ClassInfo> matchingClasses = matchingClassesForExtensionMethodParameter(kind,
                    constraintAnnotation, requiredAnnotations);

            Object argument = createArgumentForExtensionMethodParameter(kind, matchingClasses);

            arguments.add(argument);
        }

        callExtensionMethod(method, arguments);
    }

    private enum Phase {
        DISCOVERY,
        ENHANCEMENT,
        SYNTHESIS,
        VALIDATION
    }

    private enum ExtensionMethodParameterType {
        CLASS_ENTRYPOINT(Phase.ENHANCEMENT),
        METHOD_ENTRYPOINT(Phase.ENHANCEMENT),
        FIELD_ENTRYPOINT(Phase.ENHANCEMENT),

        ANNOTATIONS(Phase.ENHANCEMENT),
        APP_ARCHIVE(Phase.ENHANCEMENT, Phase.SYNTHESIS, Phase.VALIDATION),
        APP_ARCHIVE_BUILDER(Phase.DISCOVERY),
        APP_ARCHIVE_CONFIG(Phase.ENHANCEMENT),
        APP_DEPLOYMENT(Phase.SYNTHESIS, Phase.VALIDATION),
        CONTEXTS(Phase.DISCOVERY),
        MESSAGES(Phase.DISCOVERY, Phase.ENHANCEMENT, Phase.SYNTHESIS, Phase.VALIDATION),
        SYNTHETIC_COMPONENTS(Phase.SYNTHESIS),
        TYPES(Phase.ENHANCEMENT, Phase.SYNTHESIS, Phase.VALIDATION),

        UNKNOWN,
        ;

        private final Set<Phase> validPhases;

        ExtensionMethodParameterType(Phase... validPhases) {
            if (validPhases == null || validPhases.length == 0) {
                this.validPhases = EnumSet.noneOf(Phase.class);
            } else {
                this.validPhases = EnumSet.copyOf(Arrays.asList(validPhases));
            }
        }

        boolean isQuery() {
            return this == CLASS_ENTRYPOINT
                    || this == METHOD_ENTRYPOINT
                    || this == FIELD_ENTRYPOINT;
        }

        boolean isAvailableIn(Phase phase) {
            return validPhases.contains(phase);
        }

        static ExtensionMethodParameterType of(org.jboss.jandex.Type type) {
            if (type.kind() == org.jboss.jandex.Type.Kind.CLASS) {
                if (type.name().equals(DotNames.CLASS_ENTRYPOINT)) {
                    return CLASS_ENTRYPOINT;
                } else if (type.name().equals(DotNames.METHOD_ENTRYPOINT)) {
                    return METHOD_ENTRYPOINT;
                } else if (type.name().equals(DotNames.FIELD_ENTRYPOINT)) {
                    return FIELD_ENTRYPOINT;
                } else if (type.name().equals(DotNames.ANNOTATIONS)) {
                    return ANNOTATIONS;
                } else if (type.name().equals(DotNames.APP_ARCHIVE)) {
                    return APP_ARCHIVE;
                } else if (type.name().equals(DotNames.APP_ARCHIVE_BUILDER)) {
                    return APP_ARCHIVE_BUILDER;
                } else if (type.name().equals(DotNames.APP_ARCHIVE_CONFIG)) {
                    return APP_ARCHIVE_CONFIG;
                } else if (type.name().equals(DotNames.APP_DEPLOYMENT)) {
                    return APP_DEPLOYMENT;
                } else if (type.name().equals(DotNames.CONTEXTS)) {
                    return CONTEXTS;
                } else if (type.name().equals(DotNames.MESSAGES)) {
                    return MESSAGES;
                } else if (type.name().equals(DotNames.SYNTHETIC_COMPONENTS)) {
                    return SYNTHETIC_COMPONENTS;
                } else if (type.name().equals(DotNames.TYPES)) {
                    return TYPES;
                }
            }

            return UNKNOWN;
        }
    }

    private org.jboss.jandex.AnnotationInstance constraintAnnotationForExtensionMethodParameter(
            org.jboss.jandex.MethodInfo jandexMethod, int parameterPosition) {
        Stream<org.jboss.jandex.AnnotationInstance> exactTypeAnnotations = jandexMethod.annotations(DotNames.EXACT_TYPE).stream();
        Stream<org.jboss.jandex.AnnotationInstance> subtypesOfAnnotations = jandexMethod.annotations(DotNames.SUBTYPES_OF).stream();
        return Stream.concat(exactTypeAnnotations, subtypesOfAnnotations)
                .filter(it -> it.target().kind() == org.jboss.jandex.AnnotationTarget.Kind.METHOD_PARAMETER
                        && it.target().asMethodParameter().position() == parameterPosition)
                .findFirst()
                .orElse(null);
    }

    private Set<DotName> requiredAnnotationsForExtensionMethodParameter(org.jboss.jandex.AnnotationInstance jandexAnnotation) {
        if (jandexAnnotation == null) {
            return null;
        }

        org.jboss.jandex.AnnotationValue jandexAnnotationAttribute = jandexAnnotation.value("annotatedWith");
        if (jandexAnnotationAttribute != null) {
            org.jboss.jandex.Type[] jandexTypes = jandexAnnotationAttribute.asClassArray();
            if (jandexTypes.length > 0) {
                return Arrays.stream(jandexTypes)
                        .map(org.jboss.jandex.Type::asClassType)
                        .map(org.jboss.jandex.Type::name)
                        .collect(Collectors.toSet());
            }
        }

        return null;
    }

    private List<org.jboss.jandex.ClassInfo> matchingClassesForExtensionMethodParameter(ExtensionMethodParameterType kind,
            org.jboss.jandex.AnnotationInstance constraintJandexAnnotation, Set<DotName> requiredJandexAnnotations) {

        if (!kind.isQuery()) {
            return Collections.emptyList();
        }

        Collection<org.jboss.jandex.ClassInfo> result;

        if (DotNames.EXACT_TYPE.equals(constraintJandexAnnotation.name())) {
            org.jboss.jandex.Type jandexType = constraintJandexAnnotation.value("type").asClass();
            ClassInfo clazz = index.getClassByName(jandexType.name());
            // if clazz is null, should report an error here
            result = Collections.singletonList(clazz);
        } else if (DotNames.SUBTYPES_OF.equals(constraintJandexAnnotation.name())) {
            org.jboss.jandex.Type upperBound = constraintJandexAnnotation.value("type").asClass();
            org.jboss.jandex.ClassInfo clazz = index.getClassByName(upperBound.name());
            // if clazz is null, should report an error here
            result = Modifier.isInterface(clazz.flags())
                    ? index.getAllKnownImplementors(upperBound.name())
                    : index.getAllKnownSubclasses(upperBound.name());
            // TODO index.getAllKnown* is not reflexive; should add the original type ourselves?
        } else {
            // TODO error
            return Collections.emptyList();
        }

        if (requiredJandexAnnotations != null) {
            result = result.stream()
                    .filter(it -> it.annotations().keySet().stream().anyMatch(requiredJandexAnnotations::contains))
                    .collect(Collectors.toList());
        }

        return result instanceof List<?> ? (List<ClassInfo>) result : new ArrayList<>(result);
    }

    private Object createArgumentForExtensionMethodParameter(ExtensionMethodParameterType kind,
            Collection<org.jboss.jandex.ClassInfo> matchingClasses) {
        switch (kind) {
            case CLASS_ENTRYPOINT:
                List<ClassConfig<?>> classConfigs = matchingClasses.stream()
                        .map(it -> new ClassConfigImpl(index, annotationTransformations.classes, it))
                        .collect(Collectors.toList());
                return new ClassEntrypointImpl(classConfigs);
            case METHOD_ENTRYPOINT:
                List<MethodConfig<?>> methodConfigs = matchingClasses.stream()
                        .flatMap(it -> it.methods().stream())
                        .filter(MethodPredicates.IS_METHOD_OR_CONSTRUCTOR_JANDEX)
                        .map(it -> new MethodConfigImpl(index, annotationTransformations.methods, it))
                        .collect(Collectors.toList());
                return new MethodEntrypointImpl(methodConfigs);
            case FIELD_ENTRYPOINT:
                List<FieldConfig<?>> fieldConfigs = matchingClasses.stream()
                        .flatMap(it -> it.fields().stream())
                        .map(it -> new FieldConfigImpl(index, annotationTransformations.fields, it))
                        .collect(Collectors.toList());
                return new FieldEntrypointImpl(fieldConfigs);

            case ANNOTATIONS:
                return new AnnotationsImpl(index, annotationOverlays);
            case APP_ARCHIVE:
                return new AppArchiveImpl(index, annotationTransformations);
            case APP_ARCHIVE_CONFIG:
                return new AppArchiveConfigImpl(index, annotationTransformations);
            case TYPES:
                return new TypesImpl(index, annotationOverlays);

            default:
                // TODO should report an error here
                return null;
        }
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

            // beware of ordering! subtypes must precede supertypes
            if (cdi.lite.extension.phases.enhancement.ClassEntrypoint.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = cdi.lite.extension.phases.enhancement.ClassEntrypoint.class;
            } else if (cdi.lite.extension.phases.enhancement.MethodEntrypoint.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = cdi.lite.extension.phases.enhancement.MethodEntrypoint.class;
            } else if (cdi.lite.extension.phases.enhancement.FieldEntrypoint.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = cdi.lite.extension.phases.enhancement.FieldEntrypoint.class;
            } else if (cdi.lite.extension.phases.enhancement.Annotations.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = cdi.lite.extension.phases.enhancement.Annotations.class;
            } else if (cdi.lite.extension.phases.enhancement.AppArchiveConfig.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = cdi.lite.extension.phases.enhancement.AppArchiveConfig.class;
            } else if (cdi.lite.extension.AppArchive.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = cdi.lite.extension.AppArchive.class;
            } else if (cdi.lite.extension.Types.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = cdi.lite.extension.Types.class;
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
