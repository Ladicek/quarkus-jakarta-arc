package cdi.lite.extension.phases.validation;

import cdi.lite.extension.beans.BeanInfo;
import cdi.lite.extension.beans.ObserverInfo;
import cdi.lite.extension.model.AnnotationTarget;

public interface Errors {
    /**
     * Add a generic error that is not related to any particular element, or that information is not known.
     */
    void add(String message);

    /**
     * Add an error which is related to given {@link AnnotationTarget} (which is most likely
     * a {@link cdi.lite.extension.model.declarations.DeclarationInfo DeclarationInfo}).
     */
    void add(String message, AnnotationTarget relatedTo);

    /**
     * Add an error which is related to given {@link BeanInfo}.
     */
    void add(String message, BeanInfo<?> relatedTo);

    /**
     * Add an error which is related to given {@link ObserverInfo}.
     */
    void add(String message, ObserverInfo<?> relatedTo);

    /**
     * Add a generic error that is represented by an exception.
     */
    void add(Exception exception);
}
