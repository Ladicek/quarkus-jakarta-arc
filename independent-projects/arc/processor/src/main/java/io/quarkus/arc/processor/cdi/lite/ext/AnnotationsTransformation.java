package io.quarkus.arc.processor.cdi.lite.ext;

import jakarta.enterprise.lang.model.AnnotationInfo;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.jboss.jandex.DotName;

// this must be symmetric with AnnotationsOverlay
abstract class AnnotationsTransformation<Key, JandexDeclaration extends org.jboss.jandex.AnnotationTarget>
        implements io.quarkus.arc.processor.AnnotationsTransformer {

    final org.jboss.jandex.IndexView jandexIndex;
    final AllAnnotationOverlays annotationOverlays;

    private final org.jboss.jandex.AnnotationTarget.Kind kind;
    private final Map<Key, List<Consumer<TransformationContext>>> transformations = new HashMap<>();

    private boolean frozen = false;

    AnnotationsTransformation(org.jboss.jandex.IndexView jandexIndex, AllAnnotationOverlays annotationOverlays,
            org.jboss.jandex.AnnotationTarget.Kind kind) {
        this.jandexIndex = jandexIndex;
        this.annotationOverlays = annotationOverlays;
        this.kind = kind;
    }

    private void addAnnotation(JandexDeclaration jandexDeclaration, org.jboss.jandex.AnnotationInstance jandexAnnotation) {
        if (frozen) {
            throw new IllegalStateException("Annotations transformation frozen");
        }

        Key key = annotationsOverlay().key(jandexDeclaration);

        org.jboss.jandex.AnnotationInstance jandexAnnotationWithTarget = org.jboss.jandex.AnnotationInstance.create(
                jandexAnnotation.name(), jandexDeclaration, jandexAnnotation.values());

        annotationsOverlay().getAnnotations(jandexDeclaration, jandexIndex).add(jandexAnnotationWithTarget);

        Consumer<TransformationContext> transformation = ctx -> {
            ctx.transform().add(jandexAnnotationWithTarget).done();
        };
        transformations.computeIfAbsent(key, ignored -> new ArrayList<>()).add(transformation);
    }

    void addAnnotation(JandexDeclaration jandexDeclaration, Class<? extends Annotation> clazz) {
        org.jboss.jandex.AnnotationInstance jandexAnnotation = org.jboss.jandex.AnnotationInstance.create(
                DotName.createSimple(clazz.getName()), null, AnnotationValueArray.EMPTY);

        addAnnotation(jandexDeclaration, jandexAnnotation);
    }

    void addAnnotation(JandexDeclaration jandexDeclaration, AnnotationInfo annotation) {
        addAnnotation(jandexDeclaration, ((AnnotationInfoImpl) annotation).jandexAnnotation);
    }

    void addAnnotation(JandexDeclaration jandexDeclaration, Annotation annotation) {
        addAnnotation(jandexDeclaration, AnnotationsReflection.jandexAnnotation(annotation));
    }

    private void removeMatchingAnnotations(JandexDeclaration declaration,
            Predicate<org.jboss.jandex.AnnotationInstance> predicate) {

        if (frozen) {
            throw new IllegalStateException("Annotations transformation frozen");
        }

        Key key = annotationsOverlay().key(declaration);

        annotationsOverlay().getAnnotations(declaration, jandexIndex).removeIf(predicate);

        Consumer<TransformationContext> transformation = ctx -> {
            ctx.transform().remove(predicate).done();
        };
        transformations.computeIfAbsent(key, ignored -> new ArrayList<>()).add(transformation);
    }

    void removeAnnotation(JandexDeclaration declaration, Predicate<AnnotationInfo> predicate) {
        Key key = annotationsOverlay().key(declaration);

        removeMatchingAnnotations(declaration, new Predicate<org.jboss.jandex.AnnotationInstance>() {
            @Override
            public boolean test(org.jboss.jandex.AnnotationInstance jandexAnnotation) {
                // we only verify the target here because ArC doesn't support annotation transformation
                // on method parameters directly; instead, it must be implemented indirectly by transforming
                // annotations on the _method_
                return key.equals(annotationOverlays.key(jandexAnnotation.target()))
                        && predicate.test(new AnnotationInfoImpl(jandexIndex, annotationOverlays, jandexAnnotation));
            }
        });
    }

    void removeAllAnnotations(JandexDeclaration declaration) {
        removeAnnotation(declaration, ignored -> true);
    }

    void freeze() {
        frozen = true;
    }

    // `appliesTo` and `transform` must be overridden for `Parameters`, because ArC doesn't
    // support annotation transformation on method parameters directly; instead, it must be
    // implemented indirectly by transforming annotations on the _method_ (and setting proper
    // annotation target)

    @Override
    public boolean appliesTo(org.jboss.jandex.AnnotationTarget.Kind kind) {
        return this.kind == kind;
    }

    @Override
    public void transform(TransformationContext ctx) {
        JandexDeclaration jandexDeclaration = targetJandexDeclaration(ctx);
        Key key = annotationsOverlay().key(jandexDeclaration);
        transformations.getOrDefault(key, Collections.emptyList())
                .forEach(it -> it.accept(ctx));
    }

    abstract JandexDeclaration targetJandexDeclaration(TransformationContext ctx);

    abstract AnnotationsOverlay<Key, JandexDeclaration> annotationsOverlay();

    static class Classes extends AnnotationsTransformation<DotName, org.jboss.jandex.ClassInfo> {
        Classes(org.jboss.jandex.IndexView jandexIndex, AllAnnotationOverlays annotationOverlays) {
            super(jandexIndex, annotationOverlays, org.jboss.jandex.AnnotationTarget.Kind.CLASS);
        }

        @Override
        protected org.jboss.jandex.ClassInfo targetJandexDeclaration(
                io.quarkus.arc.processor.AnnotationsTransformer.TransformationContext ctx) {
            return ctx.getTarget().asClass();
        }

        @Override
        AnnotationsOverlay<DotName, org.jboss.jandex.ClassInfo> annotationsOverlay() {
            return annotationOverlays.classes;
        }
    }

    static class Methods extends AnnotationsTransformation<AnnotationsOverlay.Methods.Key, org.jboss.jandex.MethodInfo> {
        Methods(org.jboss.jandex.IndexView jandexIndex, AllAnnotationOverlays annotationOverlays) {
            super(jandexIndex, annotationOverlays, org.jboss.jandex.AnnotationTarget.Kind.METHOD);
        }

        @Override
        protected org.jboss.jandex.MethodInfo targetJandexDeclaration(
                io.quarkus.arc.processor.AnnotationsTransformer.TransformationContext ctx) {
            return ctx.getTarget().asMethod();
        }

        @Override
        AnnotationsOverlay<AnnotationsOverlay.Methods.Key, org.jboss.jandex.MethodInfo> annotationsOverlay() {
            return annotationOverlays.methods;
        }
    }

    static class Parameters
            extends AnnotationsTransformation<AnnotationsOverlay.Parameters.Key, org.jboss.jandex.MethodParameterInfo> {
        Parameters(org.jboss.jandex.IndexView jandexIndex, AllAnnotationOverlays annotationOverlays) {
            super(jandexIndex, annotationOverlays, org.jboss.jandex.AnnotationTarget.Kind.METHOD_PARAMETER);
        }

        @Override
        protected org.jboss.jandex.MethodParameterInfo targetJandexDeclaration(
                io.quarkus.arc.processor.AnnotationsTransformer.TransformationContext ctx) {
            // `targetJandexDeclaration` is only called from `super.transform`, which we override here
            throw new UnsupportedOperationException();
        }

        @Override
        AnnotationsOverlay<AnnotationsOverlay.Parameters.Key, org.jboss.jandex.MethodParameterInfo> annotationsOverlay() {
            return annotationOverlays.parameters;
        }

        @Override
        public boolean appliesTo(org.jboss.jandex.AnnotationTarget.Kind kind) {
            return org.jboss.jandex.AnnotationTarget.Kind.METHOD == kind;
        }

        @Override
        public void transform(TransformationContext ctx) {
            org.jboss.jandex.MethodInfo jandexMethod = ctx.getTarget().asMethod();
            List<org.jboss.jandex.Type> jandexMethodParameters = jandexMethod.parameters();
            for (int i = 0; i < jandexMethodParameters.size(); i++) {
                org.jboss.jandex.MethodParameterInfo jandexDeclaration = org.jboss.jandex.MethodParameterInfo.create(
                        jandexMethod, (short) i);
                AnnotationsOverlay.Parameters.Key key = annotationsOverlay().key(jandexDeclaration);
                super.transformations.getOrDefault(key, Collections.emptyList())
                        .forEach(it -> it.accept(ctx));
            }
        }
    }

    static class Fields extends AnnotationsTransformation<AnnotationsOverlay.Fields.Key, org.jboss.jandex.FieldInfo> {
        Fields(org.jboss.jandex.IndexView jandexIndex, AllAnnotationOverlays annotationOverlays) {
            super(jandexIndex, annotationOverlays, org.jboss.jandex.AnnotationTarget.Kind.FIELD);
        }

        @Override
        protected org.jboss.jandex.FieldInfo targetJandexDeclaration(
                io.quarkus.arc.processor.AnnotationsTransformer.TransformationContext ctx) {
            return ctx.getTarget().asField();
        }

        @Override
        AnnotationsOverlay<AnnotationsOverlay.Fields.Key, org.jboss.jandex.FieldInfo> annotationsOverlay() {
            return annotationOverlays.fields;
        }
    }
}
