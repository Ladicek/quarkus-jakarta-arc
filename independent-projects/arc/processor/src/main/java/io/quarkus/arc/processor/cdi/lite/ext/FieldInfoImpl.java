package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.declarations.FieldInfo;
import cdi.lite.extension.model.types.Type;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.stream.Collectors;
import org.jboss.jandex.DotName;

class FieldInfoImpl extends DeclarationInfoImpl<org.jboss.jandex.FieldInfo> implements FieldInfo<Object> {
    FieldInfoImpl(org.jboss.jandex.IndexView jandexIndex, org.jboss.jandex.FieldInfo jandexDeclaration) {
        super(jandexIndex, jandexDeclaration);
    }

    @Override
    public String name() {
        return jandexDeclaration.name();
    }

    @Override
    public Type type() {
        return TypeImpl.fromJandexType(jandexIndex, jandexDeclaration.type());
    }

    @Override
    public boolean isStatic() {
        return Modifier.isStatic(jandexDeclaration.flags());
    }

    @Override
    public boolean isFinal() {
        return Modifier.isFinal(jandexDeclaration.flags());
    }

    @Override
    public int modifiers() {
        return jandexDeclaration.flags();
    }

    @Override
    public boolean hasAnnotation(Class<? extends Annotation> annotationType) {
        return jandexDeclaration.annotation(DotName.createSimple(annotationType.getName())) != null;
    }

    @Override
    public AnnotationInfo annotation(Class<? extends Annotation> annotationType) {
        // TODO null
        return new AnnotationInfoImpl(jandexIndex,
                jandexDeclaration.annotation(DotName.createSimple(annotationType.getName())));
    }

    @Override
    public Collection<AnnotationInfo> repeatableAnnotation(Class<? extends Annotation> annotationType) {
        return jandexDeclaration.annotationsWithRepeatable(DotName.createSimple(annotationType.getName()), jandexIndex)
                .stream()
                .map(it -> new AnnotationInfoImpl(jandexIndex, it))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<AnnotationInfo> annotations() {
        return jandexDeclaration.annotations()
                .stream()
                .map(it -> new AnnotationInfoImpl(jandexIndex, it))
                .collect(Collectors.toList());
    }
}
