package io.quarkus.arc.processor.cdi.build.compatible.extensions;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import org.jboss.jandex.DotName;

enum ExtensionMethodParameterType {
    META_ANNOTATIONS(DotNames.META_ANNOTATIONS, false, ExtensionPhase.DISCOVERY),
    SCANNED_CLASSES(DotNames.SCANNED_CLASSES, false, ExtensionPhase.DISCOVERY),

    CLASS_INFO(DotNames.CLASS_INFO, true, ExtensionPhase.ENHANCEMENT),
    METHOD_INFO(DotNames.METHOD_INFO, true, ExtensionPhase.ENHANCEMENT),
    FIELD_INFO(DotNames.FIELD_INFO, true, ExtensionPhase.ENHANCEMENT),

    CLASS_CONFIG(DotNames.CLASS_CONFIG, true, ExtensionPhase.ENHANCEMENT),
    METHOD_CONFIG(DotNames.METHOD_CONFIG, true, ExtensionPhase.ENHANCEMENT),
    FIELD_CONFIG(DotNames.FIELD_CONFIG, true, ExtensionPhase.ENHANCEMENT),

    BEAN_INFO(DotNames.BEAN_INFO, true, ExtensionPhase.REGISTRATION),
    INTERCEPTOR_INFO(DotNames.INTERCEPTOR_INFO, true, ExtensionPhase.REGISTRATION),
    OBSERVER_INFO(DotNames.OBSERVER_INFO, true, ExtensionPhase.REGISTRATION),

    SYNTHETIC_COMPONENTS(DotNames.SYNTHETIC_COMPONENTS, false, ExtensionPhase.SYNTHESIS),

    MESSAGES(DotNames.MESSAGES, false, ExtensionPhase.DISCOVERY, ExtensionPhase.ENHANCEMENT,
            ExtensionPhase.REGISTRATION, ExtensionPhase.SYNTHESIS, ExtensionPhase.VALIDATION),
    TYPES(DotNames.TYPES, false, ExtensionPhase.ENHANCEMENT, ExtensionPhase.REGISTRATION,
            ExtensionPhase.SYNTHESIS, ExtensionPhase.VALIDATION),

    UNKNOWN(null, false),
    ;

    private final DotName typeName;
    private final boolean isQuery;
    private final Set<ExtensionPhase> validPhases;

    ExtensionMethodParameterType(DotName typeName, boolean isQuery, ExtensionPhase... validPhases) {
        this.typeName = typeName;
        this.isQuery = isQuery;
        if (validPhases == null || validPhases.length == 0) {
            this.validPhases = EnumSet.noneOf(ExtensionPhase.class);
        } else {
            this.validPhases = EnumSet.copyOf(Arrays.asList(validPhases));
        }
    }

    boolean isQuery() {
        return isQuery;
    }

    void verifyAvailable(ExtensionPhase phase, org.jboss.jandex.MethodInfo method) {
        if (!validPhases.contains(phase)) {
            throw new IllegalArgumentException(phase + " methods can't declare a parameter of type "
                    + (typeName != null ? typeName.withoutPackagePrefix() : this.name()) + ", found at "
                    + method.declaringClass().simpleName() + "." + method.name());
        }
    }

    static ExtensionMethodParameterType of(org.jboss.jandex.Type type) {
        if (type.kind() == org.jboss.jandex.Type.Kind.CLASS) {
            for (ExtensionMethodParameterType candidate : ExtensionMethodParameterType.values()) {
                if (candidate.typeName.equals(type.name())) {
                    return candidate;
                }
            }
        }

        return UNKNOWN;
    }
}
