package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.model.types.Type;
import cdi.lite.extension.model.types.TypeVariable;
import java.util.Collections;
import java.util.List;

class UnresolvedTypeVariableImpl extends TypeImpl<org.jboss.jandex.UnresolvedTypeVariable> implements TypeVariable {
    UnresolvedTypeVariableImpl(org.jboss.jandex.IndexView jandexIndex, AllAnnotationOverlays annotationOverlays,
            org.jboss.jandex.UnresolvedTypeVariable jandexType) {
        super(jandexIndex, annotationOverlays, jandexType);
    }

    @Override
    public String name() {
        return jandexType.identifier();
    }

    @Override
    public List<Type> bounds() {
        return Collections.emptyList();
    }
}
