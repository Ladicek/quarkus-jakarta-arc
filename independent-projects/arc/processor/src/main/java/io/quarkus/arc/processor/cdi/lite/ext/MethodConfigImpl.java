package io.quarkus.arc.processor.cdi.lite.ext;

import jakarta.enterprise.inject.build.compatible.spi.MethodConfig;
import jakarta.enterprise.inject.build.compatible.spi.ParameterConfig;
import jakarta.enterprise.lang.model.declarations.MethodInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class MethodConfigImpl
        extends DeclarationConfigImpl<AnnotationsOverlay.Methods.Key, org.jboss.jandex.MethodInfo, MethodConfigImpl>
        implements MethodConfig {
    MethodConfigImpl(org.jboss.jandex.IndexView jandexIndex, AllAnnotationTransformations allTransformations,
            org.jboss.jandex.MethodInfo jandexDeclaration) {
        super(jandexIndex, allTransformations, allTransformations.methods, jandexDeclaration);
    }

    @Override
    public MethodInfo info() {
        return new MethodInfoImpl(jandexIndex, allTransformations.annotationOverlays, jandexDeclaration);
    }

    @Override
    public List<ParameterConfig> parameters() {
        int params = jandexDeclaration.parameters().size();
        List<ParameterConfig> result = new ArrayList<>(params);
        for (int i = 0; i < params; i++) {
            org.jboss.jandex.MethodParameterInfo jandexParameter = org.jboss.jandex.MethodParameterInfo.create(
                    jandexDeclaration, (short) i);
            result.add(new ParameterConfigImpl(jandexIndex, allTransformations, jandexParameter));
        }
        return Collections.unmodifiableList(result);
    }
}
