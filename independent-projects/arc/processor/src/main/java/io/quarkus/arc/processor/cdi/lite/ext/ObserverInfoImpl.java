package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.beans.BeanInfo;
import cdi.lite.extension.beans.ObserverInfo;
import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.declarations.ClassInfo;
import cdi.lite.extension.model.declarations.MethodInfo;
import cdi.lite.extension.model.declarations.ParameterInfo;
import cdi.lite.extension.model.types.Type;
import java.util.Collection;
import java.util.stream.Collectors;
import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;
import org.jboss.jandex.IndexView;

class ObserverInfoImpl implements ObserverInfo<Object> {
    private final IndexView jandexIndex;
    private final AllAnnotationOverlays annotationOverlays;
    private final io.quarkus.arc.processor.ObserverInfo arcObserverInfo;

    ObserverInfoImpl(IndexView jandexIndex, AllAnnotationOverlays annotationOverlays,
            io.quarkus.arc.processor.ObserverInfo arcObserverInfo) {
        this.jandexIndex = jandexIndex;
        this.annotationOverlays = annotationOverlays;
        this.arcObserverInfo = arcObserverInfo;
    }

    @Override
    public String id() {
        return arcObserverInfo.getId();
    }

    @Override
    public ClassInfo<?> declaringClass() {
        org.jboss.jandex.ClassInfo jandexClass = jandexIndex.getClassByName(arcObserverInfo.getBeanClass());
        return new ClassInfoImpl(jandexIndex, annotationOverlays, jandexClass);
    }

    @Override
    public MethodInfo<?> observerMethod() {
        return new MethodInfoImpl(jandexIndex, annotationOverlays, arcObserverInfo.getObserverMethod());
    }

    @Override
    public ParameterInfo eventParameter() {
        org.jboss.jandex.MethodParameterInfo jandexParameter = arcObserverInfo.getEventParameter();
        return new ParameterInfoImpl(jandexIndex, annotationOverlays, jandexParameter.method(), jandexParameter.position());
    }

    @Override
    public BeanInfo<?> bean() {
        return null;
    }

    @Override
    public Type observedType() {
        return TypeImpl.fromJandexType(jandexIndex, annotationOverlays, arcObserverInfo.getObservedType());
    }

    @Override
    public Collection<AnnotationInfo> qualifiers() {
        return arcObserverInfo.getQualifiers()
                .stream()
                .map(it -> new AnnotationInfoImpl(jandexIndex, annotationOverlays, it))
                .collect(Collectors.toList());
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
