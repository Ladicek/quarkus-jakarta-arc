package io.quarkus.arc.processor.cdi.lite.ext;

class AllAnnotationOverlays {
    final AnnotationsOverlay.Classes classes;
    final AnnotationsOverlay.Methods methods;
    final AnnotationsOverlay.Parameters parameters;
    final AnnotationsOverlay.Fields fields;

    AllAnnotationOverlays() {
        classes = new AnnotationsOverlay.Classes();
        methods = new AnnotationsOverlay.Methods();
        parameters = new AnnotationsOverlay.Parameters();
        fields = new AnnotationsOverlay.Fields();
    }

    void invalidate() {
        classes.invalidate();
        methods.invalidate();
        parameters.invalidate();
        fields.invalidate();
    }

    Object key(org.jboss.jandex.AnnotationTarget jandexAnnotationTarget) {
        switch (jandexAnnotationTarget.kind()) {
            case CLASS:
                return classes.key(jandexAnnotationTarget.asClass());
            case METHOD:
                return methods.key(jandexAnnotationTarget.asMethod());
            case METHOD_PARAMETER:
                return parameters.key(jandexAnnotationTarget.asMethodParameter());
            case FIELD:
                return fields.key(jandexAnnotationTarget.asField());
            default:
                throw new IllegalArgumentException("Unknown annotation target: " + jandexAnnotationTarget);
        }
    }
}
