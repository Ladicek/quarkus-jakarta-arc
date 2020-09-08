package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.AppArchive;
import cdi.lite.extension.AppDeployment;
import cdi.lite.extension.ExtensionPriority;
import cdi.lite.extension.Types;
import cdi.lite.extension.WithAnnotations;
import cdi.lite.extension.beans.BeanInfo;
import cdi.lite.extension.beans.ObserverInfo;
import cdi.lite.extension.model.declarations.ClassInfo;
import cdi.lite.extension.model.declarations.FieldInfo;
import cdi.lite.extension.model.declarations.MethodInfo;
import cdi.lite.extension.phases.Discovery;
import cdi.lite.extension.phases.Enhancement;
import cdi.lite.extension.phases.Synthesis;
import cdi.lite.extension.phases.Validation;
import cdi.lite.extension.phases.discovery.AppArchiveBuilder;
import cdi.lite.extension.phases.discovery.Contexts;
import cdi.lite.extension.phases.enhancement.Annotations;
import cdi.lite.extension.phases.enhancement.AppArchiveConfig;
import cdi.lite.extension.phases.enhancement.ClassConfig;
import cdi.lite.extension.phases.enhancement.FieldConfig;
import cdi.lite.extension.phases.enhancement.MethodConfig;
import cdi.lite.extension.phases.synthesis.SyntheticComponents;
import cdi.lite.extension.phases.validation.Errors;
import java.lang.annotation.Repeatable;
import java.util.Collection;
import org.jboss.jandex.DotName;

class DotNames {
    static final DotName COLLECTION = DotName.createSimple(Collection.class.getName());
    static final DotName OBJECT = DotName.createSimple(Object.class.getName());
    static final DotName REPEATABLE = DotName.createSimple(Repeatable.class.getName());

    static final DotName DISCOVERY = DotName.createSimple(Discovery.class.getName());
    static final DotName ENHANCEMENT = DotName.createSimple(Enhancement.class.getName());
    static final DotName SYNTHESIS = DotName.createSimple(Synthesis.class.getName());
    static final DotName VALIDATION = DotName.createSimple(Validation.class.getName());

    static final DotName EXTENSION_PRIORITY = DotName.createSimple(ExtensionPriority.class.getName());
    static final DotName WITH_ANNOTATIONS = DotName.createSimple(WithAnnotations.class.getName());

    static final DotName CLASS_INFO = DotName.createSimple(ClassInfo.class.getName());
    static final DotName METHOD_INFO = DotName.createSimple(MethodInfo.class.getName());
    static final DotName FIELD_INFO = DotName.createSimple(FieldInfo.class.getName());

    static final DotName CLASS_CONFIG = DotName.createSimple(ClassConfig.class.getName());
    static final DotName METHOD_CONFIG = DotName.createSimple(MethodConfig.class.getName());
    static final DotName FIELD_CONFIG = DotName.createSimple(FieldConfig.class.getName());

    static final DotName BEAN_INFO = DotName.createSimple(BeanInfo.class.getName());
    static final DotName OBSERVER_INFO = DotName.createSimple(ObserverInfo.class.getName());

    static final DotName ANNOTATIONS = DotName.createSimple(Annotations.class.getName());
    static final DotName APP_ARCHIVE = DotName.createSimple(AppArchive.class.getName());
    static final DotName APP_ARCHIVE_BUILDER = DotName.createSimple(AppArchiveBuilder.class.getName());
    static final DotName APP_ARCHIVE_CONFIG = DotName.createSimple(AppArchiveConfig.class.getName());
    static final DotName APP_DEPLOYMENT = DotName.createSimple(AppDeployment.class.getName());
    static final DotName CONTEXTS = DotName.createSimple(Contexts.class.getName());
    static final DotName ERRORS = DotName.createSimple(Errors.class.getName());
    static final DotName SYNTHETIC_COMPONENTS = DotName.createSimple(SyntheticComponents.class.getName());
    static final DotName TYPES = DotName.createSimple(Types.class.getName());
}
