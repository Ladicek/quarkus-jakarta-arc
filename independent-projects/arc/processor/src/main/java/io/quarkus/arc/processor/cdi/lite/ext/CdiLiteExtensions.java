package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.model.declarations.MethodInfo;
import cdi.lite.extension.phases.enhancement.ClassConfig;
import cdi.lite.extension.phases.synthesis.SyntheticBeanCreator;
import cdi.lite.extension.phases.synthesis.SyntheticBeanDisposer;
import cdi.lite.extension.phases.synthesis.SyntheticObserver;
import io.quarkus.arc.InjectableContext;
import io.quarkus.arc.impl.InjectionPointProvider;
import io.quarkus.arc.processor.BeanConfigurator;
import io.quarkus.arc.processor.BeanDeploymentValidator;
import io.quarkus.arc.processor.BeanProcessor;
import io.quarkus.arc.processor.BeanRegistrar;
import io.quarkus.arc.processor.BuildExtension;
import io.quarkus.arc.processor.ContextConfigurator;
import io.quarkus.arc.processor.ContextRegistrar;
import io.quarkus.arc.processor.InterceptorBindingRegistrar;
import io.quarkus.arc.processor.ObserverConfigurator;
import io.quarkus.arc.processor.ObserverRegistrar;
import io.quarkus.arc.processor.QualifierRegistrar;
import io.quarkus.arc.processor.StereotypeInfo;
import io.quarkus.arc.processor.StereotypeRegistrar;
import io.quarkus.gizmo.FieldDescriptor;
import io.quarkus.gizmo.MethodDescriptor;
import io.quarkus.gizmo.ResultHandle;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.EventContext;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.Nonbinding;

import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;

public class CdiLiteExtensions {
    private final CdiLiteExtUtil util = new CdiLiteExtUtil();
    private final AllAnnotationOverlays annotationOverlays = new AllAnnotationOverlays();
    private final MessagesImpl messages = new MessagesImpl();

    private Map<Class<? extends Annotation>, Consumer<ClassConfig<?>>> qualifiers;
    private Map<Class<? extends Annotation>, Consumer<ClassConfig<?>>> interceptorBindings;
    private Map<Class<? extends Annotation>, Consumer<ClassConfig<?>>> stereotypes;
    private List<ContextBuilderImpl> contexts;

    private AllAnnotationTransformations annotationTransformations;

    private List<SyntheticBeanBuilderImpl<?>> syntheticBeans;
    private List<SyntheticObserverBuilderImpl> syntheticObservers;

    /**
     * Must be called first, <i>before</i> {@code initializeAnnotationTransformations}.
     */
    public void runDiscovery(org.jboss.jandex.IndexView applicationIndex, Set<String> additionalClasses) {
        CdiLiteExtDiscoveryProcessor discovery = new CdiLiteExtDiscoveryProcessor(util, applicationIndex,
                additionalClasses, messages);
        discovery.run();
        qualifiers = discovery.qualifiers;
        interceptorBindings = discovery.interceptorBindings;
        stereotypes = discovery.stereotypes;
        contexts = discovery.contexts;
    }

    /**
     * Must be called <i>after</i> {@code runDiscovery} and <i>before</i> {@code registerMetaAnnotations}.
     */
    public void initializeAnnotationTransformations(IndexView beanArchiveIndex, BeanProcessor.Builder builder) {
        this.annotationTransformations = new AllAnnotationTransformations(beanArchiveIndex, annotationOverlays);
        builder.addAnnotationTransformer(annotationTransformations.classes);
        builder.addAnnotationTransformer(annotationTransformations.methods);
        builder.addAnnotationTransformer(annotationTransformations.fields);
    }

