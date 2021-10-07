package io.quarkus.arc.processor.cdi.lite.ext;

import javax.enterprise.lang.model.types.Type;
import javax.enterprise.lang.model.types.WildcardType;

class WildcardTypeImpl extends TypeImpl<org.jboss.jandex.WildcardType> implements WildcardType {
    private final boolean hasUpperBound;

    WildcardTypeImpl(org.jboss.jandex.IndexView jandexIndex, AllAnnotationOverlays annotationOverlays,
            org.jboss.jandex.WildcardType jandexType) {
        super(jandexIndex, annotationOverlays, jandexType);
        this.hasUpperBound = jandexType.superBound() == null;
    }

    @Override
    public Type upperBound() {
        if (!hasUpperBound) {
            return null;
        }
        return TypeImpl.fromJandexType(jandexIndex, annotationOverlays, jandexType.extendsBound());
    }

    @Override
    public Type lowerBound() {
        if (hasUpperBound) {
            return null;
        }
        return TypeImpl.fromJandexType(jandexIndex, annotationOverlays, jandexType.superBound());
    }
}
