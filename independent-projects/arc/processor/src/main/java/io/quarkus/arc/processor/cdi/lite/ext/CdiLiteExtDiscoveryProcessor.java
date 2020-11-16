package io.quarkus.arc.processor.cdi.lite.ext;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CdiLiteExtDiscoveryProcessor extends CdiLiteExtProcessorBase {
    private final org.jboss.jandex.IndexView applicationIndex;
    private final Set<String> additionalClasses;

    public CdiLiteExtDiscoveryProcessor(org.jboss.jandex.IndexView applicationIndex, Set<String> additionalClasses) {
        this.applicationIndex = applicationIndex;
        this.additionalClasses = additionalClasses;
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
        List<org.jboss.jandex.MethodInfo> extensionMethods = findExtensionMethods(DotNames.DISCOVERY);

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

        callExtensionMethod(method, arguments);
    }

    private Object createArgumentForExtensionMethodParameter(ExtensionMethodParameterType kind) {
        switch (kind) {
            case APP_ARCHIVE_BUILDER:
                return new AppArchiveBuilderImpl(applicationIndex, additionalClasses);

            default:
                // TODO should report an error here
                return null;
        }
    }
}
