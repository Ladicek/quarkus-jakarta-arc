package io.quarkus.arc.processor.cdi.lite.ext;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import javax.enterprise.inject.build.compatible.spi.BeanInfo;
import javax.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import javax.enterprise.inject.build.compatible.spi.ClassConfig;
import javax.enterprise.inject.build.compatible.spi.Discovery;
import javax.enterprise.inject.build.compatible.spi.Enhancement;
import javax.enterprise.inject.build.compatible.spi.FieldConfig;
import javax.enterprise.inject.build.compatible.spi.InterceptorInfo;
import javax.enterprise.inject.build.compatible.spi.Messages;
import javax.enterprise.inject.build.compatible.spi.MetaAnnotations;
import javax.enterprise.inject.build.compatible.spi.MethodConfig;
import javax.enterprise.inject.build.compatible.spi.ObserverInfo;
import javax.enterprise.inject.build.compatible.spi.Registration;
import javax.enterprise.inject.build.compatible.spi.ScannedClasses;
import javax.enterprise.inject.build.compatible.spi.Synthesis;
import javax.enterprise.inject.build.compatible.spi.SyntheticComponents;
import javax.enterprise.inject.build.compatible.spi.Types;
import javax.enterprise.inject.build.compatible.spi.Validation;
import javax.enterprise.lang.model.declarations.ClassInfo;
import javax.enterprise.lang.model.declarations.FieldInfo;
import javax.enterprise.lang.model.declarations.MethodInfo;
import org.jboss.jandex.DotName;

class DotNames {
    static final DotName ANNOTATION = DotName.createSimple(Annotation.class.getName());
    static final DotName OBJECT = DotName.createSimple(Object.class.getName());
    static final DotName REPEATABLE = DotName.createSimple(Repeatable.class.getName());

    // common annotations

    // TODO temporary, until we can use jakarta.annotation.Priority
    static final DotName PRIORITY = DotName.createSimple(cdi.lite.Priority.class.getName());

    // lang model

    static final DotName CLASS_INFO = DotName.createSimple(ClassInfo.class.getName());
    static final DotName METHOD_INFO = DotName.createSimple(MethodInfo.class.getName());
    static final DotName FIELD_INFO = DotName.createSimple(FieldInfo.class.getName());

    // extension API

    static final DotName BUILD_COMPATIBLE_EXTENSION = DotName.createSimple(BuildCompatibleExtension.class.getName());

    static final DotName DISCOVERY = DotName.createSimple(Discovery.class.getName());
    static final DotName ENHANCEMENT = DotName.createSimple(Enhancement.class.getName());
    static final DotName REGISTRATION = DotName.createSimple(Registration.class.getName());
    static final DotName SYNTHESIS = DotName.createSimple(Synthesis.class.getName());
    static final DotName VALIDATION = DotName.createSimple(Validation.class.getName());

    static final DotName CLASS_CONFIG = DotName.createSimple(ClassConfig.class.getName());
    static final DotName METHOD_CONFIG = DotName.createSimple(MethodConfig.class.getName());
    static final DotName FIELD_CONFIG = DotName.createSimple(FieldConfig.class.getName());

    static final DotName BEAN_INFO = DotName.createSimple(BeanInfo.class.getName());
    static final DotName INTERCEPTOR_INFO = DotName.createSimple(InterceptorInfo.class.getName());
    static final DotName OBSERVER_INFO = DotName.createSimple(ObserverInfo.class.getName());

    static final DotName MESSAGES = DotName.createSimple(Messages.class.getName());
    static final DotName META_ANNOTATIONS = DotName.createSimple(MetaAnnotations.class.getName());
    static final DotName SCANNED_CLASSES = DotName.createSimple(ScannedClasses.class.getName());
    static final DotName SYNTHETIC_COMPONENTS = DotName.createSimple(SyntheticComponents.class.getName());
    static final DotName TYPES = DotName.createSimple(Types.class.getName());
}
