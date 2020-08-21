package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.declarations.MethodInfo;
import cdi.lite.extension.model.declarations.ParameterInfo;
import cdi.lite.extension.model.types.Type;
import cdi.lite.extension.model.types.TypeVariable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jboss.jandex.DotName;

class MethodInfoImpl extends DeclarationInfoImpl<org.jboss.jandex.MethodInfo> implements MethodInfo<Object> {
    // only for equals/hashCode
    private final DotName className;
    private final String name;
    private final List<org.jboss.jandex.Type> parameterTypes;

    MethodInfoImpl(org.jboss.jandex.IndexView jandexIndex, org.jboss.jandex.MethodInfo jandexDeclaration) {
        super(jandexIndex, jandexDeclaration);
        this.className = jandexDeclaration.declaringClass().name();
        this.name = jandexDeclaration.name();
        this.parameterTypes = jandexDeclaration.parameters();
    }

    @Override
    public String name() {
        return jandexDeclaration.name();
    }

    @Override
    public List<ParameterInfo<Object>> parameters() {
        int parameters = jandexDeclaration.parameters().size();
        List<ParameterInfo<Object>> result = new ArrayList<>(parameters);

        for (int i = 0; i < parameters; i++) {
            result.add(new ParameterInfoImpl(jandexIndex, jandexDeclaration, i));
        }

        return result;
    }

    @Override
    public Type returnType() {
        return TypeImpl.fromJandexType(jandexIndex, jandexDeclaration.returnType());
    }

    @Override
    public Optional<Type> receiverType() {
        return Optional.of(TypeImpl.fromJandexType(jandexIndex, jandexDeclaration.receiverType()));
    }

    @Override
    public List<Type> throwsTypes() {
        return jandexDeclaration.exceptions()
                .stream()
                .map(it -> TypeImpl.fromJandexType(jandexIndex, it))
                .collect(Collectors.toList());
    }

    @Override
    public List<TypeVariable> typeParameters() {
        return jandexDeclaration.typeParameters()
                .stream()
                .map(it -> TypeImpl.fromJandexType(jandexIndex, it))
                .filter(Type::isTypeVariable) // not necessary, just as a precaution
                .map(Type::asTypeVariable) // not necessary, just as a precaution
                .collect(Collectors.toList());
    }

    @Override
    public boolean isStatic() {
        return Modifier.isStatic(jandexDeclaration.flags());
    }

    @Override
    public boolean isAbstract() {
        return Modifier.isAbstract(jandexDeclaration.flags());
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
        return jandexDeclaration.annotations(DotName.createSimple(annotationType.getName()))
                .stream()
                .anyMatch(it -> it.target().kind() == org.jboss.jandex.AnnotationTarget.Kind.METHOD);
    }

    @Override
    public AnnotationInfo annotation(Class<? extends Annotation> annotationType) {
        return jandexDeclaration.annotations(DotName.createSimple(annotationType.getName()))
                .stream()
                .filter(it -> it.target().kind() == org.jboss.jandex.AnnotationTarget.Kind.METHOD)
                .findFirst()
                .map(it -> new AnnotationInfoImpl(jandexIndex, it))
                .get();
    }

    @Override
    public Collection<AnnotationInfo> repeatableAnnotation(Class<? extends Annotation> annotationType) {
        return jandexDeclaration.annotationsWithRepeatable(DotName.createSimple(annotationType.getName()), jandexIndex)
                .stream()
                .filter(it -> it.target().kind() == org.jboss.jandex.AnnotationTarget.Kind.METHOD)
                .map(it -> new AnnotationInfoImpl(jandexIndex, it))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<AnnotationInfo> annotations() {
        return jandexDeclaration.annotations()
                .stream()
                .filter(it -> it.target().kind() == org.jboss.jandex.AnnotationTarget.Kind.METHOD)
                .map(it -> new AnnotationInfoImpl(jandexIndex, it))
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MethodInfoImpl that = (MethodInfoImpl) o;
        return Objects.equals(className, that.className) &&
                Objects.equals(name, that.name) &&
                Objects.equals(parameterTypes, that.parameterTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, name, parameterTypes);
    }
}
