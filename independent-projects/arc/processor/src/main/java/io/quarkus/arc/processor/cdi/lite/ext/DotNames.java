package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.AppArchive;
import cdi.lite.extension.AppDeployment;
import cdi.lite.extension.BuildCompatibleExtension;
import cdi.lite.extension.ExtensionPriority;
import cdi.lite.extension.Messages;
import cdi.lite.extension.Types;
import cdi.lite.extension.phases.Discovery;
import cdi.lite.extension.phases.Enhancement;
import cdi.lite.extension.phases.Synthesis;
import cdi.lite.extension.phases.Validation;
import cdi.lite.extension.phases.discovery.AppArchiveBuilder;
import cdi.lite.extension.phases.discovery.Contexts;
import cdi.lite.extension.phases.enhancement.Annotations;
import cdi.lite.extension.phases.enhancement.AppArchiveConfig;
import cdi.lite.extension.phases.enhancement.ClassEntrypoint;
import cdi.lite.extension.phases.enhancement.ExactType;
import cdi.lite.extension.phases.enhancement.FieldEntrypoint;
import cdi.lite.extension.phases.enhancement.MethodEntrypoint;
import cdi.lite.extension.phases.enhancement.SubtypesOf;
import cdi.lite.extension.phases.synthesis.SyntheticComponents;
import java.lang.annotation.Repeatable;
import org.jboss.jandex.DotName;

class DotNames {
    static final DotName REPEATABLE = DotName.createSimple(Repeatable.class.getName());

    static final DotName BUILD_COMPATIBLE_EXTENSION = DotName.createSimple(BuildCompatibleExtension.class.getName());

    static final DotName DISCOVERY = DotName.createSimple(Discovery.class.getName());
    static final DotName ENHANCEMENT = DotName.createSimple(Enhancement.class.getName());
    static final DotName SYNTHESIS = DotName.createSimple(Synthesis.class.getName());
    static final DotName VALIDATION = DotName.createSimple(Validation.class.getName());

    static final DotName EXTENSION_PRIORITY = DotName.createSimple(ExtensionPriority.class.getName());
    static final DotName EXACT_TYPE = DotName.createSimple(ExactType.class.getName());
    static final DotName SUBTYPES_OF = DotName.createSimple(SubtypesOf.class.getName());

    static final DotName CLASS_ENTRYPOINT = DotName.createSimple(ClassEntrypoint.class.getName());
    static final DotName METHOD_ENTRYPOINT = DotName.createSimple(MethodEntrypoint.class.getName());
    static final DotName FIELD_ENTRYPOINT = DotName.createSimple(FieldEntrypoint.class.getName());

    static final DotName ANNOTATIONS = DotName.createSimple(Annotations.class.getName());
    static final DotName APP_ARCHIVE = DotName.createSimple(AppArchive.class.getName());
    static final DotName APP_ARCHIVE_BUILDER = DotName.createSimple(AppArchiveBuilder.class.getName());
    static final DotName APP_ARCHIVE_CONFIG = DotName.createSimple(AppArchiveConfig.class.getName());
    static final DotName APP_DEPLOYMENT = DotName.createSimple(AppDeployment.class.getName());
    static final DotName CONTEXTS = DotName.createSimple(Contexts.class.getName());
    static final DotName MESSAGES = DotName.createSimple(Messages.class.getName());
    static final DotName SYNTHETIC_COMPONENTS = DotName.createSimple(SyntheticComponents.class.getName());
    static final DotName TYPES = DotName.createSimple(Types.class.getName());
}
