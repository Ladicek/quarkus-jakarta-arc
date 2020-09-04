package cdi.lite.extension.phases.validation;

import cdi.lite.extension.model.AnnotationTarget;

public interface Errors {
    void add(String message);

    void add(String message, AnnotationTarget relatedTo);

    void add(Exception exception);
}
