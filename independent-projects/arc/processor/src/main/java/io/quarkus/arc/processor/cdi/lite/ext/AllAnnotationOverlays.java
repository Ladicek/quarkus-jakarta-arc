package io.quarkus.arc.processor.cdi.lite.ext;

class AllAnnotationOverlays {
    final AnnotationsOverlay.Classes classes;
    final AnnotationsOverlay.Methods methods;
    final AnnotationsOverlay.Fields fields;

    AllAnnotationOverlays() {
        classes = new AnnotationsOverlay.Classes();
        methods = new AnnotationsOverlay.Methods();
        fields = new AnnotationsOverlay.Fields();
    }

    void invalidate() {
        classes.invalidate();
        methods.invalidate();
        fields.invalidate();
    }
}
