package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.model.types.VoidType;

class VoidTypeImpl extends TypeImpl<org.jboss.jandex.VoidType> implements VoidType {
    VoidTypeImpl(org.jboss.jandex.IndexView jandexIndex, AllAnnotationOverlays annotationOverlays,
            org.jboss.jandex.VoidType jandexType) {
        super(jandexIndex, annotationOverlays, jandexType);
    }

    @Override
    public String name() {
        return jandexType.name().toString();
    }
}
