package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.model.types.ArrayType;
import cdi.lite.extension.model.types.Type;

class ArrayTypeImpl extends TypeImpl<org.jboss.jandex.ArrayType> implements ArrayType {
    ArrayTypeImpl(org.jboss.jandex.IndexView jandexIndex, org.jboss.jandex.ArrayType jandexType) {
        super(jandexIndex, jandexType);
    }

    @Override
    public int dimensions() {
        return jandexType.dimensions();
    }

    @Override
    public Type componentType() {
        return TypeImpl.fromJandexType(jandexIndex, jandexType.component());
    }
}
