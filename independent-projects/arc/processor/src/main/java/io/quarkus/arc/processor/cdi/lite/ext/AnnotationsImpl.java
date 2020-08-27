package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.Annotations;
import cdi.lite.extension.model.AnnotationAttribute;
import cdi.lite.extension.model.AnnotationAttributeValue;
import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.declarations.ClassInfo;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jboss.jandex.DotName;

class AnnotationsImpl implements Annotations {
    private final org.jboss.jandex.IndexView jandexIndex;
    private final AllAnnotationOverlays annotationOverlays;

    AnnotationsImpl(org.jboss.jandex.IndexView jandexIndex, AllAnnotationOverlays annotationOverlays) {
        this.jandexIndex = jandexIndex;
        this.annotationOverlays = annotationOverlays;
    }

    @Override
    public AnnotationAttributeValue value(boolean value) {
        return attribute(null, value).value();
    }

    @Override
    public AnnotationAttributeValue value(byte value) {
        return attribute(null, value).value();
    }

    @Override
    public AnnotationAttributeValue value(short value) {
        return attribute(null, value).value();
    }

    @Override
    public AnnotationAttributeValue value(int value) {
        return attribute(null, value).value();
    }

    @Override
    public AnnotationAttributeValue value(long value) {
        return attribute(null, value).value();
    }

    @Override
    public AnnotationAttributeValue value(float value) {
        return attribute(null, value).value();
    }

    @Override
    public AnnotationAttributeValue value(double value) {
        return attribute(null, value).value();
    }

    @Override
    public AnnotationAttributeValue value(char value) {
        return attribute(null, value).value();
    }

    @Override
    public AnnotationAttributeValue value(String value) {
        return attribute(null, value).value();
    }

    @Override
    public AnnotationAttributeValue value(Enum<?> enumValue) {
        return attribute(null, enumValue).value();
    }

    @Override
    public AnnotationAttributeValue value(Class<? extends Enum<?>> enumType, String enumValue) {
        return attribute(null, enumType, enumValue).value();
    }

    @Override
    public AnnotationAttributeValue value(ClassInfo<?> enumType, String enumValue) {
        return attribute(null, enumType, enumValue).value();
    }

    @Override
    public AnnotationAttributeValue value(Class<?> value) {
        return attribute(null, value).value();
    }

    @Override
    public AnnotationAttributeValue annotationValue(Class<? extends Annotation> annotationType,
            AnnotationAttribute... attributes) {
        return annotationAttribute(null, annotationType, attributes).value();
    }

    @Override
    public AnnotationAttributeValue annotationValue(ClassInfo<?> annotationType, AnnotationAttribute... attributes) {
        return annotationAttribute(null, annotationType, attributes).value();
    }

    @Override
    public AnnotationAttributeValue annotationValue(AnnotationInfo annotation) {
        return annotationAttribute(null, annotation).value();
    }

    @Override
    public AnnotationAttributeValue annotationValue(Annotation annotation) {
        return annotationAttribute(null, annotation).value();
    }

    @Override
    public AnnotationAttribute attribute(String name, boolean value) {
        return new AnnotationAttributeImpl(jandexIndex, annotationOverlays,
                org.jboss.jandex.AnnotationValue.createBooleanValue(name, value));
    }

    @Override
    public AnnotationAttribute attribute(String name, byte value) {
        return new AnnotationAttributeImpl(jandexIndex, annotationOverlays,
                org.jboss.jandex.AnnotationValue.createByteValue(name, value));
    }

    @Override
    public AnnotationAttribute attribute(String name, short value) {
        return new AnnotationAttributeImpl(jandexIndex, annotationOverlays,
                org.jboss.jandex.AnnotationValue.createShortValue(name, value));
    }

    @Override
    public AnnotationAttribute attribute(String name, int value) {
        return new AnnotationAttributeImpl(jandexIndex, annotationOverlays,
                org.jboss.jandex.AnnotationValue.createIntegerValue(name, value));
    }

    @Override
    public AnnotationAttribute attribute(String name, long value) {
        return new AnnotationAttributeImpl(jandexIndex, annotationOverlays,
                org.jboss.jandex.AnnotationValue.createLongValue(name, value));
    }

    @Override
    public AnnotationAttribute attribute(String name, float value) {
        return new AnnotationAttributeImpl(jandexIndex, annotationOverlays,
                org.jboss.jandex.AnnotationValue.createFloatValue(name, value));
    }

    @Override
    public AnnotationAttribute attribute(String name, double value) {
        return new AnnotationAttributeImpl(jandexIndex, annotationOverlays,
                org.jboss.jandex.AnnotationValue.createDoubleValue(name, value));
    }

    @Override
    public AnnotationAttribute attribute(String name, char value) {
        return new AnnotationAttributeImpl(jandexIndex, annotationOverlays,
                org.jboss.jandex.AnnotationValue.createCharacterValue(name, value));
    }

    @Override
    public AnnotationAttribute attribute(String name, String value) {
        return new AnnotationAttributeImpl(jandexIndex, annotationOverlays,
                org.jboss.jandex.AnnotationValue.createStringValue(name, value));
    }

