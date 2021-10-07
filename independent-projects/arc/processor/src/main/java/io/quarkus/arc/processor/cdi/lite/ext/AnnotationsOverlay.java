package io.quarkus.arc.processor.cdi.lite.ext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.MethodParameterInfo;

// - Key must have equals/hashCode
// - JandexDeclaration must be a Jandex declaration for which Arc supports annotation transformations
//   directly (classes, methods, fields) or indirectly (parameters); see also AnnotationsTransformation
abstract class AnnotationsOverlay<Key, JandexDeclaration extends org.jboss.jandex.AnnotationTarget> {
    private final Map<Key, AnnotationSet> overlay = new HashMap<>();
    private final Map<Key, JandexDeclaration> overlaidDeclarations = new HashMap<>();
    private boolean invalid = false;

    AnnotationSet getAnnotations(JandexDeclaration jandexDeclaration, org.jboss.jandex.IndexView jandexIndex) {
        if (invalid) {
            throw new IllegalStateException("Annotations overlay no longer valid");
        }

        Key key = key(jandexDeclaration);
        if (overlay.containsKey(key)) {
            return overlay.get(key);
        }

        AnnotationSet annotationSet = createAnnotationSet(jandexDeclaration, jandexIndex);
        overlay.put(key, annotationSet);
        overlaidDeclarations.put(key, jandexDeclaration);
        return annotationSet;
    }

    boolean hasAnnotation(JandexDeclaration jandexDeclaration, DotName annotationName, org.jboss.jandex.IndexView jandexIndex) {
        if (invalid) {
            throw new IllegalStateException("Annotations overlay no longer valid");
        }

        Key key = key(jandexDeclaration);
        boolean hasOverlay = overlay.containsKey(key);

        if (hasOverlay) {
            return getAnnotations(jandexDeclaration, jandexIndex).hasAnnotation(annotationName);
        } else {
            return originalJandexAnnotationsContain(jandexDeclaration, annotationName, jandexIndex);
        }
    }

    // TODO remove if not necessary, together with `overlaidDeclarations`
    Collection<JandexDeclaration> overlaidDeclarationsWithAnnotation(DotName annotationName) {
        Set<Key> keysWithAnnotation = new HashSet<>();
        for (Map.Entry<Key, AnnotationSet> entry : overlay.entrySet()) {
            if (entry.getValue().hasAnnotation(annotationName)) {
                keysWithAnnotation.add(entry.getKey());
            }
        }

        return keysWithAnnotation.stream()
                .map(overlaidDeclarations::get)
                .collect(Collectors.toUnmodifiableList());
    }

    void invalidate() {
        overlay.clear();
        overlaidDeclarations.clear();
        invalid = true;
    }

    abstract Key key(JandexDeclaration jandexDeclaration);

    abstract AnnotationSet createAnnotationSet(JandexDeclaration jandexDeclaration, org.jboss.jandex.IndexView jandexIndex);

    // this is "just" an optimization to avoid creating and populating an `AnnotationSet`
    // when the only thing we need to know is if an annotation is present
    abstract boolean originalJandexAnnotationsContain(JandexDeclaration jandexDeclaration, DotName annotationName,
            org.jboss.jandex.IndexView jandexIndex);

    static class Classes extends AnnotationsOverlay<DotName, org.jboss.jandex.ClassInfo> {
        @Override
        DotName key(org.jboss.jandex.ClassInfo classInfo) {
            return classInfo.name();
        }

        @Override
        AnnotationSet createAnnotationSet(org.jboss.jandex.ClassInfo classInfo,
                org.jboss.jandex.IndexView jandexIndex) {
            // if an `@Inherited` annotation of some type is declared directly on class C, then annotations
            // of the same type declared directly on any direct or indirect superclass are _not_ present on C
            Set<DotName> alreadySeen = new HashSet<>();

            List<org.jboss.jandex.AnnotationInstance> jandexAnnotations = new ArrayList<>();
            Map<DotName, Integer> inheritanceDistances = new HashMap<>();

            int currentDistance = 0;
            while (classInfo != null && !classInfo.name().equals(DotNames.OBJECT)) {
                for (org.jboss.jandex.AnnotationInstance jandexAnnotation : classInfo.classAnnotations()) {
                    if (alreadySeen.contains(jandexAnnotation.name())) {
                        continue;
                    }
                    alreadySeen.add(jandexAnnotation.name());

                    jandexAnnotations.add(jandexAnnotation);
                    inheritanceDistances.put(jandexAnnotation.name(), currentDistance);
                }

                DotName superClassName = classInfo.superName();
                classInfo = jandexIndex.getClassByName(superClassName);
                currentDistance++;
            }

            return new AnnotationSet(jandexAnnotations, inheritanceDistances);
        }

