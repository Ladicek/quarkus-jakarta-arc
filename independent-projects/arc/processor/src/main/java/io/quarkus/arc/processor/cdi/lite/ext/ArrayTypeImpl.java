package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.model.types.ArrayType;
import cdi.lite.extension.model.types.Type;

class ArrayTypeImpl extends TypeImpl<org.jboss.jandex.ArrayType> implements ArrayType {
    ArrayTypeImpl(org.jboss.jandex.IndexView jandexIndex, AllAnnotationOverlays annotationOverlays,
            org.jboss.jandex.ArrayType jandexType) {
        super(jandexIndex, annotationOverlays, jandexType);
    }

    @Override
    public int dimensions() {
        return jandexType.dimensions();
    }

    @Override
    public Type componentType() {
        return TypeImpl.fromJandexType(jandexIndex, annotationOverlays, jandexType.component());
    }
}
