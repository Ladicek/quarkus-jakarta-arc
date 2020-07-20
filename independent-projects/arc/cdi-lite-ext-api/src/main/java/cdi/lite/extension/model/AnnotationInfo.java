package cdi.lite.extension.model;

import cdi.lite.extension.model.declarations.ClassInfo;
import cdi.lite.extension.model.declarations.DeclarationInfo;
import java.util.Collection;

public interface AnnotationInfo {
    /**
     * Target of this annotation.
     * That is, the declaration on which this annotation is present.
     * TODO what if this annotation is a nested annotation?
     */
    DeclarationInfo target();

    /**
     * Declaration of the annotation itself.
     */
    ClassInfo<?> declaration();

    /**
     * Whether the annotation has an attribute with given {@code name}.
     */
    boolean hasAttribute(String name);

    /**
     * Value of the annotation's attribute with given {@code name}.
     * TODO what if it doesn't exist? null, exception, or change return type to Optional
     */
    AnnotationAttributeValue attribute(String name);

    default boolean hasValue() {
        return hasAttribute("value");
    }

    default AnnotationAttributeValue value() {
        return attribute("value");
    }

    /**
     * All attributes of this annotation.
     */
    Collection<AnnotationAttribute> attributes();
}
