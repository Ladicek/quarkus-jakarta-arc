package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.model.configs.ClassConfig;
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
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

public class CdiLiteExtProcessor {
    private final IndexView index;
    private final BeanProcessor.Builder builder;

    public CdiLiteExtProcessor(IndexView index, BeanProcessor.Builder builder) {
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
        for (AnnotationInstance annotation : index.getAnnotations(DotNames.LITE_EXTENSION)) {
            MethodInfo method = annotation.target().asMethod();
            processExtensionMethod(method);
        }
    }

    private void processExtensionMethod(MethodInfo method) throws ReflectiveOperationException {
        // TODO
        //  - changes performed through the API should be visible in subsequent usages of the API
        //    (this is non-trivial to define, so ignoring that concern for now)
        //  - diagnostics

        List<Object> arguments = new ArrayList<>();
        for (Type parameterType : method.parameters()) {
            ExtensionMethodParameterKind kind = ExtensionMethodParameterKind.of(parameterType);
            Collection<ClassInfo> matchingClasses = matchingClassesForExtensionMethodParameter(kind, parameterType);

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
                        arguments.add(new ClassConfigImpl(index, matchingClasses.iterator().next(), builder));
                    } else {
                        // TODO should report an error here
                        arguments.add(null);
                    }
                    break;

                // TODO other singular *Info and *Config

                case COLLECTION_CLASS_INFO:
                    arguments.add(matchingClasses.stream()
                            .map(it -> new ClassInfoImpl(index, it))
                            .collect(Collectors.toList()));
                    break;
                case COLLECTION_METHOD_INFO:
                    arguments.add(matchingClasses.stream()
                            .flatMap(it -> it.methods().stream())
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
                            .map(it -> new ClassConfigImpl(index, it, builder))
                            .collect(Collectors.toList()));
                    break;

                // TODO other Collection<*Config>

                default:
                    // TODO should report an error here
                    arguments.add(null);
                    break;
            }
        }

        callExtensionMethod(method, arguments);
    }

    private enum ExtensionMethodParameterKind {
        PACKAGE_INFO(true),
        CLASS_INFO(true),
        METHOD_INFO(true),
        PARAMETER_INFO(true),
        FIELD_INFO(true),

        COLLECTION_PACKAGE_INFO(false),
        COLLECTION_CLASS_INFO(false),
        COLLECTION_METHOD_INFO(false),
        COLLECTION_PARAMETER_INFO(false),
        COLLECTION_FIELD_INFO(false),

        CLASS_CONFIG(true),
        METHOD_CONFIG(true),
        PARAMETER_CONFIG(true),
        FIELD_CONFIG(true),

        COLLECTION_CLASS_CONFIG(false),
        COLLECTION_METHOD_CONFIG(false),
        COLLECTION_PARAMETER_CONFIG(false),
        COLLECTION_FIELD_CONFIG(false),

        UNKNOWN(false),
        ;

        final boolean singular;

        ExtensionMethodParameterKind(boolean singular) {
            this.singular = singular;
        }

        static ExtensionMethodParameterKind of(Type parameter) {
            if (parameter.kind() == Type.Kind.PARAMETERIZED_TYPE) {
                if (parameter.name().equals(DotNames.COLLECTION)) {
                    Type collectionElement = parameter.asParameterizedType().arguments().get(0);
                    if (collectionElement.kind() == Type.Kind.PARAMETERIZED_TYPE) {
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
                        }
                    }
                } else {
                    if (parameter.name().equals(DotNames.CLASS_INFO)) {
                        return CLASS_INFO;
                    } else if (parameter.name().equals(DotNames.METHOD_INFO)) {
                        return METHOD_INFO;
                    } else if (parameter.name().equals(DotNames.PARAMETER_INFO)) {
                        return PARAMETER_INFO;
                    } else if (parameter.name().equals(DotNames.FIELD_INFO)) {
                        return FIELD_INFO;
                    } else if (parameter.name().equals(DotNames.CLASS_CONFIG)) {
                        return CLASS_CONFIG;
                    }
                }
            }

            return UNKNOWN;
        }
    }

    private Collection<ClassInfo> matchingClassesForExtensionMethodParameter(ExtensionMethodParameterKind kind,
            Type parameter) {
        Collection<ClassInfo> result;

        Type queryHolder;
        if (kind.singular) {
            queryHolder = parameter;
        } else {
            queryHolder = parameter.asParameterizedType().arguments().get(0);
        }
        Type query = queryHolder.asParameterizedType().arguments().get(0);

        if (query.kind() == Type.Kind.WILDCARD_TYPE) {
            Type lowerBound = query.asWildcardType().superBound();
            if (lowerBound != null) {
                result = new ArrayList<>();
                DotName name = lowerBound.name();
                while (name != null) {
                    ClassInfo clazz = index.getClassByName(name);
                    if (clazz != null) {
                        result.add(clazz);
                        name = clazz.superName();
                    } else {
                        // should report an error here
                        name = null;
                    }
                }
            } else {
                Type upperBound = query.asWildcardType().extendsBound();
                ClassInfo clazz = index.getClassByName(upperBound.name());
                // if clazz is null, should report an error here
                result = Modifier.isInterface(clazz.flags())
                        ? index.getAllKnownImplementors(upperBound.name())
                        : index.getAllKnownSubclasses(upperBound.name());
                // TODO index.getAllKnown* is not reflexive; should add the original type ourselves?
                //  we do that for lower bound currently (see above)
            }
        } else if (query.kind() == Type.Kind.CLASS) {
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

    private void callExtensionMethod(MethodInfo method, List<Object> arguments) throws ReflectiveOperationException {
        Class<?>[] parameterTypes = new Class[arguments.size()];
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> argumentClass = arguments.get(i).getClass();
            if (Collection.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = Collection.class;
            } else if (ClassConfig.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = ClassConfig.class;
            } else if (cdi.lite.extension.model.declarations.ClassInfo.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = cdi.lite.extension.model.declarations.ClassInfo.class;
            } else if (cdi.lite.extension.model.declarations.MethodInfo.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = cdi.lite.extension.model.declarations.ClassInfo.class;
            } else if (cdi.lite.extension.model.declarations.ParameterInfo.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = cdi.lite.extension.model.declarations.ClassInfo.class;
            } else if (cdi.lite.extension.model.declarations.FieldInfo.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = cdi.lite.extension.model.declarations.ClassInfo.class;
            } else {
                // should never happen at this point
                parameterTypes[i] = null;
            }
        }

        Class<?> extensionClass = getExtensionClass(method.declaringClass().name().toString());
        Object extensionClassInstance = getExtensionClassInstance(extensionClass);

        Method methodReflective = extensionClass.getMethod(method.name(), parameterTypes);
        methodReflective.invoke(extensionClassInstance, arguments.toArray());
    }
}
