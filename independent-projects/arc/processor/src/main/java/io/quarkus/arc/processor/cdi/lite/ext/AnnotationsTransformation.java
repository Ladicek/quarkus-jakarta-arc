package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.model.AnnotationAttribute;
import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.declarations.ClassInfo;
import io.quarkus.arc.processor.AnnotationsTransformer;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.jboss.jandex.DotName;

// this must be symmetric with AnnotationsOverlay
abstract class AnnotationsTransformation<Key, JandexDeclaration extends org.jboss.jandex.AnnotationTarget>
        implements AnnotationsTransformer {

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

        annotationsOverlay().getAnnotations(jandexDeclaration).add(jandexAnnotation);

        Consumer<TransformationContext> transformation = ctx -> {
            ctx.transform().add(jandexAnnotation).done();
        };
        transformations.computeIfAbsent(key, ignored -> new ArrayList<>()).add(transformation);
    }

    private void addAnnotation(JandexDeclaration jandexDeclaration, DotName name, AnnotationAttribute[] attributes) {
        org.jboss.jandex.AnnotationValue[] jandexAnnotationAttributes = Arrays.stream(attributes)
                .map(it -> ((AnnotationAttributeImpl) it).jandexAnnotationAttribute)
                .toArray(org.jboss.jandex.AnnotationValue[]::new);
        org.jboss.jandex.AnnotationInstance jandexAnnotation = org.jboss.jandex.AnnotationInstance.create(name,
                null, jandexAnnotationAttributes);

        addAnnotation(jandexDeclaration, jandexAnnotation);
    }

    void addAnnotation(JandexDeclaration jandexDeclaration, Class<? extends Annotation> clazz,
            AnnotationAttribute... attributes) {
        DotName name = DotName.createSimple(clazz.getName());
        addAnnotation(jandexDeclaration, name, attributes);
    }

    void addAnnotation(JandexDeclaration jandexDeclaration, ClassInfo<?> clazz, AnnotationAttribute... attributes) {
        DotName name = ((ClassInfoImpl) clazz).jandexDeclaration.name();
        addAnnotation(jandexDeclaration, name, attributes);
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

        annotationsOverlay().getAnnotations(declaration).removeIf(predicate);

        Consumer<TransformationContext> transformation = ctx -> {
            ctx.transform().remove(predicate).done();
        };
        transformations.computeIfAbsent(key, ignored -> new ArrayList<>()).add(transformation);
    }

    void removeAnnotation(JandexDeclaration declaration, Predicate<AnnotationInfo> predicate) {
        removeMatchingAnnotations(declaration, new Predicate<org.jboss.jandex.AnnotationInstance>() {
            @Override
            public boolean test(org.jboss.jandex.AnnotationInstance jandexAnnotation) {
                return predicate.test(new AnnotationInfoImpl(jandexIndex, annotationOverlays, jandexAnnotation));
            }
        });
    }

    void removeAllAnnotations(JandexDeclaration declaration) {
        removeMatchingAnnotations(declaration, ignored -> true);
    }

    void freeze() {
        // not necessary, just to be explicit about the relationship
        annotationsOverlay().invalidate();
        frozen = true;
    }

    @Override
    public boolean appliesTo(org.jboss.jandex.AnnotationTarget.Kind kind) {
        return this.kind == kind;
    }

    @Override
    public void transform(TransformationContext ctx) {
        JandexDeclaration jandexDeclaration = transformedJandexDeclaration(ctx);
        Key key = annotationsOverlay().key(jandexDeclaration);
        transformations.getOrDefault(key, Collections.emptyList())
                .forEach(it -> it.accept(ctx));
    }

    abstract JandexDeclaration transformedJandexDeclaration(TransformationContext ctx);

    abstract AnnotationsOverlay<Key, JandexDeclaration> annotationsOverlay();

    static class Classes extends AnnotationsTransformation<DotName, org.jboss.jandex.ClassInfo> {
        Classes(org.jboss.jandex.IndexView jandexIndex, AllAnnotationOverlays annotationOverlays) {
            super(jandexIndex, annotationOverlays, org.jboss.jandex.AnnotationTarget.Kind.CLASS);
        }

        @Override
        protected org.jboss.jandex.ClassInfo transformedJandexDeclaration(AnnotationsTransformer.TransformationContext ctx) {
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
        protected org.jboss.jandex.MethodInfo transformedJandexDeclaration(AnnotationsTransformer.TransformationContext ctx) {
            return ctx.getTarget().asMethod();
        }

        @Override
        AnnotationsOverlay<AnnotationsOverlay.Methods.Key, org.jboss.jandex.MethodInfo> annotationsOverlay() {
            return annotationOverlays.methods;
        }
    }

    static class Fields extends AnnotationsTransformation<AnnotationsOverlay.Fields.Key, org.jboss.jandex.FieldInfo> {
        Fields(org.jboss.jandex.IndexView jandexIndex, AllAnnotationOverlays annotationOverlays) {
            super(jandexIndex, annotationOverlays, org.jboss.jandex.AnnotationTarget.Kind.FIELD);
        }

        @Override
        protected org.jboss.jandex.FieldInfo transformedJandexDeclaration(AnnotationsTransformer.TransformationContext ctx) {
            return ctx.getTarget().asField();
        }

        @Override
        AnnotationsOverlay<AnnotationsOverlay.Fields.Key, org.jboss.jandex.FieldInfo> annotationsOverlay() {
            return annotationOverlays.fields;
        }
    }
}