    /**
     * Must be called <i>after</i> {@code initializeAnnotationTransformations} and <i>before</i> {@code runEnhancement}.
     */
    public void registerMetaAnnotations(IndexView beanArchiveIndex, BeanProcessor.Builder builder) {
        this.annotationTransformations = new AllAnnotationTransformations(beanArchiveIndex, annotationOverlays);
        builder.addAnnotationTransformer(annotationTransformations.classes);
        builder.addAnnotationTransformer(annotationTransformations.methods);
        builder.addAnnotationTransformer(annotationTransformations.fields);

        if (qualifiers != null && !qualifiers.isEmpty()) {
            builder.addQualifierRegistrar(new QualifierRegistrar() {
                @Override
                public Map<DotName, Set<String>> getAdditionalQualifiers() {
                    Map<DotName, Set<String>> result = new HashMap<>();
                    for (Map.Entry<Class<? extends Annotation>, Consumer<ClassConfig<?>>> entry : qualifiers.entrySet()) {
                        DotName annotationName = DotName.createSimple(entry.getKey().getName());

                        org.jboss.jandex.ClassInfo jandexAnnotation = beanArchiveIndex.getClassByName(annotationName);
                        ClassConfigImpl config = new ClassConfigImpl(beanArchiveIndex, annotationTransformations, jandexAnnotation);
                        entry.getValue().accept(config);

                        Set<String> nonbindingMembers = config.methods()
                                .stream()
                                .filter(it -> it.hasAnnotation(Nonbinding.class))
                                .map(MethodInfo::name)
                                .collect(Collectors.toSet());
                        result.put(annotationName, nonbindingMembers);
                    }
                    return result;
                }
            });
        }

        if (interceptorBindings != null && !interceptorBindings.isEmpty()) {
            builder.addInterceptorBindingRegistrar(new InterceptorBindingRegistrar() {
                @Override
                public Map<DotName, Set<String>> registerAdditionalBindings() {
                    Map<DotName, Set<String>> result = new HashMap<>();
                    for (Map.Entry<Class<? extends Annotation>, Consumer<ClassConfig<?>>> entry : qualifiers.entrySet()) {
                        DotName annotationName = DotName.createSimple(entry.getKey().getName());

                        org.jboss.jandex.ClassInfo jandexAnnotation = beanArchiveIndex.getClassByName(annotationName);
                        ClassConfigImpl config = new ClassConfigImpl(beanArchiveIndex, annotationTransformations, jandexAnnotation);
                        entry.getValue().accept(config);

                        Set<String> nonbindingMembers = config.methods()
                                .stream()
                                .filter(it -> it.hasAnnotation(Nonbinding.class))
                                .map(MethodInfo::name)
                                .collect(Collectors.toSet());
                        result.put(annotationName, nonbindingMembers);
                    }
                    return result;
                }
            });
        }

        if (stereotypes != null && !stereotypes.isEmpty()) {
            builder.addStereotypeRegistrar(new StereotypeRegistrar() {
                @Override
                public Set<DotName> getAdditionalStereotypes() {
                    Set<DotName> result = new HashSet<>();
                    for (Map.Entry<Class<? extends Annotation>, Consumer<ClassConfig<?>>> entry : qualifiers.entrySet()) {
                        DotName annotationName = DotName.createSimple(entry.getKey().getName());

                        org.jboss.jandex.ClassInfo jandexAnnotation = beanArchiveIndex.getClassByName(annotationName);
                        ClassConfigImpl config = new ClassConfigImpl(beanArchiveIndex, annotationTransformations, jandexAnnotation);
                        entry.getValue().accept(config);

                        result.add(annotationName);
                    }
                    return result;
                }
            });
        }

        if (contexts != null) {
            for (ContextBuilderImpl context : contexts) {
                if (context.implementationClass == null) {
                    // TODO proper diagnostics
                    throw new IllegalArgumentException("Context implementation class not set");
                }

                builder.addContextRegistrar(new ContextRegistrar() {
                    @Override
                    public void register(RegistrationContext registrationContext) {
                        // TODO this is all quite weird, maybe change the ContextBuilder API?

                        Class<? extends Annotation> scopeAnnotation = context.scopeAnnotation;
                        if (scopeAnnotation == null) {
                            try {
                                scopeAnnotation = context.implementationClass.newInstance().getScope();
                            } catch (ReflectiveOperationException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        // TODO !!!
                        Class<? extends InjectableContext> contextClass = (Class<? extends InjectableContext>) context.implementationClass;

                        ContextConfigurator config = registrationContext.configure(scopeAnnotation)
                                .contextClass(contextClass);
                        if (context.isNormal != null) {
                            config.normal(context.isNormal);
                        }
                        config.done();
                    }
                });
            }
        }
    }

    public void runEnhancement(org.jboss.jandex.IndexView beanArchiveIndex) {
        new CdiLiteExtEnhancementProcessor(util, beanArchiveIndex, annotationTransformations, messages).run();
    }

    public void runSynthesis(IndexView beanArchiveIndex, Collection<io.quarkus.arc.processor.BeanInfo> allBeans,
            Collection<io.quarkus.arc.processor.ObserverInfo> allObservers) {
        CdiLiteExtSynthesisProcessor synthesis = new CdiLiteExtSynthesisProcessor(util, beanArchiveIndex,
                annotationOverlays, allBeans, allObservers, messages);
        synthesis.run();
        syntheticBeans = synthesis.syntheticBeans;
        syntheticObservers = synthesis.syntheticObservers;
    }

    /**
     * Must be called <i>after</i> {@code runSynthesis} and <i>before</i> {@code runValidation}.
     */
    public void registerSyntheticBeans(BeanRegistrar.RegistrationContext context) {
        Map<DotName, StereotypeInfo> registeredStereotypes = context.get(BuildExtension.Key.STEREOTYPES);

        for (SyntheticBeanBuilderImpl<?> syntheticBean : syntheticBeans) {
            StereotypeInfo[] stereotypes = syntheticBean.stereotypes.stream()
                    .map(registeredStereotypes::get)
                    .toArray(StereotypeInfo[]::new);

            BeanConfigurator<Object> bean = context.configure(syntheticBean.implementationClass)
                    .types(syntheticBean.types.toArray(new org.jboss.jandex.Type[0]))
                    .qualifiers(syntheticBean.qualifiers.toArray(new org.jboss.jandex.AnnotationInstance[0]))
                    .stereotypes(stereotypes);
            if (syntheticBean.scope != null) {
                bean.scope(syntheticBean.scope);
            }
            if (syntheticBean.name != null) {
                bean.name(syntheticBean.name);
            }
            if (syntheticBean.isAlternative) {
                bean.alternativePriority(syntheticBean.priority);
            }
            for (Map.Entry<String, Object> entry : syntheticBean.params.entrySet()) {
                // TODO ugly
                if (entry.getValue() instanceof Boolean) {
                    bean.param(entry.getKey(), (Boolean) entry.getValue());
                } else if (entry.getValue() instanceof boolean[]) {
                    bean.param(entry.getKey(), (boolean[]) entry.getValue());
                } else if (entry.getValue() instanceof Integer) {
                    bean.param(entry.getKey(), (Integer) entry.getValue());
                } else if (entry.getValue() instanceof int[]) {
                    bean.param(entry.getKey(), (int[]) entry.getValue());
                } else if (entry.getValue() instanceof Long) {
                    bean.param(entry.getKey(), (Long) entry.getValue());
                } else if (entry.getValue() instanceof long[]) {
                    bean.param(entry.getKey(), (long[]) entry.getValue());
                } else if (entry.getValue() instanceof Double) {
                    bean.param(entry.getKey(), (Double) entry.getValue());
                } else if (entry.getValue() instanceof double[]) {
                    bean.param(entry.getKey(), (double[]) entry.getValue());
                } else if (entry.getValue() instanceof String) {
                    bean.param(entry.getKey(), (String) entry.getValue());
                } else if (entry.getValue() instanceof String[]) {
                    bean.param(entry.getKey(), (String[]) entry.getValue());
                } else if (entry.getValue() instanceof Class<?>) {
                    bean.param(entry.getKey(), (Class<?>) entry.getValue());
                } else if (entry.getValue() instanceof Class[]) {
                    bean.param(entry.getKey(), (Class<?>[]) entry.getValue());
                }
            }
            // TODO can't really know if the scope is @Dependent, because there may be a stereotype with default scope
            //  but this will have to do for now
            boolean isDependent = syntheticBean.scope == null || Dependent.class.equals(syntheticBean.scope);
            bean.creator(mc -> { // generated method signature: Object(CreationalContext)
                // | InjectionPoint injectionPoint = isDependent ? InjectionPointProvider.get() : null;
                ResultHandle injectionPoint = isDependent
                        ? mc.invokeStaticMethod(
                                MethodDescriptor.ofMethod(InjectionPointProvider.class, "get", InjectionPoint.class))
                        : mc.loadNull();

                // | Map<String, Object> params = this.params;
                // the generated bean class has a "params" field filled with all the data
                ResultHandle params = mc.readInstanceField(
                        FieldDescriptor.of(mc.getMethodDescriptor().getDeclaringClass(), "params", Map.class),
                        mc.getThis());

                // | SyntheticBeanCreator creator = new ConfiguredSyntheticBeanCreator();
                ResultHandle creator = mc.newInstance(MethodDescriptor.ofConstructor(syntheticBean.creatorClass));

                // | Object instance = creator.create(creationalContext, injectionPoint, params);
                ResultHandle[] args = { mc.getMethodParam(0), injectionPoint, params };
                ResultHandle instance = mc.invokeInterfaceMethod(MethodDescriptor.ofMethod(SyntheticBeanCreator.class,
                        "create", Object.class, CreationalContext.class, InjectionPoint.class, Map.class),
                        creator, args);

                // | return instance;
                mc.returnValue(instance);
            });
            bean.destroyer(mc -> { // generated method signature: void(Object, CreationalContext)
                // | Map<String, Object> params = this.params;
                // the generated bean class has a "params" field filled with all the data
                ResultHandle params = mc.readInstanceField(
                        FieldDescriptor.of(mc.getMethodDescriptor().getDeclaringClass(), "params", Map.class),
                        mc.getThis());

                // | SyntheticBeanDisposer disposer = new ConfiguredSyntheticBeanDisposer();
                ResultHandle disposer = mc.newInstance(MethodDescriptor.ofConstructor(syntheticBean.disposerClass));

                // | disposer.dispose(instance, creationalContext, params);
                ResultHandle[] args = { mc.getMethodParam(0), mc.getMethodParam(1), params };
                mc.invokeInterfaceMethod(MethodDescriptor.ofMethod(SyntheticBeanDisposer.class, "dispose",
                        void.class, Object.class, CreationalContext.class, Map.class), disposer, args);

                // return type is void
                mc.returnValue(null);
            });
            bean.done();
        }
    }

    /**
     * Must be called <i>after</i> {@code runSynthesis} and <i>before</i> {@code runValidation}.
     */
    public void registerSyntheticObservers(ObserverRegistrar.RegistrationContext context) {
        for (SyntheticObserverBuilderImpl syntheticObserver : syntheticObservers) {
            ObserverConfigurator observer = context.configure()
                    .beanClass(syntheticObserver.declaringClass)
                    .observedType(syntheticObserver.type)
                    .qualifiers(syntheticObserver.qualifiers.toArray(new org.jboss.jandex.AnnotationInstance[0]))
                    .priority(syntheticObserver.priority)
                    .async(syntheticObserver.isAsync)
                    .reception(syntheticObserver.reception)
                    .transactionPhase(syntheticObserver.transactionPhase);
            observer.notify(mc -> { // generated method signature: void(EventContext)
                // | SyntheticObserver instance = new ConfiguredEventConsumer();
                ResultHandle instance = mc.newInstance(MethodDescriptor.ofConstructor(syntheticObserver.implementationClass));

                // | instance.observe(eventContext);
                ResultHandle[] args = { mc.getMethodParam(0) };
                mc.invokeInterfaceMethod(MethodDescriptor.ofMethod(SyntheticObserver.class, "observe",
                        void.class, EventContext.class), instance, args);

                // return type is void
                mc.returnValue(null);
            });
            observer.done();
        }
    }

    /**
     * Must be called <i>after</i> {@code registerSynthetic{Beans,Observers}} and <i>before</i> {@code registerValidationErrors}.
     */
    public void runValidation(IndexView beanArchiveIndex, Collection<io.quarkus.arc.processor.BeanInfo> allBeans,
            Collection<io.quarkus.arc.processor.ObserverInfo> allObservers) {
        new CdiLiteExtValidationProcessor(util, beanArchiveIndex, annotationOverlays, allBeans, allObservers, messages).run();
    }

    /**
     * Must be called last, <i>after</i> {@code runValidation}.
     */
    public void registerValidationErrors(BeanDeploymentValidator.ValidationContext context) {
        for (Throwable error : messages.errors) {
            context.addDeploymentProblem(error);
        }
    }
}
