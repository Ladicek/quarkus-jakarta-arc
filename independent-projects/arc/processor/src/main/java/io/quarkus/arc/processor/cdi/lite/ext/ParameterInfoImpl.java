package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.declarations.ParameterInfo;
import cdi.lite.extension.model.types.Type;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jboss.jandex.DotName;

class ParameterInfoImpl extends DeclarationInfoImpl<org.jboss.jandex.MethodInfo> implements ParameterInfo<Object> {
    // only for equals/hashCode
    private final MethodInfoImpl method;
    private final int position;

    ParameterInfoImpl(org.jboss.jandex.IndexView jandexIndex, org.jboss.jandex.MethodInfo jandexDeclaration, int position) {
        super(jandexIndex, jandexDeclaration);
        this.method = new MethodInfoImpl(jandexIndex, jandexDeclaration);
        this.position = position;
    }

    @Override
    public String name() {
        return jandexDeclaration.parameterName(position);
    }

    @Override
    public Type type() {
        return TypeImpl.fromJandexType(jandexIndex, jandexDeclaration.parameters().get(position));
    }

    @Override
    public String toString() {
        String name = name();
        return "parameter " + (name != null ? name : position) + " of method " + jandexDeclaration;
    }

    @Override
    public boolean hasAnnotation(Class<? extends Annotation> annotationType) {
        return jandexDeclaration.annotations(DotName.createSimple(annotationType.getName()))
                .stream()
                .anyMatch(it -> it.target().kind() == org.jboss.jandex.AnnotationTarget.Kind.METHOD_PARAMETER
                        && it.target().asMethodParameter().position() == position);
    }

    @Override
    public AnnotationInfo annotation(Class<? extends Annotation> annotationType) {
        return jandexDeclaration
                .annotations(DotName.createSimple(annotationType.getName()))
                .stream()
                .filter(it -> it.target().kind() == org.jboss.jandex.AnnotationTarget.Kind.METHOD_PARAMETER
                        && it.target().asMethodParameter().position() == position)
                .findFirst()
                .map(it -> new AnnotationInfoImpl(jandexIndex, it))
                .get();
    }

    @Override
    public Collection<AnnotationInfo> repeatableAnnotation(Class<? extends Annotation> annotationType) {
        return jandexDeclaration.annotationsWithRepeatable(DotName.createSimple(annotationType.getName()), jandexIndex)
                .stream()
                .filter(it -> it.target().kind() == org.jboss.jandex.AnnotationTarget.Kind.METHOD_PARAMETER
                        && it.target().asMethodParameter().position() == position)
                .map(it -> new AnnotationInfoImpl(jandexIndex, it))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<AnnotationInfo> annotations() {
        return jandexDeclaration
                .annotations()
                .stream()
                .filter(it -> it.target().kind() == org.jboss.jandex.AnnotationTarget.Kind.METHOD_PARAMETER
                        && it.target().asMethodParameter().position() == position)
                .map(it -> new AnnotationInfoImpl(jandexIndex, it))
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ParameterInfoImpl that = (ParameterInfoImpl) o;
        return position == that.position &&
                Objects.equals(method, that.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, position);
    }
}
