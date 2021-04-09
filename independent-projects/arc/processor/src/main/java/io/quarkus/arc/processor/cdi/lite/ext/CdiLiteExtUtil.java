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
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.Type;

// TODO better name!
class CdiLiteExtUtil {
    private final Map<String, Class<?>> extensionClasses = new HashMap<>();
    private final Map<Class<?>, Object> extensionClassInstances = new HashMap<>();

    private final IndexView extensionsIndex;

    public CdiLiteExtUtil() {
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
        extensionsIndex = extensionsIndexer.complete();
    }

    List<org.jboss.jandex.MethodInfo> findExtensionMethods(DotName annotation) {
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

                    int p1 = getExtensionMethodPriority(m1);
                    int p2 = getExtensionMethodPriority(m2);

                    // must _not_ return 0 if priorities are equal, because that isn't consistent
                    // with the `equals` method (see also above)
                    return p1 < p2 ? -1 : 1;
                })
                .collect(Collectors.toList());
    }

    private int getExtensionMethodPriority(org.jboss.jandex.MethodInfo method) {
        // the annotation can only be put on methods, so no need to filter out parameter annotations etc.
        org.jboss.jandex.AnnotationInstance priority = method.annotation(DotNames.EXTENSION_PRIORITY);
        if (priority != null) {
            return priority.value().asInt();
        }
        return 10_000;
    }

    // ---

    enum Phase {
        DISCOVERY,
        ENHANCEMENT,
        PROCESSING,
        SYNTHESIS,
        VALIDATION
    }

    enum ExtensionMethodParameterType {
        CLASS_CONFIG(Phase.ENHANCEMENT),
        METHOD_CONFIG(Phase.ENHANCEMENT),
        FIELD_CONFIG(Phase.ENHANCEMENT),

        BEAN_INFO(Phase.PROCESSING),
        OBSERVER_INFO(Phase.PROCESSING),

        ANNOTATIONS(Phase.ENHANCEMENT),
        APP_ARCHIVE(Phase.ENHANCEMENT, Phase.SYNTHESIS, Phase.VALIDATION), // TODO remove @Enhancement?
        APP_ARCHIVE_BUILDER(Phase.DISCOVERY),
        APP_ARCHIVE_CONFIG(Phase.ENHANCEMENT),
        APP_DEPLOYMENT(Phase.SYNTHESIS, Phase.VALIDATION),
        MESSAGES(Phase.DISCOVERY, Phase.ENHANCEMENT, Phase.PROCESSING, Phase.SYNTHESIS, Phase.VALIDATION),
        META_ANNOTATIONS(Phase.DISCOVERY),
        SYNTHETIC_COMPONENTS(Phase.SYNTHESIS),
        TYPES(Phase.ENHANCEMENT, Phase.PROCESSING, Phase.SYNTHESIS, Phase.VALIDATION),

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
                    || this == FIELD_CONFIG
                    || this == BEAN_INFO
                    || this == OBSERVER_INFO;
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
                } else if (type.name().equals(DotNames.BEAN_INFO)) {
                    return BEAN_INFO;
                } else if (type.name().equals(DotNames.OBSERVER_INFO)) {
                    return OBSERVER_INFO;
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
                } else if (type.name().equals(DotNames.MESSAGES)) {
                    return MESSAGES;
                } else if (type.name().equals(DotNames.META_ANNOTATIONS)) {
                    return META_ANNOTATIONS;
                } else if (type.name().equals(DotNames.SYNTHETIC_COMPONENTS)) {
                    return SYNTHETIC_COMPONENTS;
                } else if (type.name().equals(DotNames.TYPES)) {
                    return TYPES;
                }
            }

            if (type.kind() == Type.Kind.PARAMETERIZED_TYPE) {
                // for now, let's also accept {Class,Method,Field}Config<?> and {Bean,Observer}Info<?>
                // this will later be removed, if {Class,Method,Field}Config and {Bean,Observer}Info stop being parameterized,
                // or will be replaced with something more complex, if we return back to expressing queries using
                // type parameter bounds
                List<Type> typeArguments = type.asParameterizedType().arguments();
                if (typeArguments.size() == 1
                        && typeArguments.get(0).kind() == Type.Kind.WILDCARD_TYPE
                        && typeArguments.get(0).asWildcardType().superBound() == null
                        && typeArguments.get(0).asWildcardType().extendsBound().name().equals(DotNames.OBJECT)) {
                    if (type.name().equals(DotNames.CLASS_CONFIG)) {
                        return CLASS_CONFIG;
                    } else if (type.name().equals(DotNames.METHOD_CONFIG)) {
                        return METHOD_CONFIG;
                    } else if (type.name().equals(DotNames.FIELD_CONFIG)) {
                        return FIELD_CONFIG;
                    } else if (type.name().equals(DotNames.BEAN_INFO)) {
                        return BEAN_INFO;
                    } else if (type.name().equals(DotNames.OBSERVER_INFO)) {
                        return OBSERVER_INFO;
                    }
                }
            }

            return UNKNOWN;
        }
    }

    // ---
    // the following methods use reflection, everything else in the CdiLiteExt processors is reflection-free

    void callExtensionMethod(org.jboss.jandex.MethodInfo jandexMethod, List<Object> arguments)
            throws ReflectiveOperationException {

        Class<?>[] parameterTypes = new Class[arguments.size()];

        for (int i = 0; i < parameterTypes.length; i++) {
            Object argument = arguments.get(i);
            Class<?> argumentClass = argument.getClass();

            // beware of ordering! subtypes must precede supertypes
            if (cdi.lite.extension.phases.discovery.AppArchiveBuilder.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = cdi.lite.extension.phases.discovery.AppArchiveBuilder.class;
            } else if (cdi.lite.extension.phases.discovery.MetaAnnotations.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = cdi.lite.extension.phases.discovery.MetaAnnotations.class;
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
            } else if (cdi.lite.extension.phases.synthesis.SyntheticComponents.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = cdi.lite.extension.phases.synthesis.SyntheticComponents.class;
            } else if (cdi.lite.extension.beans.BeanInfo.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = cdi.lite.extension.beans.BeanInfo.class;
            } else if (cdi.lite.extension.beans.ObserverInfo.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = cdi.lite.extension.beans.ObserverInfo.class;
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

        Class<?> extensionClass = extensionClasses.get(jandexMethod.declaringClass().name().toString());
        Object extensionClassInstance = extensionClassInstances.get(extensionClass);

        Method methodReflective = extensionClass.getDeclaredMethod(jandexMethod.name(), parameterTypes);
        methodReflective.setAccessible(true);
        methodReflective.invoke(extensionClassInstance, arguments.toArray());
    }
}
