package io.quarkus.arc.processor.cdi.lite.ext;

import java.util.Collection;
import java.util.stream.Collectors;
import javax.enterprise.inject.build.compatible.spi.InjectionPointInfo;
import javax.enterprise.lang.model.AnnotationInfo;
import javax.enterprise.lang.model.declarations.DeclarationInfo;
import javax.enterprise.lang.model.types.Type;

class InjectionPointInfoImpl implements InjectionPointInfo {
    private final org.jboss.jandex.IndexView jandexIndex;
    private final AllAnnotationOverlays annotationOverlays;
    private final io.quarkus.arc.processor.InjectionPointInfo arcInjectionPointInfo;

    InjectionPointInfoImpl(org.jboss.jandex.IndexView jandexIndex, AllAnnotationOverlays annotationOverlays,
            io.quarkus.arc.processor.InjectionPointInfo arcInjectionPointInfo) {
        this.jandexIndex = jandexIndex;
        this.annotationOverlays = annotationOverlays;
        this.arcInjectionPointInfo = arcInjectionPointInfo;
    }

    @Override
    public Type type() {
        return TypeImpl.fromJandexType(jandexIndex, annotationOverlays, arcInjectionPointInfo.getRequiredType());
    }

    @Override
    public Collection<AnnotationInfo> qualifiers() {
        return arcInjectionPointInfo.getRequiredQualifiers()
                .stream()
                .map(it -> new AnnotationInfoImpl(jandexIndex, annotationOverlays, it))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public DeclarationInfo declaration() {
        if (arcInjectionPointInfo.isField()) {
            org.jboss.jandex.FieldInfo jandexField = arcInjectionPointInfo.getTarget().asField();
            return new FieldInfoImpl(jandexIndex, annotationOverlays, jandexField);
        } else if (arcInjectionPointInfo.isParam()) {
            org.jboss.jandex.MethodInfo jandexMethod = arcInjectionPointInfo.getTarget().asMethod();
            int parameterPosition = arcInjectionPointInfo.getPosition();
            org.jboss.jandex.MethodParameterInfo jandexParameter = org.jboss.jandex.MethodParameterInfo.create(
                    jandexMethod, (short) parameterPosition);
            return new ParameterInfoImpl(jandexIndex, annotationOverlays, jandexParameter);
        } else {
            throw new IllegalStateException("Unknown injection point: " + arcInjectionPointInfo);
        }
    }
}
