package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.beans.InjectionPointInfo;
import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.declarations.DeclarationInfo;
import cdi.lite.extension.model.types.Type;
import java.util.Collection;
import java.util.stream.Collectors;
import org.jboss.jandex.IndexView;

class InjectionPointInfoImpl implements InjectionPointInfo {
    private final org.jboss.jandex.IndexView jandexIndex;
    private final AllAnnotationOverlays annotationOverlays;
    private final io.quarkus.arc.processor.InjectionPointInfo arcInjectionPointInfo;

    InjectionPointInfoImpl(IndexView jandexIndex, AllAnnotationOverlays annotationOverlays,
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
                .collect(Collectors.toList());
    }

    @Override
    public DeclarationInfo declaration() {
        if (arcInjectionPointInfo.isField()) {
            org.jboss.jandex.FieldInfo jandexField = arcInjectionPointInfo.getTarget().asField();
            return new FieldInfoImpl(jandexIndex, annotationOverlays, jandexField);
        } else if (arcInjectionPointInfo.isParam()) {
            org.jboss.jandex.MethodInfo jandexMethod = arcInjectionPointInfo.getTarget().asMethod();
            int parameterPosition = arcInjectionPointInfo.getPosition();
            return new ParameterInfoImpl(jandexIndex, annotationOverlays, jandexMethod, parameterPosition);
        } else {
            throw new IllegalStateException("Unknown injection point: " + arcInjectionPointInfo);
        }
    }
}
