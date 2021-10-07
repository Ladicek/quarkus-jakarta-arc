package io.quarkus.arc.processor.cdi.lite.ext;

import java.util.ArrayList;
import java.util.List;

abstract class ExtensionPhaseBase {
    private final ExtensionPhase phase;

    final ExtensionInvoker util;
    // this is the application index in @Discovery and the bean archive index in subsequent phases
    final org.jboss.jandex.IndexView index;
    final MessagesImpl messages;

    ExtensionPhaseBase(ExtensionPhase phase, ExtensionInvoker util, org.jboss.jandex.IndexView index, MessagesImpl messages) {
        this.phase = phase;

        this.util = util;
        this.index = index;
        this.messages = messages;
    }

    final void run() {
        try {
            List<org.jboss.jandex.MethodInfo> extensionMethods = util.findExtensionMethods(phase.annotationName);

            for (org.jboss.jandex.MethodInfo method : extensionMethods) {
                runExtensionMethod(method);
            }
        } catch (Exception e) {
            // TODO proper diagnostics system
            throw new RuntimeException(e);
        }
    }

    // complex phases may override, but this is enough for the simple phases
    void runExtensionMethod(org.jboss.jandex.MethodInfo method) throws ReflectiveOperationException {
        int numParameters = method.parameters().size();
        List<ExtensionMethodParameterType> parameters = new ArrayList<>(numParameters);
        for (int i = 0; i < numParameters; i++) {
            org.jboss.jandex.Type parameterType = method.parameters().get(i);
            ExtensionMethodParameterType parameter = ExtensionMethodParameterType.of(parameterType);
            parameters.add(parameter);

            parameter.verifyAvailable(phase, method);
        }

        List<Object> arguments = new ArrayList<>(numParameters);
        for (ExtensionMethodParameterType parameter : parameters) {
            Object argument = argumentForExtensionMethod(parameter, method);
            arguments.add(argument);
        }

        util.callExtensionMethod(method, arguments);
    }

    // all phases should override and use this as a fallback
    Object argumentForExtensionMethod(ExtensionMethodParameterType type, org.jboss.jandex.MethodInfo method) {
        if (type == ExtensionMethodParameterType.MESSAGES) {
            return messages;
        }

        throw new IllegalArgumentException("internal error, " + type + " parameter declared at "
                + method.declaringClass().simpleName() + "." + method.name());
    }
}
