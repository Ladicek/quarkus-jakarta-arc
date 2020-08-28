package io.quarkus.arc.processor.cdi.lite.ext;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.jboss.jandex.DotName;

// - Key must have equals/hashCode
// - JandexDeclaration must be a Jandex declaration for which Arc supports annotation transformations
abstract class AnnotationsOverlay<Key, JandexDeclaration extends org.jboss.jandex.AnnotationTarget> {
    private final Map<Key, AnnotationSet> overlay = new HashMap<>();
    private final Map<Key, JandexDeclaration> overlaidDeclarations = new HashMap<>();
    private boolean invalid = false;

    AnnotationSet getAnnotations(JandexDeclaration jandexDeclaration) {
        if (invalid) {
            throw new IllegalStateException("Annotations overlay no longer valid");
        }

        Key key = key(jandexDeclaration);
        if (overlay.containsKey(key)) {
            return overlay.get(key);
        }

        AnnotationSet annotationSet = new AnnotationSet(originalJandexAnnotations(jandexDeclaration));
        overlay.put(key, annotationSet);
        overlaidDeclarations.put(key, jandexDeclaration);
        return annotationSet;
    }

    boolean hasOverlay(JandexDeclaration jandexDeclaration) {
        Key key = key(jandexDeclaration);
        return overlay.containsKey(key);
    }

    boolean hasAnnotation(JandexDeclaration jandexDeclaration, DotName annotationName) {
        if (hasOverlay(jandexDeclaration)) {
            return getAnnotations(jandexDeclaration).hasAnnotation(annotationName);
        } else {
            return originalJandexAnnotationsContain(jandexDeclaration, annotationName);
        }
    }

    Collection<JandexDeclaration> overlaidDeclarationsWithAnnotation(DotName annotationName) {
        Set<Key> keysWithAnnotation = new HashSet<>();
        for (Map.Entry<Key, AnnotationSet> entry : overlay.entrySet()) {
            if (entry.getValue().hasAnnotation(annotationName)) {
                keysWithAnnotation.add(entry.getKey());
            }
        }

        return keysWithAnnotation.stream()
                .map(overlaidDeclarations::get)
                .collect(Collectors.toList());
    }

    void invalidate() {
        overlay.clear();
        overlaidDeclarations.clear();
        invalid = true;
    }

    abstract Key key(JandexDeclaration jandexDeclaration);

    abstract Collection<org.jboss.jandex.AnnotationInstance> originalJandexAnnotations(JandexDeclaration jandexDeclaration);

    abstract boolean originalJandexAnnotationsContain(JandexDeclaration jandexDeclaration, DotName annotationName);

    static class Classes extends AnnotationsOverlay<DotName, org.jboss.jandex.ClassInfo> {
        @Override
        DotName key(org.jboss.jandex.ClassInfo classInfo) {
            return classInfo.name();
        }

        @Override
        Collection<org.jboss.jandex.AnnotationInstance> originalJandexAnnotations(org.jboss.jandex.ClassInfo classInfo) {
            return classInfo.classAnnotations();
        }

        @Override
        boolean originalJandexAnnotationsContain(org.jboss.jandex.ClassInfo classInfo, DotName annotationName) {
            return classInfo.classAnnotation(annotationName) != null;
        }
    }

    static class Methods extends AnnotationsOverlay<Methods.Key, org.jboss.jandex.MethodInfo> {
        @Override
        protected Key key(org.jboss.jandex.MethodInfo method) {
            return new Key(method.declaringClass().name(), method.name(), method.parameters());
        }

        @Override
        protected Collection<org.jboss.jandex.AnnotationInstance> originalJandexAnnotations(
                org.jboss.jandex.MethodInfo methodInfo) {
            return methodInfo.annotations()
                    .stream()
                    .filter(it -> it.target().kind() == org.jboss.jandex.AnnotationTarget.Kind.METHOD)
                    .collect(Collectors.toList());
        }

        @Override
        boolean originalJandexAnnotationsContain(org.jboss.jandex.MethodInfo methodInfo, DotName annotationName) {
            return methodInfo.annotations(annotationName)
                    .stream()
                    .anyMatch(it -> it.target().kind() == org.jboss.jandex.AnnotationTarget.Kind.METHOD);
        }

        static final class Key {
            private final DotName className;
            private final String methodName;
            private final List<org.jboss.jandex.Type> parameterTypes;

            Key(DotName className, String methodName, List<org.jboss.jandex.Type> parameterTypes) {
                this.className = className;
                this.methodName = methodName;
                this.parameterTypes = parameterTypes;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o)
                    return true;
                if (!(o instanceof Key))
                    return false;
                Key key = (Key) o;
                return Objects.equals(className, key.className)
                        && Objects.equals(methodName, key.methodName)
                        && Objects.equals(parameterTypes, key.parameterTypes);
            }

            @Override
            public int hashCode() {
                return Objects.hash(className, methodName, parameterTypes);
            }
        }
    }

    static class Fields extends AnnotationsOverlay<Fields.Key, org.jboss.jandex.FieldInfo> {
        @Override
        protected Key key(org.jboss.jandex.FieldInfo field) {
            return new Key(field.declaringClass().name(), field.name());
        }

        @Override
        protected Collection<org.jboss.jandex.AnnotationInstance> originalJandexAnnotations(
                org.jboss.jandex.FieldInfo fieldInfo) {
            return fieldInfo.annotations();
        }

        @Override
        boolean originalJandexAnnotationsContain(org.jboss.jandex.FieldInfo fieldInfo, DotName annotationName) {
            return fieldInfo.hasAnnotation(annotationName);
        }

        static final class Key {
            private final DotName className;
            private final String fieldName;

            Key(DotName className, String fieldName) {
                this.className = className;
                this.fieldName = fieldName;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o)
                    return true;
                if (!(o instanceof Key))
                    return false;
                Key key = (Key) o;
                return Objects.equals(className, key.className)
                        && Objects.equals(fieldName, key.fieldName);
            }

            @Override
            public int hashCode() {
                return Objects.hash(className, fieldName);
            }
        }
    }
}
