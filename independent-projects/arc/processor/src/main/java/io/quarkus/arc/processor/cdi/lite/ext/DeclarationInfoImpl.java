package io.quarkus.arc.processor.cdi.lite.ext;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.enterprise.lang.model.AnnotationInfo;
import javax.enterprise.lang.model.declarations.DeclarationInfo;
import org.jboss.jandex.DotName;

// TODO all *Info subclasses have equals/hashCode, but *Config do not, and that's probably correct?
abstract class DeclarationInfoImpl<JandexDeclaration extends org.jboss.jandex.AnnotationTarget> implements DeclarationInfo {
    final org.jboss.jandex.IndexView jandexIndex;
    final AllAnnotationOverlays annotationOverlays;
    final JandexDeclaration jandexDeclaration;

    DeclarationInfoImpl(org.jboss.jandex.IndexView jandexIndex, AllAnnotationOverlays annotationOverlays,
            JandexDeclaration jandexDeclaration) {
        this.jandexIndex = jandexIndex;
        this.annotationOverlays = annotationOverlays;
        this.jandexDeclaration = jandexDeclaration;
    }

    static DeclarationInfo fromJandexDeclaration(org.jboss.jandex.IndexView jandexIndex,
            AllAnnotationOverlays annotationOverlays,
            org.jboss.jandex.AnnotationTarget jandexDeclaration) {
        switch (jandexDeclaration.kind()) {
            case CLASS:
                return new ClassInfoImpl(jandexIndex, annotationOverlays, jandexDeclaration.asClass());
            case METHOD:
                return new MethodInfoImpl(jandexIndex, annotationOverlays, jandexDeclaration.asMethod());
            case METHOD_PARAMETER:
                return new ParameterInfoImpl(jandexIndex, annotationOverlays, jandexDeclaration.asMethodParameter().method(),
                        jandexDeclaration.asMethodParameter().position());
            case FIELD:
                return new FieldInfoImpl(jandexIndex, annotationOverlays, jandexDeclaration.asField());
            default:
                throw new IllegalStateException("Unknown declaration " + jandexDeclaration);
        }
    }

    @Override
    public boolean hasAnnotation(Class<? extends Annotation> annotationType) {
        return annotationsOverlay().hasAnnotation(jandexDeclaration, DotName.createSimple(annotationType.getName()));
    }

    @Override
    public boolean hasAnnotation(Predicate<AnnotationInfo<?>> predicate) {
        return annotationsOverlay().getAnnotations(jandexDeclaration).annotations()
                .stream()
                .anyMatch(it -> predicate.test(new AnnotationInfoImpl<>(jandexIndex, annotationOverlays, it)));
    }

    @Override
    public <T extends Annotation> AnnotationInfo<T> annotation(Class<T> annotationType) {
        return new AnnotationInfoImpl<T>(jandexIndex, annotationOverlays,
                annotationsOverlay().getAnnotations(jandexDeclaration).annotation(annotationType));
    }

    @Override
    public <T extends Annotation> Collection<AnnotationInfo<T>> repeatableAnnotation(Class<T> annotationType) {
        return annotationsOverlay().getAnnotations(jandexDeclaration).annotationsWithRepeatable(annotationType, jandexIndex)
                .stream()
                .map(it -> new AnnotationInfoImpl<T>(jandexIndex, annotationOverlays, it))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<AnnotationInfo<?>> annotations(Predicate<AnnotationInfo<?>> predicate) {
        return annotationsOverlay().getAnnotations(jandexDeclaration)
                .annotations()
                .stream()
                .map(it -> new AnnotationInfoImpl<>(jandexIndex, annotationOverlays, it))
                .filter(predicate)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<AnnotationInfo<?>> annotations() {
        return annotations(it -> true);
    }

    abstract AnnotationsOverlay<?, JandexDeclaration> annotationsOverlay();

    @Override
    public String toString() {
        return jandexDeclaration.toString();
    }
}