        @Override
        boolean originalJandexAnnotationsContain(org.jboss.jandex.ClassInfo classInfo, DotName annotationName,
                org.jboss.jandex.IndexView jandexIndex) {
            while (classInfo != null && !classInfo.name().equals(DotNames.OBJECT)) {
                if (classInfo.classAnnotation(annotationName) != null) {
                    return true;
                }

                DotName superClassName = classInfo.superName();
                classInfo = jandexIndex.getClassByName(superClassName);
            }
            return false;
        }
    }

    static class Methods extends AnnotationsOverlay<Methods.Key, org.jboss.jandex.MethodInfo> {
        @Override
        Key key(org.jboss.jandex.MethodInfo methodInfo) {
            return new Key(methodInfo.declaringClass().name(), methodInfo.name(), methodInfo.parameters());
        }

        @Override
        AnnotationSet createAnnotationSet(org.jboss.jandex.MethodInfo methodInfo,
                org.jboss.jandex.IndexView jandexIndex) {
            List<org.jboss.jandex.AnnotationInstance> jandexAnnotations = methodInfo.annotations()
                    .stream()
                    .filter(it -> it.target().kind() == org.jboss.jandex.AnnotationTarget.Kind.METHOD)
                    .collect(Collectors.toUnmodifiableList());
            return new AnnotationSet(jandexAnnotations);
        }

        @Override
        boolean originalJandexAnnotationsContain(org.jboss.jandex.MethodInfo methodInfo, DotName annotationName,
                org.jboss.jandex.IndexView jandexIndex) {
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

    static class Parameters extends AnnotationsOverlay<Parameters.Key, org.jboss.jandex.MethodParameterInfo> {
        @Override
        Key key(org.jboss.jandex.MethodParameterInfo methodParameterInfo) {
            return new Key(methodParameterInfo.method().declaringClass().name(), methodParameterInfo.method().name(),
                    methodParameterInfo.method().parameters(), methodParameterInfo.position());
        }

        @Override
        AnnotationSet createAnnotationSet(org.jboss.jandex.MethodParameterInfo methodParameterInfo,
                org.jboss.jandex.IndexView jandexIndex) {
            List<org.jboss.jandex.AnnotationInstance> jandexAnnotations = methodParameterInfo.method()
                    .annotations()
                    .stream()
                    .filter(it -> it.target().kind() == org.jboss.jandex.AnnotationTarget.Kind.METHOD_PARAMETER
                            && it.target().asMethodParameter().position() == methodParameterInfo.position())
                    .collect(Collectors.toUnmodifiableList());
            return new AnnotationSet(jandexAnnotations);
        }

        @Override
        boolean originalJandexAnnotationsContain(org.jboss.jandex.MethodParameterInfo methodParameterInfo,
                DotName annotationName, org.jboss.jandex.IndexView jandexIndex) {
            return methodParameterInfo.method()
                    .annotations(annotationName)
                    .stream()
                    .anyMatch(it -> it.target().kind() == org.jboss.jandex.AnnotationTarget.Kind.METHOD_PARAMETER
                            && it.target().asMethodParameter().position() == methodParameterInfo.position());
        }

        static final class Key {
            private final DotName className;
            private final String methodName;
            private final List<org.jboss.jandex.Type> parameterTypes;
            private final short parameterPosition;

            Key(DotName className, String methodName, List<org.jboss.jandex.Type> parameterTypes, short parameterPosition) {
                this.className = className;
                this.methodName = methodName;
                this.parameterTypes = parameterTypes;
                this.parameterPosition = parameterPosition;
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
                        && Objects.equals(parameterTypes, key.parameterTypes)
                        && parameterPosition == key.parameterPosition;
            }

            @Override
            public int hashCode() {
                return Objects.hash(className, methodName, parameterTypes, parameterPosition);
            }
        }
    }

    static class Fields extends AnnotationsOverlay<Fields.Key, org.jboss.jandex.FieldInfo> {
        @Override
        Key key(org.jboss.jandex.FieldInfo fieldInfo) {
            return new Key(fieldInfo.declaringClass().name(), fieldInfo.name());
        }

        @Override
        AnnotationSet createAnnotationSet(org.jboss.jandex.FieldInfo fieldInfo,
                org.jboss.jandex.IndexView jandexIndex) {
            List<org.jboss.jandex.AnnotationInstance> jandexAnnotations = fieldInfo.annotations()
                    .stream()
                    .filter(it -> it.target().kind() == org.jboss.jandex.AnnotationTarget.Kind.FIELD)
                    .collect(Collectors.toUnmodifiableList());
            return new AnnotationSet(jandexAnnotations);
        }

        @Override
        boolean originalJandexAnnotationsContain(org.jboss.jandex.FieldInfo fieldInfo, DotName annotationName,
                org.jboss.jandex.IndexView jandexIndex) {
            return fieldInfo.annotations()
                    .stream()
                    .anyMatch(it -> it.name().equals(annotationName)
                            && it.target().kind() == org.jboss.jandex.AnnotationTarget.Kind.FIELD);
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
