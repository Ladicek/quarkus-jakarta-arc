package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.model.declarations.ClassInfo;
import cdi.lite.extension.model.types.ClassType;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;

class ClassTypeImpl extends TypeImpl<org.jboss.jandex.ClassType> implements ClassType {
    ClassTypeImpl(IndexView jandexIndex, org.jboss.jandex.ClassType jandexType) {
        super(jandexIndex, jandexType);
    }

    @Override
    public ClassInfo<?> declaration() {
        DotName name = jandexType.name();
        return new ClassInfoImpl(jandexIndex, jandexIndex.getClassByName(name));
    }
}
