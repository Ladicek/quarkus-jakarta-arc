package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.model.AnnotationAttribute;
import cdi.lite.extension.model.AnnotationAttributeValue;
import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.AnnotationTarget;
import cdi.lite.extension.model.declarations.ClassInfo;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jboss.jandex.DotName;

class AnnotationInfoImpl implements AnnotationInfo {
    final org.jboss.jandex.IndexView jandexIndex;
    final AllAnnotationOverlays annotationOverlays;
    final org.jboss.jandex.AnnotationInstance jandexAnnotation;

    AnnotationInfoImpl(org.jboss.jandex.IndexView jandexIndex, AllAnnotationOverlays annotationOverlays,
            org.jboss.jandex.AnnotationInstance jandexAnnotation) {
        this.jandexIndex = jandexIndex;
        this.annotationOverlays = annotationOverlays;
        this.jandexAnnotation = jandexAnnotation;
    }

    @Override
    public AnnotationTarget target() {
        org.jboss.jandex.AnnotationTarget jandexAnnotationTarget = jandexAnnotation.target();
        if (jandexAnnotationTarget == null) {
            // TODO
            return null;
        } else if (jandexAnnotationTarget.kind() == org.jboss.jandex.AnnotationTarget.Kind.TYPE) {
            return TypeImpl.fromJandexType(jandexIndex, annotationOverlays, jandexAnnotationTarget.asType().target());
        } else {
            return DeclarationInfoImpl.fromJandexDeclaration(jandexIndex, annotationOverlays, jandexAnnotation.target());
        }
    }

    @Override
    public ClassInfo<?> declaration() {
        DotName annotationClassName = jandexAnnotation.name();
        org.jboss.jandex.ClassInfo annotationClass = jandexIndex.getClassByName(annotationClassName);
        if (annotationClass == null) {
            throw new IllegalStateException("Class " + annotationClassName + " not found in Jandex");
        }
        return new ClassInfoImpl(jandexIndex, annotationOverlays, annotationClass);
    }

    @Override
    public boolean hasAttribute(String name) {
        return jandexAnnotation.valueWithDefault(jandexIndex, name) != null;
    }

    @Override
    public AnnotationAttributeValue attribute(String name) {
        return new AnnotationAttributeValueImpl(jandexIndex, annotationOverlays,
                jandexAnnotation.valueWithDefault(jandexIndex, name));
    }

    @Override
    public Collection<AnnotationAttribute> attributes() {
        return jandexAnnotation.valuesWithDefaults(jandexIndex)
                .stream()
                .map(it -> new AnnotationAttributeImpl(jandexIndex, annotationOverlays, it))
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AnnotationInfoImpl that = (AnnotationInfoImpl) o;
        return Objects.equals(jandexAnnotation.name(), that.jandexAnnotation.name())
                && Objects.equals(attributes(), that.attributes());
    }

    @Override
    public int hashCode() {
        return Objects.hash(jandexAnnotation.name(), attributes());
    }

    @Override
    public String toString() {
        return jandexAnnotation.toString(false);
    }
}
