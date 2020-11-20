package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.BuildCompatibleExtension;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;

// TODO better name!
class CdiLiteExtUtil {
    private final Map<String, Class<?>> extensionClasses = new HashMap<>();
    private final Map<Class<?>, Object> extensionClassInstances = new HashMap<>();

    List<org.jboss.jandex.MethodInfo> findExtensionMethods(DotName annotation) {
        // TODO this is silly and we should just use reflection,
        //  I only do this to reuse previously written code
        Indexer extensionsIndexer = new Indexer();
        for (BuildCompatibleExtension extension : ServiceLoader.load(BuildCompatibleExtension.class)) {
            Class<? extends BuildCompatibleExtension> extensionClass = extension.getClass();
            extensionClasses.put(extensionClass.getName(), extensionClass);
            extensionClassInstances.put(extensionClass, extension);
            try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(
                    extensionClass.getName().replace('.', '/') + ".class")) {
                extensionsIndexer.index(stream);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        Index extensionsIndex = extensionsIndexer.complete();

        return extensionsIndex.getAllKnownImplementors(DotNames.BUILD_COMPATIBLE_EXTENSION)
                .stream()
                .flatMap(it -> it.annotations()
                        .getOrDefault(annotation, Collections.emptyList())
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
    }

    private OptionalInt getExtensionMethodPriority(org.jboss.jandex.MethodInfo method) {
        // the annotation can only be put on methods, so no need to filter out parameter annotations etc.
        org.jboss.jandex.AnnotationInstance priority = method.annotation(DotNames.EXTENSION_PRIORITY);
        if (priority != null) {
            return OptionalInt.of(priority.value().asInt());
        }
        return OptionalInt.empty();
    }

    // ---

    enum Phase {
        DISCOVERY,
        ENHANCEMENT,
        SYNTHESIS,
        VALIDATION
    }

    enum ExtensionMethodParameterType {
        CLASS_CONFIG(Phase.ENHANCEMENT),
        METHOD_CONFIG(Phase.ENHANCEMENT),
        FIELD_CONFIG(Phase.ENHANCEMENT),

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
            return this == CLASS_CONFIG
                    || this == METHOD_CONFIG
                    || this == FIELD_CONFIG;
        }

        boolean isAvailableIn(Phase phase) {
            return validPhases.contains(phase);
        }

        static ExtensionMethodParameterType of(org.jboss.jandex.Type type) {
            if (type.kind() == org.jboss.jandex.Type.Kind.CLASS) {
                if (type.name().equals(DotNames.CLASS_CONFIG)) {
                    return CLASS_CONFIG;
                } else if (type.name().equals(DotNames.METHOD_CONFIG)) {
                    return METHOD_CONFIG;
                } else if (type.name().equals(DotNames.FIELD_CONFIG)) {
                    return FIELD_CONFIG;
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

    // ---

    // ---
    // the following methods use reflection, everything else in the CdiLiteExt processors is reflection-free

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

    void callExtensionMethod(org.jboss.jandex.MethodInfo jandexMethod, List<Object> arguments)
            throws ReflectiveOperationException {

        Class<?>[] parameterTypes = new Class[arguments.size()];

        for (int i = 0; i < parameterTypes.length; i++) {
            Object argument = arguments.get(i);
            Class<?> argumentClass = argument.getClass();

            // beware of ordering! subtypes must precede supertypes
            if (cdi.lite.extension.phases.discovery.AppArchiveBuilder.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = cdi.lite.extension.phases.discovery.AppArchiveBuilder.class;
            } else if (cdi.lite.extension.phases.enhancement.ClassConfig.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = cdi.lite.extension.phases.enhancement.ClassConfig.class;
            } else if (cdi.lite.extension.phases.enhancement.MethodConfig.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = cdi.lite.extension.phases.enhancement.MethodConfig.class;
            } else if (cdi.lite.extension.phases.enhancement.FieldConfig.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = cdi.lite.extension.phases.enhancement.FieldConfig.class;
            } else if (cdi.lite.extension.phases.enhancement.Annotations.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = cdi.lite.extension.phases.enhancement.Annotations.class;
            } else if (cdi.lite.extension.phases.enhancement.AppArchiveConfig.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = cdi.lite.extension.phases.enhancement.AppArchiveConfig.class;
            } else if (cdi.lite.extension.AppArchive.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = cdi.lite.extension.AppArchive.class;
            } else if (cdi.lite.extension.AppDeployment.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = cdi.lite.extension.AppDeployment.class;
            } else if (cdi.lite.extension.Messages.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = cdi.lite.extension.Messages.class;
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
