package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.model.AnnotationAttribute;
import cdi.lite.extension.model.AnnotationAttributeValue;
import java.util.Objects;

class AnnotationAttributeImpl implements AnnotationAttribute {
    final org.jboss.jandex.IndexView jandexIndex;
    final AllAnnotationOverlays annotationOverlays;
    final org.jboss.jandex.AnnotationValue jandexAnnotationAttribute;

    AnnotationAttributeImpl(org.jboss.jandex.IndexView jandexIndex, AllAnnotationOverlays annotationOverlays,
            org.jboss.jandex.AnnotationValue jandexAnnotationAttribute) {
        this.jandexIndex = jandexIndex;
        this.annotationOverlays = annotationOverlays;
        this.jandexAnnotationAttribute = jandexAnnotationAttribute;
    }

    @Override
    public String name() {
        return jandexAnnotationAttribute.name();
    }

    @Override
    public AnnotationAttributeValue value() {
        return new AnnotationAttributeValueImpl(jandexIndex, annotationOverlays, jandexAnnotationAttribute);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AnnotationAttributeImpl that = (AnnotationAttributeImpl) o;
        return Objects.equals(jandexAnnotationAttribute.name(), that.jandexAnnotationAttribute.name())
                && Objects.equals(jandexAnnotationAttribute.value(), that.jandexAnnotationAttribute.value());
    }

    @Override
    public int hashCode() {
        return Objects.hash(jandexAnnotationAttribute.name(), jandexAnnotationAttribute.value());
    }

    @Override
    public String toString() {
        return jandexAnnotationAttribute.toString();
    }
}
