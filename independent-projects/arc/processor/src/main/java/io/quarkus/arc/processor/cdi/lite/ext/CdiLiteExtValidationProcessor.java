package io.quarkus.arc.processor.cdi.lite.ext;

import static io.quarkus.arc.processor.cdi.lite.ext.CdiLiteExtUtil.ExtensionMethodParameterType;
import static io.quarkus.arc.processor.cdi.lite.ext.CdiLiteExtUtil.Phase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jboss.jandex.IndexView;

class CdiLiteExtValidationProcessor {
    private final CdiLiteExtUtil util;
    private final org.jboss.jandex.IndexView beanArchiveIndex;
    private final AllAnnotationOverlays annotationOverlays;
    private final Collection<io.quarkus.arc.processor.BeanInfo> allBeans;
    private final Collection<io.quarkus.arc.processor.ObserverInfo> allObservers;
    private final MessagesImpl messages;

    CdiLiteExtValidationProcessor(CdiLiteExtUtil util, IndexView beanArchiveIndex, AllAnnotationOverlays annotationOverlays,
            Collection<io.quarkus.arc.processor.BeanInfo> allBeans,
            Collection<io.quarkus.arc.processor.ObserverInfo> allObservers,
            MessagesImpl messages) {
        this.util = util;
        this.beanArchiveIndex = beanArchiveIndex;
        this.annotationOverlays = annotationOverlays;
        this.allBeans = allBeans;
        this.allObservers = allObservers;
        this.messages = messages;
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
        List<org.jboss.jandex.MethodInfo> extensionMethods = util.findExtensionMethods(DotNames.VALIDATION);

        for (org.jboss.jandex.MethodInfo method : extensionMethods) {
            processExtensionMethod(method);
        }
    }

    private void processExtensionMethod(org.jboss.jandex.MethodInfo method) throws ReflectiveOperationException {
        int numParameters = method.parameters().size();
        List<ExtensionMethodParameterType> parameters = new ArrayList<>(numParameters);
        for (int i = 0; i < numParameters; i++) {
            org.jboss.jandex.Type parameterType = method.parameters().get(i);
            ExtensionMethodParameterType kind = ExtensionMethodParameterType.of(parameterType);
            parameters.add(kind);

            if (!kind.isAvailableIn(Phase.VALIDATION)) {
                throw new IllegalArgumentException("@Validation methods can't declare a parameter of type "
                        + parameterType + ", found at " + method + " @ " + method.declaringClass());
            }
        }

        List<Object> arguments = new ArrayList<>(numParameters);
        for (ExtensionMethodParameterType parameter : parameters) {
            Object argument = createArgumentForExtensionMethodParameter(parameter);
            arguments.add(argument);
        }

        util.callExtensionMethod(method, arguments);
    }

    private Object createArgumentForExtensionMethodParameter(ExtensionMethodParameterType kind) {
        switch (kind) {
            case APP_ARCHIVE:
                return new AppArchiveImpl(beanArchiveIndex, annotationOverlays);
            case APP_DEPLOYMENT:
                return new AppDeploymentImpl(beanArchiveIndex, annotationOverlays, allBeans, allObservers);
            case MESSAGES:
                return messages;
            case TYPES:
                return new TypesImpl(beanArchiveIndex, annotationOverlays);

            default:
                // TODO should report an error here
                return null;
        }
    }
}
