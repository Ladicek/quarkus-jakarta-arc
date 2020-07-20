package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.model.AnnotationAttribute;
import cdi.lite.extension.model.AnnotationAttributeValue;

class AnnotationAttributeImpl implements AnnotationAttribute {
    final org.jboss.jandex.IndexView jandexIndex;
    final org.jboss.jandex.AnnotationValue jandexAnnotationAttribute;

    AnnotationAttributeImpl(org.jboss.jandex.IndexView jandexIndex,
            org.jboss.jandex.AnnotationValue jandexAnnotationAttribute) {
        this.jandexIndex = jandexIndex;
        this.jandexAnnotationAttribute = jandexAnnotationAttribute;
    }

    @Override
    public String name() {
        return jandexAnnotationAttribute.name();
    }

    @Override
    public AnnotationAttributeValue value() {
        return new AnnotationAttributeValueImpl(jandexIndex, jandexAnnotationAttribute);
    }
}
