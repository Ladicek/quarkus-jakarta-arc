package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.Annotations;
import cdi.lite.extension.Extension;
import cdi.lite.extension.Types;
import cdi.lite.extension.WithAnnotations;
import cdi.lite.extension.World;
import cdi.lite.extension.model.configs.ClassConfig;
import cdi.lite.extension.model.configs.FieldConfig;
import cdi.lite.extension.model.configs.MethodConfig;
import cdi.lite.extension.model.declarations.ClassInfo;
import cdi.lite.extension.model.declarations.FieldInfo;
import cdi.lite.extension.model.declarations.MethodInfo;
import cdi.lite.extension.model.declarations.ParameterInfo;
import java.util.Collection;
import org.jboss.jandex.DotName;

class DotNames {
    static final DotName OBJECT = DotName.createSimple(Object.class.getName());

    static final DotName LITE_EXTENSION = DotName.createSimple(Extension.class.getName());
    static final DotName COLLECTION = DotName.createSimple(Collection.class.getName());

    static final DotName CLASS_INFO = DotName.createSimple(ClassInfo.class.getName());
    static final DotName METHOD_INFO = DotName.createSimple(MethodInfo.class.getName());
    static final DotName PARAMETER_INFO = DotName.createSimple(ParameterInfo.class.getName());
    static final DotName FIELD_INFO = DotName.createSimple(FieldInfo.class.getName());

    static final DotName CLASS_CONFIG = DotName.createSimple(ClassConfig.class.getName());
    static final DotName METHOD_CONFIG = DotName.createSimple(MethodConfig.class.getName());
    static final DotName FIELD_CONFIG = DotName.createSimple(FieldConfig.class.getName());

    static final DotName WITH_ANNOTATIONS = DotName.createSimple(WithAnnotations.class.getName());

    static final DotName ANNOTATIONS = DotName.createSimple(Annotations.class.getName());
    static final DotName TYPES = DotName.createSimple(Types.class.getName());
    static final DotName WORLD = DotName.createSimple(World.class.getName());
}
