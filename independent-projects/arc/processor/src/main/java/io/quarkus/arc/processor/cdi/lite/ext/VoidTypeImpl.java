package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.model.types.VoidType;

class VoidTypeImpl extends TypeImpl<org.jboss.jandex.VoidType> implements VoidType {
    VoidTypeImpl(org.jboss.jandex.IndexView jandexIndex, org.jboss.jandex.VoidType jandexType) {
        super(jandexIndex, jandexType);
    }

    @Override
    public String name() {
        return jandexType.name().toString();
    }
}
