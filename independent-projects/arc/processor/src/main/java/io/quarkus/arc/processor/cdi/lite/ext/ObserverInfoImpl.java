package io.quarkus.arc.processor.cdi.lite.ext;

import java.util.Collection;
import java.util.stream.Collectors;
import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.build.compatible.spi.BeanInfo;
import javax.enterprise.inject.build.compatible.spi.ObserverInfo;
import javax.enterprise.lang.model.AnnotationInfo;
import javax.enterprise.lang.model.declarations.ClassInfo;
import javax.enterprise.lang.model.declarations.MethodInfo;
import javax.enterprise.lang.model.declarations.ParameterInfo;
import javax.enterprise.lang.model.types.Type;

class ObserverInfoImpl implements ObserverInfo {
    private final org.jboss.jandex.IndexView jandexIndex;
    private final AllAnnotationOverlays annotationOverlays;
    private final io.quarkus.arc.processor.ObserverInfo arcObserverInfo;

    ObserverInfoImpl(org.jboss.jandex.IndexView jandexIndex, AllAnnotationOverlays annotationOverlays,
            io.quarkus.arc.processor.ObserverInfo arcObserverInfo) {
        this.jandexIndex = jandexIndex;
        this.annotationOverlays = annotationOverlays;
        this.arcObserverInfo = arcObserverInfo;
    }

    @Override
    public Type eventType() {
        return TypeImpl.fromJandexType(jandexIndex, annotationOverlays, arcObserverInfo.getObservedType());
    }

    @Override
    public Collection<AnnotationInfo> qualifiers() {
        return arcObserverInfo.getQualifiers()
                .stream()
                .map(it -> new AnnotationInfoImpl(jandexIndex, annotationOverlays, it))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public ClassInfo declaringClass() {
        org.jboss.jandex.ClassInfo jandexClass = jandexIndex.getClassByName(arcObserverInfo.getBeanClass());
        return new ClassInfoImpl(jandexIndex, annotationOverlays, jandexClass);
    }

    @Override
    public MethodInfo observerMethod() {
        if (arcObserverInfo.isSynthetic()) {
            return null;
        }
        return new MethodInfoImpl(jandexIndex, annotationOverlays, arcObserverInfo.getObserverMethod());
    }

    @Override
    public ParameterInfo eventParameter() {
        if (arcObserverInfo.isSynthetic()) {
            return null;
        }
        org.jboss.jandex.MethodParameterInfo jandexParameter = arcObserverInfo.getEventParameter();
        return new ParameterInfoImpl(jandexIndex, annotationOverlays, jandexParameter);
    }

    @Override
    public BeanInfo bean() {
        if (arcObserverInfo.isSynthetic()) {
            return null;
        }
        return BeanInfoImpl.create(jandexIndex, annotationOverlays, arcObserverInfo.getDeclaringBean());
    }

    @Override
    public boolean isSynthetic() {
        return arcObserverInfo.isSynthetic();
    }

    @Override
    public int priority() {
        return arcObserverInfo.getPriority();
    }

    @Override
    public boolean isAsync() {
        return arcObserverInfo.isAsync();
    }

    @Override
    public Reception reception() {
        return arcObserverInfo.getReception();
    }

    @Override
    public TransactionPhase transactionPhase() {
        return arcObserverInfo.getTransactionPhase();
    }

    @Override
    public String toString() {
        return arcObserverInfo.toString();
    }
}
