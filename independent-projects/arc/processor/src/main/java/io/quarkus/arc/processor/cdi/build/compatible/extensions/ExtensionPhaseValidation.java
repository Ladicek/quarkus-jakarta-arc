package io.quarkus.arc.processor.cdi.build.compatible.extensions;

import java.util.Collection;

class ExtensionPhaseValidation extends ExtensionPhaseBase {
    private final AllAnnotationOverlays annotationOverlays;
    private final Collection<io.quarkus.arc.processor.BeanInfo> allBeans;
    private final Collection<io.quarkus.arc.processor.ObserverInfo> allObservers;

    ExtensionPhaseValidation(ExtensionInvoker invoker, org.jboss.jandex.IndexView beanArchiveIndex, MessagesImpl messages,
            AllAnnotationOverlays annotationOverlays, Collection<io.quarkus.arc.processor.BeanInfo> allBeans,
            Collection<io.quarkus.arc.processor.ObserverInfo> allObservers) {
        super(ExtensionPhase.VALIDATION, invoker, beanArchiveIndex, messages);
        this.annotationOverlays = annotationOverlays;
        this.allBeans = allBeans;
        this.allObservers = allObservers;
    }

    @Override
    Object argumentForExtensionMethod(ExtensionMethodParameterType type, org.jboss.jandex.MethodInfo method) {
        if (type == ExtensionMethodParameterType.TYPES) {
            return new TypesImpl(index, annotationOverlays);
        }

        return super.argumentForExtensionMethod(type, method);
    }
}
