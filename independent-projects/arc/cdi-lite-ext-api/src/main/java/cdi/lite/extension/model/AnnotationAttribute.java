package cdi.lite.extension.model;

// TODO "attribute" is a colloquial expression, perhaps use something closer to the JLS?
public interface AnnotationAttribute {
    String name();

    AnnotationAttributeValue value();
}
