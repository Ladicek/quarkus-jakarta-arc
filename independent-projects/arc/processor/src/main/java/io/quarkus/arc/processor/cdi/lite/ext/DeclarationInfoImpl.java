package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.declarations.DeclarationInfo;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.stream.Collectors;

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
        return annotationsOverlay().getAnnotations(jandexDeclaration).hasAnnotation(annotationType);
    }

    @Override
    public AnnotationInfo annotation(Class<? extends Annotation> annotationType) {
        return new AnnotationInfoImpl(jandexIndex, annotationOverlays,
                annotationsOverlay().getAnnotations(jandexDeclaration).annotation(annotationType));
    }

    @Override
    public Collection<AnnotationInfo> repeatableAnnotation(Class<? extends Annotation> annotationType) {
        return annotationsOverlay().getAnnotations(jandexDeclaration).annotationsWithRepeatable(annotationType, jandexIndex)
                .stream()
                .map(it -> new AnnotationInfoImpl(jandexIndex, annotationOverlays, it))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<AnnotationInfo> annotations() {
        return annotationsOverlay().getAnnotations(jandexDeclaration).annotations()
                .stream()
                .map(it -> new AnnotationInfoImpl(jandexIndex, annotationOverlays, it))
                .collect(Collectors.toList());
    }

    abstract AnnotationsOverlay<?, JandexDeclaration> annotationsOverlay();

    @Override
    public String toString() {
        return jandexDeclaration.toString();
    }
}
