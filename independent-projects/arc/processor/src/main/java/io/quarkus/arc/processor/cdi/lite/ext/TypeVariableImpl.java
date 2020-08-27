package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.model.types.Type;
import cdi.lite.extension.model.types.TypeVariable;
import java.util.List;
import java.util.stream.Collectors;

class TypeVariableImpl extends TypeImpl<org.jboss.jandex.TypeVariable> implements TypeVariable {
    TypeVariableImpl(org.jboss.jandex.IndexView jandexIndex, AllAnnotationOverlays annotationOverlays,
            org.jboss.jandex.TypeVariable jandexType) {
        super(jandexIndex, annotationOverlays, jandexType);
    }

    @Override
    public String name() {
        return jandexType.identifier();
    }

    @Override
    public List<Type> bounds() {
        return jandexType.bounds()
                .stream()
                .map(it -> TypeImpl.fromJandexType(jandexIndex, annotationOverlays, it))
                .collect(Collectors.toList());
    }
}
