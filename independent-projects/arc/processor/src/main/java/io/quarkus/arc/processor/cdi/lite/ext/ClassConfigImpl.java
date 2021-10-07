package io.quarkus.arc.processor.cdi.lite.ext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.enterprise.inject.build.compatible.spi.ClassConfig;
import javax.enterprise.inject.build.compatible.spi.FieldConfig;
import javax.enterprise.inject.build.compatible.spi.MethodConfig;
import javax.enterprise.lang.model.declarations.ClassInfo;
import javax.enterprise.lang.model.declarations.FieldInfo;
import javax.enterprise.lang.model.declarations.MethodInfo;
import org.jboss.jandex.DotName;

class ClassConfigImpl extends DeclarationConfigImpl<DotName, org.jboss.jandex.ClassInfo, ClassConfigImpl>
        implements ClassConfig {
    ClassConfigImpl(org.jboss.jandex.IndexView jandexIndex, AllAnnotationTransformations allTransformations,
            org.jboss.jandex.ClassInfo jandexDeclaration) {
        super(jandexIndex, allTransformations, allTransformations.classes, jandexDeclaration);
    }

    @Override
    public ClassInfo info() {
        return new ClassInfoImpl(jandexIndex, allTransformations.annotationOverlays, jandexDeclaration);
    }

    @Override
    public Collection<MethodConfig> constructors() {
        List<MethodConfig> result = new ArrayList<>();
        for (MethodInfo constructor : info().constructors()) {
            result.add(new MethodConfigImpl(jandexIndex, allTransformations, ((MethodInfoImpl) constructor).jandexDeclaration));
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public Collection<MethodConfig> methods() {
        List<MethodConfig> result = new ArrayList<>();
        for (MethodInfo method : info().methods()) {
            result.add(new MethodConfigImpl(jandexIndex, allTransformations, ((MethodInfoImpl) method).jandexDeclaration));
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public Collection<FieldConfig> fields() {
        List<FieldConfig> result = new ArrayList<>();
        for (FieldInfo field : info().fields()) {
            result.add(new FieldConfigImpl(jandexIndex, allTransformations, ((FieldInfoImpl) field).jandexDeclaration));
        }
        return Collections.unmodifiableList(result);
    }
}
