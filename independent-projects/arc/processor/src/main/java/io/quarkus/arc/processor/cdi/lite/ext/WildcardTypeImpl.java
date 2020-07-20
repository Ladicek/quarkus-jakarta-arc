package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.model.types.Type;
import cdi.lite.extension.model.types.WildcardType;
import java.util.Optional;

class WildcardTypeImpl extends TypeImpl<org.jboss.jandex.WildcardType> implements WildcardType {
    private final boolean hasUpperBound;

    WildcardTypeImpl(org.jboss.jandex.IndexView jandexIndex, org.jboss.jandex.WildcardType jandexType) {
        super(jandexIndex, jandexType);
        this.hasUpperBound = jandexType.superBound() == null;
    }

    @Override
    public Optional<Type> upperBound() {
        return hasUpperBound
                ? Optional.of(TypeImpl.fromJandexType(jandexIndex, jandexType.extendsBound()))
                : Optional.empty();
    }

    @Override
    public Optional<Type> lowerBound() {
        return hasUpperBound
                ? Optional.empty()
                : Optional.of(TypeImpl.fromJandexType(jandexIndex, jandexType.superBound()));
    }
}
