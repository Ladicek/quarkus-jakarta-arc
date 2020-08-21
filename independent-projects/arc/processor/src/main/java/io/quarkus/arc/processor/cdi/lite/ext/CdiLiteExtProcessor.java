package io.quarkus.arc.processor.cdi.lite.ext;

import io.quarkus.arc.processor.BeanProcessor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jboss.jandex.DotName;

public class CdiLiteExtProcessor {
    private final org.jboss.jandex.IndexView index;
    private final BeanProcessor.Builder builder;

    public CdiLiteExtProcessor(org.jboss.jandex.IndexView index, BeanProcessor.Builder builder) {
        this.index = index;
        this.builder = builder;
    }

    public void run() {
        try {
            doRun();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void doRun() throws ReflectiveOperationException {
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

        ClassAnnotationTransformations classTransformations = new ClassAnnotationTransformations();
        builder.addAnnotationTransformer(classTransformations);

        MethodAnnotationTransformations methodTransformations = new MethodAnnotationTransformations();
        builder.addAnnotationTransformer(methodTransformations);

        FieldAnnotationTransformations fieldTransformations = new FieldAnnotationTransformations();
        builder.addAnnotationTransformer(fieldTransformations);

        List<Object> arguments = new ArrayList<>();
        for (org.jboss.jandex.Type parameterType : method.parameters()) {
            ExtensionMethodParameterType kind = ExtensionMethodParameterType.of(parameterType);
            Collection<org.jboss.jandex.ClassInfo> matchingClasses = matchingClassesForExtensionMethodParameter(kind,
                    parameterType);

            switch (kind) {
                case CLASS_INFO:
                    if (matchingClasses.size() == 1) {
                        arguments.add(new ClassInfoImpl(index, matchingClasses.iterator().next()));
                    } else {
                        // TODO should report an error here
                        arguments.add(null);
                    }
                    break;
                case CLASS_CONFIG:
                    if (matchingClasses.size() == 1) {
                        arguments.add(new ClassConfigImpl(index, matchingClasses.iterator().next(), classTransformations));
                    } else {
                        // TODO should report an error here
                        arguments.add(null);
                    }
                    break;

                case COLLECTION_CLASS_INFO:
                    arguments.add(matchingClasses.stream()
                            .map(it -> new ClassInfoImpl(index, it))
                            .collect(Collectors.toList()));
                    break;
                case COLLECTION_METHOD_INFO:
                    arguments.add(matchingClasses.stream()
                            .flatMap(it -> it.methods().stream())
                            .filter(MethodPredicates.IS_METHOD_OR_CONSTRUCTOR_JANDEX)
                            .map(it -> new MethodInfoImpl(index, it))
                            .collect(Collectors.toList()));
                    break;
                case COLLECTION_PARAMETER_INFO:
                    List<ParameterInfoImpl> parameterInfos = new ArrayList<>();
                    matchingClasses.stream()
                            .flatMap(it -> it.methods().stream())
                            .forEach(it -> {
                                for (int i = 0; i < it.parameters().size(); i++) {
                                    parameterInfos.add(new ParameterInfoImpl(index, it, i));
                                }
                            });
                    arguments.add(parameterInfos);
                    break;
                case COLLECTION_FIELD_INFO:
                    arguments.add(matchingClasses.stream()
                            .flatMap(it -> it.fields().stream())
                            .map(it -> new FieldInfoImpl(index, it))
                            .collect(Collectors.toList()));
                    break;

                case COLLECTION_CLASS_CONFIG:
                    arguments.add(matchingClasses.stream()
                            .map(it -> new ClassConfigImpl(index, it, classTransformations))
                            .collect(Collectors.toList()));
                    break;
                case COLLECTION_METHOD_CONFIG:
                    arguments.add(matchingClasses.stream()
                            .flatMap(it -> it.methods().stream())
                            .filter(MethodPredicates.IS_METHOD_OR_CONSTRUCTOR_JANDEX)
                            .map(it -> new MethodConfigImpl(index, it, methodTransformations))
                            .collect(Collectors.toList()));
                    break;
                case COLLECTION_FIELD_CONFIG:
                    arguments.add(matchingClasses.stream()
                            .flatMap(it -> it.fields().stream())
                            .map(it -> new FieldConfigImpl(index, it, fieldTransformations))
                            .collect(Collectors.toList()));
                    break;

                case TYPES:
                    arguments.add(new TypesImpl(index));

                case WORLD:
                    arguments.add(new WorldImpl(index));
                    break;

                default:
                    // TODO should report an error here
                    arguments.add(null);
                    break;
            }
        }

        callExtensionMethod(method, arguments);
    }

    private enum ExtensionMethodParameterType {
        CLASS_INFO(true),

        COLLECTION_CLASS_INFO(false),
        COLLECTION_METHOD_INFO(false),
        COLLECTION_PARAMETER_INFO(false),
        COLLECTION_FIELD_INFO(false),

        CLASS_CONFIG(true),

        COLLECTION_CLASS_CONFIG(false),
        COLLECTION_METHOD_CONFIG(false),
        COLLECTION_PARAMETER_CONFIG(false),
        COLLECTION_FIELD_CONFIG(false),

        TYPES(true),
        WORLD(true),

        UNKNOWN(true), // the singular parameter doesn't make any sense in this case
        ;

        final boolean singular;

        ExtensionMethodParameterType(boolean singular) {
            this.singular = singular;
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
                if (type.name().equals(DotNames.TYPES)) {
                    return TYPES;
                } else if (type.name().equals(DotNames.WORLD)) {
                    return WORLD;
                }
            }

            return UNKNOWN;
        }
    }

    private Collection<org.jboss.jandex.ClassInfo> matchingClassesForExtensionMethodParameter(ExtensionMethodParameterType kind,
            org.jboss.jandex.Type parameter) {

        if (kind == ExtensionMethodParameterType.WORLD) {
            return Collections.emptySet();
        }

        Collection<org.jboss.jandex.ClassInfo> result;

        org.jboss.jandex.Type queryHolder;
        if (kind.singular) {
            queryHolder = parameter;
        } else {
            queryHolder = parameter.asParameterizedType().arguments().get(0);
        }
        org.jboss.jandex.Type query = queryHolder.asParameterizedType().arguments().get(0);

        if (query.kind() == org.jboss.jandex.Type.Kind.WILDCARD_TYPE) {
            org.jboss.jandex.Type lowerBound = query.asWildcardType().superBound();
            if (lowerBound != null) {
                result = new ArrayList<>();
                DotName name = lowerBound.name();
                while (name != null) {
                    org.jboss.jandex.ClassInfo clazz = index.getClassByName(name);
                    if (clazz != null) {
                        result.add(clazz);
                        name = clazz.superName();
                    } else {
                        // should report an error here
                        name = null;
                    }
                }
            } else {
                org.jboss.jandex.Type upperBound = query.asWildcardType().extendsBound();
                org.jboss.jandex.ClassInfo clazz = index.getClassByName(upperBound.name());
                // if clazz is null, should report an error here
                result = Modifier.isInterface(clazz.flags())
                        ? index.getAllKnownImplementors(upperBound.name())
                        : index.getAllKnownSubclasses(upperBound.name());
                // TODO index.getAllKnown* is not reflexive; should add the original type ourselves?
                //  we do that for lower bound currently (see above)
            }
        } else if (query.kind() == org.jboss.jandex.Type.Kind.CLASS) {
            result = Collections.singleton(index.getClassByName(query.asClassType().name()));
        } else {
            // TODO should report an error here (well, perhaps there are other valid cases, e.g. arrays?)
            result = Collections.emptySet();
        }

        return result;
    }

    // ---
    // the following methods use reflection, everything else is reflection-free

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

    private void callExtensionMethod(org.jboss.jandex.MethodInfo method, List<Object> arguments)
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
            } else if (cdi.lite.extension.Types.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = cdi.lite.extension.Types.class;
            } else if (cdi.lite.extension.World.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = cdi.lite.extension.World.class;
            } else {
                // should never happen, internal error (or missing error handling) if it does
                throw new IllegalArgumentException("Unexpected extension method argument: " + argument);
            }
        }

        Class<?> extensionClass = getExtensionClass(method.declaringClass().name().toString());
        Object extensionClassInstance = getExtensionClassInstance(extensionClass);

        Method methodReflective = extensionClass.getMethod(method.name(), parameterTypes);
        methodReflective.invoke(extensionClassInstance, arguments.toArray());
    }
}
