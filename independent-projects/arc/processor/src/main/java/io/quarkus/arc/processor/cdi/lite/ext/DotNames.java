package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.LiteExtension;
import cdi.lite.extension.model.configs.ClassConfig;
import cdi.lite.extension.model.declarations.ClassInfo;
import cdi.lite.extension.model.declarations.FieldInfo;
import cdi.lite.extension.model.declarations.MethodInfo;
import cdi.lite.extension.model.declarations.ParameterInfo;
import java.util.Collection;
import java.util.stream.Stream;
import org.jboss.jandex.DotName;

class DotNames {
    static final DotName LITE_EXTENSION = DotName.createSimple(LiteExtension.class.getName());
    static final DotName COLLECTION = DotName.createSimple(Collection.class.getName());
    static final DotName STREAM = DotName.createSimple(Stream.class.getName());

    static final DotName CLASS_INFO = DotName.createSimple(ClassInfo.class.getName());
    static final DotName METHOD_INFO = DotName.createSimple(MethodInfo.class.getName());
    static final DotName PARAMETER_INFO = DotName.createSimple(ParameterInfo.class.getName());
    static final DotName FIELD_INFO = DotName.createSimple(FieldInfo.class.getName());

    static final DotName CLASS_CONFIG = DotName.createSimple(ClassConfig.class.getName());
}
