package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.model.declarations.ClassInfo;
import cdi.lite.extension.model.types.ParameterizedType;
import cdi.lite.extension.model.types.Type;
import java.util.List;
import java.util.stream.Collectors;

class ParameterizedTypeImpl extends TypeImpl<org.jboss.jandex.ParameterizedType> implements ParameterizedType {
    ParameterizedTypeImpl(org.jboss.jandex.IndexView jandexIndex, org.jboss.jandex.ParameterizedType jandexType) {
        super(jandexIndex, jandexType);
    }

    @Override
    public ClassInfo<?> declaration() {
        return new ClassInfoImpl(jandexIndex, jandexIndex.getClassByName(jandexType.name()));
    }

    @Override
    public List<Type> typeArguments() {
        return jandexType.arguments()
                .stream()
                .map(it -> TypeImpl.fromJandexType(jandexIndex, it))
                .collect(Collectors.toList());
    }
}
