package io.quarkus.arc.processor.cdi.lite.ext;

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

    void freeze() {
        // not necessary, just to be explicit about the relationship
        annotationOverlays.invalidate();

        classes.freeze();
        methods.freeze();
        fields.freeze();
    }
}
