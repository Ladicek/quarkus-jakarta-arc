package io.quarkus.arc.processor.cdi.lite.ext;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.enterprise.lang.model.AnnotationInfo;
import javax.enterprise.lang.model.AnnotationMember;
import javax.enterprise.lang.model.declarations.ClassInfo;
import org.jboss.jandex.DotName;

class AnnotationInfoImpl<T extends Annotation> implements AnnotationInfo<T> {
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
    public ClassInfo<T> declaration() {
        DotName annotationClassName = jandexAnnotation.name();
        org.jboss.jandex.ClassInfo annotationClass = jandexIndex.getClassByName(annotationClassName);
        if (annotationClass == null) {
            throw new IllegalStateException("Class " + annotationClassName + " not found in Jandex");
        }
        // TODO ClassInfo should lose its type parameter
        return (ClassInfo) new ClassInfoImpl(jandexIndex, annotationOverlays, annotationClass);
    }

    @Override
    public boolean hasMember(String name) {
        return jandexAnnotation.valueWithDefault(jandexIndex, name) != null;
    }

    @Override
    public AnnotationMember member(String name) {
        return new AnnotationMemberImpl(jandexIndex, annotationOverlays,
                jandexAnnotation.valueWithDefault(jandexIndex, name));
    }

    @Override
    public Map<String, AnnotationMember> members() {
        Map<String, AnnotationMember> result = new HashMap<>();
        for (org.jboss.jandex.AnnotationValue jandexAnnotationMember : jandexAnnotation.valuesWithDefaults(jandexIndex)) {
            result.put(jandexAnnotationMember.name(),
                    new AnnotationMemberImpl(jandexIndex, annotationOverlays, jandexAnnotationMember));
        }
        return Collections.unmodifiableMap(result);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AnnotationInfoImpl that = (AnnotationInfoImpl) o;
        return Objects.equals(jandexAnnotation.name(), that.jandexAnnotation.name())
                && Objects.equals(members(), that.members());
    }

    @Override
    public int hashCode() {
        return Objects.hash(jandexAnnotation.name(), members());
    }

    @Override
    public String toString() {
        return jandexAnnotation.toString(false);
    }
}
