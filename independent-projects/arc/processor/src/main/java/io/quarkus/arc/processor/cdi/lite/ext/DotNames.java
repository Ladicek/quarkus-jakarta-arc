package io.quarkus.arc.processor.cdi.lite.ext;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import javax.enterprise.inject.build.compatible.spi.Annotations;
import javax.enterprise.inject.build.compatible.spi.AppArchive;
import javax.enterprise.inject.build.compatible.spi.AppArchiveBuilder;
import javax.enterprise.inject.build.compatible.spi.AppArchiveConfig;
import javax.enterprise.inject.build.compatible.spi.AppDeployment;
import javax.enterprise.inject.build.compatible.spi.BeanInfo;
import javax.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import javax.enterprise.inject.build.compatible.spi.ClassConfig;
import javax.enterprise.inject.build.compatible.spi.Discovery;
import javax.enterprise.inject.build.compatible.spi.Enhancement;
import javax.enterprise.inject.build.compatible.spi.ExactType;
import javax.enterprise.inject.build.compatible.spi.ExtensionPriority;
import javax.enterprise.inject.build.compatible.spi.FieldConfig;
import javax.enterprise.inject.build.compatible.spi.Messages;
import javax.enterprise.inject.build.compatible.spi.MetaAnnotations;
import javax.enterprise.inject.build.compatible.spi.MethodConfig;
import javax.enterprise.inject.build.compatible.spi.ObserverInfo;
import javax.enterprise.inject.build.compatible.spi.Processing;
import javax.enterprise.inject.build.compatible.spi.SubtypesOf;
import javax.enterprise.inject.build.compatible.spi.Synthesis;
import javax.enterprise.inject.build.compatible.spi.SyntheticComponents;
import javax.enterprise.inject.build.compatible.spi.Types;
import javax.enterprise.inject.build.compatible.spi.Validation;
import org.jboss.jandex.DotName;

class DotNames {
    static final DotName ANNOTATION = DotName.createSimple(Annotation.class.getName());
    static final DotName OBJECT = DotName.createSimple(Object.class.getName());
    static final DotName REPEATABLE = DotName.createSimple(Repeatable.class.getName());

    static final DotName BUILD_COMPATIBLE_EXTENSION = DotName.createSimple(BuildCompatibleExtension.class.getName());

    static final DotName DISCOVERY = DotName.createSimple(Discovery.class.getName());
    static final DotName ENHANCEMENT = DotName.createSimple(Enhancement.class.getName());
    static final DotName PROCESSING = DotName.createSimple(Processing.class.getName());
    static final DotName SYNTHESIS = DotName.createSimple(Synthesis.class.getName());
    static final DotName VALIDATION = DotName.createSimple(Validation.class.getName());

    static final DotName EXTENSION_PRIORITY = DotName.createSimple(ExtensionPriority.class.getName());
    static final DotName EXACT_TYPE = DotName.createSimple(ExactType.class.getName());
    static final DotName SUBTYPES_OF = DotName.createSimple(SubtypesOf.class.getName());

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
    static final DotName MESSAGES = DotName.createSimple(Messages.class.getName());
    static final DotName META_ANNOTATIONS = DotName.createSimple(MetaAnnotations.class.getName());
    static final DotName SYNTHETIC_COMPONENTS = DotName.createSimple(SyntheticComponents.class.getName());
    static final DotName TYPES = DotName.createSimple(Types.class.getName());
}
