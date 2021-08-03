package io.quarkus.arc.processor.cdi.lite.ext;

import static io.quarkus.arc.processor.cdi.lite.ext.CdiLiteExtUtil.ExtensionMethodParameterType;
import static io.quarkus.arc.processor.cdi.lite.ext.CdiLiteExtUtil.Phase;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import javax.enterprise.inject.build.compatible.spi.ClassConfig;
import javax.enterprise.inject.build.compatible.spi.Messages;

class CdiLiteExtDiscoveryProcessor {
    private final CdiLiteExtUtil util;
    private final org.jboss.jandex.IndexView applicationIndex;
    private final Set<String> additionalClasses;
    private final Messages messages;

    final Map<Class<? extends Annotation>, Consumer<ClassConfig<?>>> qualifiers = new HashMap<>();
    final Map<Class<? extends Annotation>, Consumer<ClassConfig<?>>> interceptorBindings = new HashMap<>();
    final Map<Class<? extends Annotation>, Consumer<ClassConfig<?>>> stereotypes = new HashMap<>();
    final List<ContextConfigImpl> contexts = new ArrayList<>();

    CdiLiteExtDiscoveryProcessor(CdiLiteExtUtil util, org.jboss.jandex.IndexView applicationIndex,
            Set<String> additionalClasses, Messages messages) {
        this.util = util;
        this.applicationIndex = applicationIndex;
        this.additionalClasses = additionalClasses;
        this.messages = messages;
    }

    void run() {
        try {
            doRun();
        } catch (Exception e) {
            // TODO proper diagnostics system
            throw new RuntimeException(e);
        }
    }

    private void doRun() throws ReflectiveOperationException {
        List<org.jboss.jandex.MethodInfo> extensionMethods = util.findExtensionMethods(DotNames.DISCOVERY);

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

            if (!kind.isAvailableIn(Phase.DISCOVERY)) {
                throw new IllegalArgumentException("@Discovery methods can't declare a parameter of type "
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
            case META_ANNOTATIONS:
                return new MetaAnnotationsImpl(qualifiers, interceptorBindings, stereotypes, contexts);
            case MESSAGES:
                return messages;
            case SCANNED_CLASSES:
                return new ScannedClassesImpl(additionalClasses);

            default:
                // TODO should report an error here
                return null;
        }
    }
}
