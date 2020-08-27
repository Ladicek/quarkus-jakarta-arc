package io.quarkus.arc.processor.cdi.lite.ext;

import io.quarkus.arc.processor.BeanProcessor;

class AllAnnotationTransformations {
    final AllAnnotationOverlays annotationOverlays;
    final AnnotationsTransformation.Classes classes;
    final AnnotationsTransformation.Methods methods;
    final AnnotationsTransformation.Fields fields;

    AllAnnotationTransformations(org.jboss.jandex.IndexView jandexIndex, AllAnnotationOverlays annotationOverlays) {
        this.annotationOverlays = annotationOverlays;
        classes = new AnnotationsTransformation.Classes(jandexIndex, annotationOverlays);
        methods = new AnnotationsTransformation.Methods(jandexIndex, annotationOverlays);
        fields = new AnnotationsTransformation.Fields(jandexIndex, annotationOverlays);
    }

    void register(BeanProcessor.Builder builder) {
        builder.addAnnotationTransformer(classes);
        builder.addAnnotationTransformer(methods);
        builder.addAnnotationTransformer(fields);
    }

    void freeze() {
        // not necessary, just to be explicit about the relationship
        annotationOverlays.invalidate();

        classes.freeze();
        methods.freeze();
        fields.freeze();
    }
}
