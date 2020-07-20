package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.model.AnnotationAttribute;
import cdi.lite.extension.model.AnnotationAttributeValue;
import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.declarations.ClassInfo;
import cdi.lite.extension.model.declarations.DeclarationInfo;
import java.util.Collection;
import java.util.stream.Collectors;
import org.jboss.jandex.DotName;

class AnnotationInfoImpl implements AnnotationInfo {
    final org.jboss.jandex.IndexView jandexIndex;
    final org.jboss.jandex.AnnotationInstance jandexAnnotation;

    AnnotationInfoImpl(org.jboss.jandex.IndexView jandexIndex, org.jboss.jandex.AnnotationInstance jandexAnnotation) {
        this.jandexIndex = jandexIndex;
        this.jandexAnnotation = jandexAnnotation;
    }

    @Override
    public DeclarationInfo target() {
        return DeclarationInfoImpl.fromJandexDeclaration(jandexIndex, jandexAnnotation.target());
    }

    @Override
    public ClassInfo<?> declaration() {
        DotName annotationClassName = jandexAnnotation.name();
        org.jboss.jandex.ClassInfo annotationClass = jandexIndex.getClassByName(annotationClassName);
        if (annotationClass == null) {
            throw new IllegalStateException("Class " + annotationClassName + " not found in Jandex");
        }
        return new ClassInfoImpl(jandexIndex, annotationClass);
    }

    @Override
    public boolean hasAttribute(String name) {
        return jandexAnnotation.valueWithDefault(jandexIndex, name) != null;
    }

    @Override
    public AnnotationAttributeValue attribute(String name) {
        return new AnnotationAttributeValueImpl(jandexIndex, jandexAnnotation.valueWithDefault(jandexIndex, name));
    }

    @Override
    public Collection<AnnotationAttribute> attributes() {
        return jandexAnnotation.valuesWithDefaults(jandexIndex)
                .stream()
                .map(it -> new AnnotationAttributeImpl(jandexIndex, it))
                .collect(Collectors.toList());
    }
}