    @Override
    public AnnotationAttribute attribute(String name, Enum<?> enumValue) {
        return new AnnotationAttributeImpl(jandexIndex, annotationOverlays,
                org.jboss.jandex.AnnotationValue.createEnumValue(name,
                        DotName.createSimple(enumValue.getDeclaringClass().getName()), enumValue.name()));
    }

    @Override
    public AnnotationAttribute attribute(String name, Class<? extends Enum<?>> enumType, String enumValue) {
        return new AnnotationAttributeImpl(jandexIndex, annotationOverlays,
                org.jboss.jandex.AnnotationValue.createEnumValue(name, DotName.createSimple(enumType.getName()), enumValue));
    }

    @Override
    public AnnotationAttribute attribute(String name, ClassInfo<?> enumType, String enumValue) {
        return new AnnotationAttributeImpl(jandexIndex, annotationOverlays,
                org.jboss.jandex.AnnotationValue.createEnumValue(name, ((ClassInfoImpl) enumType).jandexDeclaration.name(),
                        enumValue));
    }

    @Override
    public AnnotationAttribute attribute(String name, Class<?> value) {
        return new AnnotationAttributeImpl(jandexIndex, annotationOverlays,
                org.jboss.jandex.AnnotationValue.createClassValue(name, TypesReflection.jandexType(value)));
    }

    @Override
    public AnnotationAttribute arrayAttribute(String name, AnnotationAttributeValue... values) {
        return arrayAttribute(name, Arrays.stream(values));
    }

    @Override
    public AnnotationAttribute arrayAttribute(String name, List<AnnotationAttributeValue> values) {
        return arrayAttribute(name, values.stream());
    }

    private AnnotationAttribute arrayAttribute(String name, Stream<AnnotationAttributeValue> values) {
        org.jboss.jandex.AnnotationValue[] jandexAnnotationAttributes = values
                .map(it -> ((AnnotationAttributeValueImpl) it).jandexAnnotationAttribute)
                .toArray(org.jboss.jandex.AnnotationValue[]::new);
        return new AnnotationAttributeImpl(jandexIndex, annotationOverlays,
                org.jboss.jandex.AnnotationValue.createArrayValue(name, jandexAnnotationAttributes));
    }

    @Override
    public AnnotationAttribute annotationAttribute(String name, Class<? extends Annotation> annotationType,
            AnnotationAttribute... attributes) {
        List<org.jboss.jandex.AnnotationValue> jandexAttributes = Arrays.stream(attributes)
                .map(it -> ((AnnotationAttributeImpl) it).jandexAnnotationAttribute)
                .collect(Collectors.toList());
        org.jboss.jandex.AnnotationInstance jandexAnnotation = org.jboss.jandex.AnnotationInstance.create(
                DotName.createSimple(annotationType.getName()), null, jandexAttributes);
        org.jboss.jandex.AnnotationValue jandexNestedAnnotation = org.jboss.jandex.AnnotationValue.createNestedAnnotationValue(
                name, jandexAnnotation);
        return new AnnotationAttributeImpl(jandexIndex, annotationOverlays, jandexNestedAnnotation);
    }

    @Override
    public AnnotationAttribute annotationAttribute(String name, ClassInfo<?> annotationType,
            AnnotationAttribute... attributes) {
        org.jboss.jandex.AnnotationValue[] jandexAnnotationAttributes = Arrays.stream(attributes)
                .map(it -> ((AnnotationAttributeImpl) it).jandexAnnotationAttribute)
                .toArray(org.jboss.jandex.AnnotationValue[]::new);
        org.jboss.jandex.AnnotationInstance jandexAnnotation = org.jboss.jandex.AnnotationInstance.create(
                ((ClassInfoImpl) annotationType).jandexDeclaration.name(), null, jandexAnnotationAttributes);
        org.jboss.jandex.AnnotationValue jandexNestedAnnotation = org.jboss.jandex.AnnotationValue.createNestedAnnotationValue(
                name, jandexAnnotation);
        return new AnnotationAttributeImpl(jandexIndex, annotationOverlays, jandexNestedAnnotation);
    }

    @Override
    public AnnotationAttribute annotationAttribute(String name, AnnotationInfo annotation) {
        org.jboss.jandex.AnnotationValue jandexNestedAnnotation = org.jboss.jandex.AnnotationValue.createNestedAnnotationValue(
                name, ((AnnotationInfoImpl) annotation).jandexAnnotation);
        return new AnnotationAttributeImpl(jandexIndex, annotationOverlays, jandexNestedAnnotation);
    }

    @Override
    public AnnotationAttribute annotationAttribute(String name, Annotation annotation) {
        return AnnotationsReflection.with(annotation, (jandexName, jandexAnnotationAttributes) -> {
            org.jboss.jandex.AnnotationInstance jandexAnnotation = org.jboss.jandex.AnnotationInstance.create(jandexName, null,
                    jandexAnnotationAttributes);
            org.jboss.jandex.AnnotationValue jandexAttribute = org.jboss.jandex.AnnotationValue.createNestedAnnotationValue(
                    name, jandexAnnotation);
            return new AnnotationAttributeImpl(jandexIndex, annotationOverlays, jandexAttribute);
        });
    }
}
