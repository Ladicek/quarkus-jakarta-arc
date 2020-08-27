package io.quarkus.arc.processor.cdi.lite.ext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import org.jboss.jandex.DotName;

class AnnotationsReflection {
    // TODO if this method is enough, get rid of `with`
    static org.jboss.jandex.AnnotationInstance from(Annotation annotation) {
        return with(annotation, (name, jandexAnnotationAttributes) -> {
            return org.jboss.jandex.AnnotationInstance.create(name, null, jandexAnnotationAttributes);
        });
    }

    static <T> T with(Annotation annotation, BiFunction<DotName, org.jboss.jandex.AnnotationValue[], T> function) {
        Class<? extends Annotation> annotationType = findAnnotationType(annotation);

        DotName jandexName = DotName.createSimple(annotationType.getName());
        org.jboss.jandex.AnnotationValue[] jandexAnnotationValues = AnnotationsReflection.jandexAnnotationAttributes(
                (Class<Annotation>) annotationType, annotation);

        return function.apply(jandexName, jandexAnnotationValues);
    }

    static void with(Annotation annotation, BiConsumer<DotName, org.jboss.jandex.AnnotationValue[]> consumer) {
        with(annotation, biFunctionFromBiConsumer(consumer));
    }

    private static <T, U> BiFunction<T, U, Void> biFunctionFromBiConsumer(BiConsumer<T, U> biConsumer) {
        return (t, u) -> {
            biConsumer.accept(t, u);
            return null;
        };
    }

    private static Class<? extends Annotation> findAnnotationType(Annotation annotation) {
        Class<? extends Annotation> annotationType = null;

        Queue<Class<?>> candidates = new ArrayDeque<>();
        candidates.add(annotation.getClass());
        while (!candidates.isEmpty()) {
            Class<?> candidate = candidates.remove();

            if (candidate.isAnnotation()) {
                annotationType = (Class<? extends Annotation>) candidate;
                break;
            }

            Collections.addAll(candidates, candidate.getInterfaces());
        }

        if (annotationType == null) {
            throw new IllegalArgumentException("Not an annotation: " + annotation);
        }

        return annotationType;
    }

    private static <A extends Annotation> org.jboss.jandex.AnnotationValue[] jandexAnnotationAttributes(Class<A> type,
            A value) {
        List<org.jboss.jandex.AnnotationValue> result = new ArrayList<>();
        for (Method attribute : type.getDeclaredMethods()) {
            try {
                result.add(jandexAnnotationValue(attribute.getName(), attribute.invoke(value)));
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
        return result.toArray(new org.jboss.jandex.AnnotationValue[0]);
    }

    private static org.jboss.jandex.AnnotationValue jandexAnnotationValue(String name, Object value) {
        if (value instanceof Boolean) {
            return org.jboss.jandex.AnnotationValue.createBooleanValue(name, (Boolean) value);
        } else if (value instanceof Byte) {
            return org.jboss.jandex.AnnotationValue.createByteValue(name, (Byte) value);
        } else if (value instanceof Short) {
            return org.jboss.jandex.AnnotationValue.createShortValue(name, (Short) value);
        } else if (value instanceof Integer) {
            return org.jboss.jandex.AnnotationValue.createIntegerValue(name, (Integer) value);
        } else if (value instanceof Long) {
            return org.jboss.jandex.AnnotationValue.createLongValue(name, (Long) value);
        } else if (value instanceof Float) {
            return org.jboss.jandex.AnnotationValue.createFloatValue(name, (Float) value);
        } else if (value instanceof Double) {
            return org.jboss.jandex.AnnotationValue.createDoubleValue(name, (Double) value);
        } else if (value instanceof Character) {
            return org.jboss.jandex.AnnotationValue.createCharacterValue(name, (Character) value);
        } else if (value instanceof String) {
            return org.jboss.jandex.AnnotationValue.createStringValue(name, (String) value);
        } else if (value instanceof Enum) {
            return org.jboss.jandex.AnnotationValue.createEnumValue(name,
                    DotName.createSimple(((Enum<?>) value).getDeclaringClass().getName()), ((Enum<?>) value).name());
        } else if (value instanceof Class) {
            return org.jboss.jandex.AnnotationValue.createClassValue(name, TypesReflection.jandexType((Class<?>) value));
        } else if (value.getClass().isArray()) {
            org.jboss.jandex.AnnotationValue[] jandexAnnotationValues = Arrays.stream((Object[]) value)
                    .map(it -> jandexAnnotationValue(name, it))
                    .toArray(org.jboss.jandex.AnnotationValue[]::new);
            return org.jboss.jandex.AnnotationValue.createArrayValue(name, jandexAnnotationValues);
        } else if (value.getClass().isAnnotation()) {
            Class<? extends Annotation> annotationType = findAnnotationType((Annotation) value);
            org.jboss.jandex.AnnotationValue[] jandexAnnotationValues = jandexAnnotationAttributes(
                    (Class<Annotation>) annotationType, (Annotation) value);
            org.jboss.jandex.AnnotationInstance jandexAnnotationInstance = org.jboss.jandex.AnnotationInstance.create(
                    DotName.createSimple(annotationType.getName()), null, jandexAnnotationValues);
            return org.jboss.jandex.AnnotationValue.createNestedAnnotationValue(name, jandexAnnotationInstance);
        } else {
            throw new IllegalArgumentException("Unknown annotation attribute value: " + value);
        }
    }
}
