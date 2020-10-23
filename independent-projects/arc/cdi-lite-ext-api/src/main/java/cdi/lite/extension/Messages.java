package cdi.lite.extension;

import cdi.lite.extension.beans.BeanInfo;
import cdi.lite.extension.beans.ObserverInfo;
import cdi.lite.extension.model.AnnotationTarget;

public interface Messages {
    /**
     * Add a generic information message that is not related to any particular element, or that information is not known.
     */
    void info(String message);

    /**
     * Add an information message which is related to given {@link AnnotationTarget} (which is most likely
     * a {@link cdi.lite.extension.model.declarations.DeclarationInfo DeclarationInfo}).
     */
    void info(String message, AnnotationTarget relatedTo);

    /**
     * Add an information message which is related to given {@link BeanInfo}.
     */
    void info(String message, BeanInfo<?> relatedTo);

    /**
     * Add an information message which is related to given {@link ObserverInfo}.
     */
    void info(String message, ObserverInfo<?> relatedTo);

    /**
     * Add a generic warning that is not related to any particular element, or that information is not known.
     */
    void warn(String message);

    /**
     * Add a warning which is related to given {@link AnnotationTarget} (which is most likely
     * a {@link cdi.lite.extension.model.declarations.DeclarationInfo DeclarationInfo}).
     */
    void warn(String message, AnnotationTarget relatedTo);

    /**
     * Add a warning which is related to given {@link BeanInfo}.
     */
    void warn(String message, BeanInfo<?> relatedTo);

    /**
     * Add a warning which is related to given {@link ObserverInfo}.
     */
    void warn(String message, ObserverInfo<?> relatedTo);

    /**
     * Add a generic error that is not related to any particular element, or that information is not known.
     */
    void error(String message);

    /**
     * Add an error which is related to given {@link AnnotationTarget} (which is most likely
     * a {@link cdi.lite.extension.model.declarations.DeclarationInfo DeclarationInfo}).
     */
    void error(String message, AnnotationTarget relatedTo);

    /**
     * Add an error which is related to given {@link BeanInfo}.
     */
    void error(String message, BeanInfo<?> relatedTo);

    /**
     * Add an error which is related to given {@link ObserverInfo}.
     */
    void error(String message, ObserverInfo<?> relatedTo);

    /**
     * Add a generic error that is represented by an exception.
     */
    void error(Exception exception);
}
