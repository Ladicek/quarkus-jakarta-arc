package io.quarkus.arc.processor.cdi.lite.ext;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import org.jboss.jandex.DotName;

class AnnotationSet {
    private final Map<DotName, org.jboss.jandex.AnnotationInstance> data;

    AnnotationSet(Collection<org.jboss.jandex.AnnotationInstance> jandexAnnotations) {
        Map<DotName, org.jboss.jandex.AnnotationInstance> data = new HashMap<>();
        for (org.jboss.jandex.AnnotationInstance jandexAnnotation : jandexAnnotations) {
            data.put(jandexAnnotation.name(), jandexAnnotation);
        }
        this.data = data;
    }

    boolean hasAnnotation(Class<? extends Annotation> annotationType) {
        DotName name = DotName.createSimple(annotationType.getName());
        return hasAnnotation(name);
    }

    boolean hasAnnotation(DotName annotationName) {
        return data.containsKey(annotationName);
    }

    org.jboss.jandex.AnnotationInstance annotation(Class<? extends Annotation> annotationType) {
        DotName name = DotName.createSimple(annotationType.getName());
        return data.get(name);
    }

    // copied from Jandex
    Collection<org.jboss.jandex.AnnotationInstance> annotationsWithRepeatable(Class<? extends Annotation> annotationType,
            org.jboss.jandex.IndexView jandexIndex) {
        DotName name = DotName.createSimple(annotationType.getName());

        org.jboss.jandex.AnnotationInstance ret = data.get(name);
        if (ret != null) {
            // Annotation present - no need to try to find repeatable annotations
            return Collections.singleton(ret);
        }
        org.jboss.jandex.ClassInfo annotationClass = jandexIndex.getClassByName(name);
        if (annotationClass == null) {
            throw new IllegalArgumentException("Index does not contain the annotation definition: " + name);
        }
        if (!annotationClass.isAnnotation()) {
            throw new IllegalArgumentException("Not an annotation type: " + annotationClass);
        }
        org.jboss.jandex.AnnotationInstance repeatable = annotationClass.classAnnotation(DotNames.REPEATABLE);
        if (repeatable == null) {
            return Collections.emptySet();
        }
        org.jboss.jandex.Type containingType = repeatable.value().asClass();
        org.jboss.jandex.AnnotationInstance containing = data.get(containingType.name());
        if (containing == null) {
            return Collections.emptySet();
        }
        org.jboss.jandex.AnnotationInstance[] values = containing.value().asNestedArray();
        return Arrays.asList(values);
    }

    Collection<org.jboss.jandex.AnnotationInstance> annotations() {
        return Collections.unmodifiableCollection(data.values());
    }

    // ---
    // modifications, can only be called from AnnotationsTransformation

    void add(org.jboss.jandex.AnnotationInstance jandexAnnotation) {
        data.put(jandexAnnotation.name(), jandexAnnotation);
    }

    void removeIf(Predicate<org.jboss.jandex.AnnotationInstance> predicate) {
        data.values().removeIf(predicate);
    }
}
