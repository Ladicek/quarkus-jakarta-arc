package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.declarations.ParameterInfo;
import cdi.lite.extension.model.types.Type;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

class ParameterInfoImpl extends DeclarationInfoImpl<org.jboss.jandex.MethodInfo> implements ParameterInfo {
    // only for equals/hashCode
    private final MethodInfoImpl method;
    private final int position;

    private AnnotationSet annotationSet;

    ParameterInfoImpl(org.jboss.jandex.IndexView jandexIndex, AllAnnotationOverlays annotationOverlays,
            org.jboss.jandex.MethodInfo jandexDeclaration, int position) {
        super(jandexIndex, annotationOverlays, jandexDeclaration);
        this.method = new MethodInfoImpl(jandexIndex, annotationOverlays, jandexDeclaration);
        this.position = position;
    }

    @Override
    public String name() {
        return jandexDeclaration.parameterName(position);
    }

    @Override
    public Type type() {
        return TypeImpl.fromJandexType(jandexIndex, annotationOverlays, jandexDeclaration.parameters().get(position));
    }

    @Override
    public String toString() {
        String name = name();
        return "parameter " + (name != null ? name : position) + " of method " + jandexDeclaration;
    }

    private AnnotationSet annotationSet() {
        if (annotationSet == null) {
            Set<org.jboss.jandex.AnnotationInstance> jandexAnnotations = jandexDeclaration.annotations()
                    .stream()
                    .filter(it -> it.target().kind() == org.jboss.jandex.AnnotationTarget.Kind.METHOD_PARAMETER
                            && it.target().asMethodParameter().position() == position)
                    .collect(Collectors.toSet());
            annotationSet = new AnnotationSet(jandexAnnotations);
        }

        return annotationSet;
    }

    @Override
    public boolean hasAnnotation(Class<? extends Annotation> annotationType) {
        return annotationSet().hasAnnotation(annotationType);
    }

    @Override
    public AnnotationInfo annotation(Class<? extends Annotation> annotationType) {
        return new AnnotationInfoImpl(jandexIndex, annotationOverlays, annotationSet().annotation(annotationType));
    }

    @Override
    public Collection<AnnotationInfo> repeatableAnnotation(Class<? extends Annotation> annotationType) {
        return annotationSet().annotationsWithRepeatable(annotationType, jandexIndex)
                .stream()
                .map(it -> new AnnotationInfoImpl(jandexIndex, annotationOverlays, it))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<AnnotationInfo> annotations() {
        return annotationSet().annotations()
                .stream()
                .map(it -> new AnnotationInfoImpl(jandexIndex, annotationOverlays, it))
                .collect(Collectors.toList());
    }

    @Override
    AnnotationsOverlay<?, org.jboss.jandex.MethodInfo> annotationsOverlay() {
        throw new IllegalStateException("No annotations overlay for parameters");
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
