package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.TypeConfigurator;
import io.quarkus.arc.processor.BeanProcessor;
import io.quarkus.arc.processor.DotNames;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
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
        //  - call all extension methods on a single instance of the declaring class
        //  - changes performed through the API should be visible in subsequent usages of the API
        //    (this is non-trivial to define, so ignoring that concern for now)
        //  - diagnostics

        List<Object> arguments = new ArrayList<>();
        for (Type type : method.parameters()) {
            Collection<ClassInfo> types = matchingTypesForExtensionMethodParameter(type);
            Collection<TypeConfigurator<?>> typeConfigurators = typeConfiguratorsForTypes(types);
            if (isSingular(type)) {
                if (typeConfigurators.size() == 1) {
                    arguments.add(typeConfigurators.iterator().next());
                } else {
                    // should report an error here
                    arguments.add(typeConfigurators);
                }
            } else {
                arguments.add(typeConfigurators);
            }
        }

        callExtensionMethod(method, arguments);
    }

    private boolean isSingular(Type parameter) {
        if (parameter.kind() == Type.Kind.PARAMETERIZED_TYPE) {
            if (DotNames.COLLECTION.equals(parameter.name())) {
                return false;
            } else if (DotNames.TYPE_CONFIGURATOR.equals(parameter.name())) {
                return true;
            }
        }

        // should report an error here
        return false;
    }

    private Collection<ClassInfo> matchingTypesForExtensionMethodParameter(Type parameter) {
        Collection<ClassInfo> matchingTypes;

        Type typeConfigurator;

        if (isSingular(parameter)) {
            typeConfigurator = parameter;
        } else {
            typeConfigurator = parameter.asParameterizedType().arguments().get(0);
        }
        Type query = typeConfigurator.asParameterizedType().arguments().get(0);
        if (query.kind() == Type.Kind.WILDCARD_TYPE) {
            Type lowerBound = query.asWildcardType().superBound();
            if (lowerBound != null) {
                matchingTypes = new ArrayList<>();
                DotName name = lowerBound.name();
                while (name != null) {
                    ClassInfo clazz = index.getClassByName(name);
                    if (clazz != null) {
                        matchingTypes.add(clazz);
                        name = clazz.superName();
                    } else {
                        // should report an error here
                        name = null;
                    }
                }
            } else {
                Type upperBound = query.asWildcardType().extendsBound();
                ClassInfo upperBoundClass = index.getClassByName(upperBound.name());
                // if upperBoundClass is null, should report an error here
                matchingTypes = Modifier.isInterface(upperBoundClass.flags())
                        ? index.getAllKnownImplementors(upperBound.name()) // TODO is this reflexive?
                        : index.getAllKnownSubclasses(upperBound.name()); // TODO is this reflexive?
            }
        } else if (query.kind() == Type.Kind.CLASS) {
            matchingTypes = Collections.singleton(index.getClassByName(query.asClassType().name()));
        } else {
            // should report an error here (well, perhaps there are other valid cases, e.g. arrays?)
            matchingTypes = Collections.emptySet();
        }

        return matchingTypes;
    }

    private Collection<TypeConfigurator<?>> typeConfiguratorsForTypes(Collection<ClassInfo> matchingTypes) {
        return matchingTypes.stream()
                .map(type -> new TypeConfigurator<Object>() {
                    @Override
                    public ClassInfo type() {
                        return type;
                    }

                    @Override
                    public void addAnnotation(Class<? extends Annotation> clazz, AnnotationValue... values) {
                        builder.addAnnotationTransformer(ctx -> {
                            if (ctx.getTarget().kind() == AnnotationTarget.Kind.CLASS
                                    && ctx.getTarget().asClass().name().equals(type.name())) {
                                ctx.transform().add(clazz, values).done();
                            }
                        });
                    }

                    @Override
                    public void removeAnnotation(Predicate<AnnotationInstance> predicate) {
                        builder.addAnnotationTransformer(ctx -> {
                            if (ctx.getTarget().kind() == AnnotationTarget.Kind.CLASS
                                    && ctx.getTarget().asClass().name().equals(type.name())) {
                                ctx.transform().remove(predicate).done();
                            }
                        });
                    }
                })
                .collect(Collectors.toSet());
    }

    private void callExtensionMethod(MethodInfo method, List<Object> arguments) throws ReflectiveOperationException {
        Class<?>[] parameterTypes = new Class[arguments.size()];
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> argumentClass = arguments.get(i).getClass();
            if (Collection.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = Collection.class;
            } else if (TypeConfigurator.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = TypeConfigurator.class;
            } else {
                // should never happen at this point
                parameterTypes[i] = null;
            }
        }

        Class<?> classReflective = Class.forName(method.declaringClass().name().toString());
        Method methodReflective = classReflective.getMethod(method.name(), parameterTypes);
        methodReflective.invoke(classReflective.newInstance(), arguments.toArray());
    }
}
