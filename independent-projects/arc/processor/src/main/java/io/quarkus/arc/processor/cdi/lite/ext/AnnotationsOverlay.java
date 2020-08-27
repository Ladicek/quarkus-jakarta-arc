package io.quarkus.arc.processor.cdi.lite.ext;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jboss.jandex.DotName;

// - Key must have equals/hashCode
// - JandexDeclaration must be a Jandex declaration for which Arc supports annotation transformations
abstract class AnnotationsOverlay<Key, JandexDeclaration extends org.jboss.jandex.AnnotationTarget> {
    // TODO add invalidation, after which this map would be cleaned up and no more modifications would be allowed
    private final Map<Key, AnnotationSet> currentAnnotations = new HashMap<>();
    private boolean invalid = false;

    AnnotationSet getAnnotations(JandexDeclaration jandexDeclaration) {
        if (invalid) {
            throw new IllegalStateException("Annotations overlay no longer valid");
        }

        Key key = keyFor(jandexDeclaration);
        return currentAnnotations.computeIfAbsent(key, ignored -> {
            Collection<org.jboss.jandex.AnnotationInstance> jandexAnnotations = originalJandexAnnotations(jandexDeclaration);
            return new AnnotationSet(jandexAnnotations);
        });
    }

    void invalidate() {
        currentAnnotations.clear();
        invalid = true;
    }

    abstract Key keyFor(JandexDeclaration jandexDeclaration);

    abstract Collection<org.jboss.jandex.AnnotationInstance> originalJandexAnnotations(JandexDeclaration jandexDeclaration);

    static class Classes extends AnnotationsOverlay<DotName, org.jboss.jandex.ClassInfo> {
        @Override
        DotName keyFor(org.jboss.jandex.ClassInfo classInfo) {
            return classInfo.name();
        }

        @Override
        Collection<org.jboss.jandex.AnnotationInstance> originalJandexAnnotations(org.jboss.jandex.ClassInfo classInfo) {
            return classInfo.classAnnotations();
        }
    }

    static class Methods extends AnnotationsOverlay<Methods.Key, org.jboss.jandex.MethodInfo> {
        @Override
        protected Key keyFor(org.jboss.jandex.MethodInfo method) {
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
        protected Key keyFor(org.jboss.jandex.FieldInfo field) {
            return new Key(field.declaringClass().name(), field.name());
        }

        @Override
        protected Collection<org.jboss.jandex.AnnotationInstance> originalJandexAnnotations(
                org.jboss.jandex.FieldInfo fieldInfo) {
            return fieldInfo.annotations();
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
