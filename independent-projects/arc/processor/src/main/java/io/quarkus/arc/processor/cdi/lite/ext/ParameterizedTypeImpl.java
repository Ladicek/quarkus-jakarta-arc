package io.quarkus.arc.processor.cdi.lite.ext;

import java.util.List;
import java.util.stream.Collectors;
import javax.enterprise.lang.model.declarations.ClassInfo;
import javax.enterprise.lang.model.types.ParameterizedType;
import javax.enterprise.lang.model.types.Type;

class ParameterizedTypeImpl extends TypeImpl<org.jboss.jandex.ParameterizedType> implements ParameterizedType {
    ParameterizedTypeImpl(org.jboss.jandex.IndexView jandexIndex, AllAnnotationOverlays annotationOverlays,
            org.jboss.jandex.ParameterizedType jandexType) {
        super(jandexIndex, annotationOverlays, jandexType);
    }

    @Override
    public ClassInfo<?> declaration() {
        return new ClassInfoImpl(jandexIndex, annotationOverlays, jandexIndex.getClassByName(jandexType.name()));
    }

    @Override
    public List<Type> typeArguments() {
        return jandexType.arguments()
                .stream()
                .map(it -> TypeImpl.fromJandexType(jandexIndex, annotationOverlays, it))
                .collect(Collectors.toList());
    }
}
