package io.quarkus.arc.processor.cdi.build.compatible.extensions;

import java.util.List;

import org.jboss.jandex.DotName;

class ExtensionPhaseSynthesis extends ExtensionPhaseBase {
    private final AllAnnotationOverlays annotationOverlays;

    private final List<SyntheticBeanBuilderImpl<?>> syntheticBeans;
    private final List<SyntheticObserverBuilderImpl<?>> syntheticObservers;

    ExtensionPhaseSynthesis(ExtensionInvoker invoker, org.jboss.jandex.IndexView beanArchiveIndex, MessagesImpl messages,
            AllAnnotationOverlays annotationOverlays, List<SyntheticBeanBuilderImpl<?>> syntheticBeans,
            List<SyntheticObserverBuilderImpl<?>> syntheticObservers) {
        super(ExtensionPhase.SYNTHESIS, invoker, beanArchiveIndex, messages);
        this.annotationOverlays = annotationOverlays;
        this.syntheticBeans = syntheticBeans;
        this.syntheticObservers = syntheticObservers;
    }

    @Override
    Object argumentForExtensionMethod(ExtensionMethodParameterType type, org.jboss.jandex.MethodInfo method) {
        switch (type) {
            case SYNTHETIC_COMPONENTS:
                DotName extensionClass = method.declaringClass().name();
                return new SyntheticComponentsImpl(syntheticBeans, syntheticObservers, extensionClass);
            case TYPES:
                return new TypesImpl(index, annotationOverlays);

            default:
                return super.argumentForExtensionMethod(type, method);
        }
    }
}
