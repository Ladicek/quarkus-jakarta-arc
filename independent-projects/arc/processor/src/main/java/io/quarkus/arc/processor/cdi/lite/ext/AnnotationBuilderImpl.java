package io.quarkus.arc.processor.cdi.lite.ext;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.inject.build.compatible.spi.AnnotationBuilder;
import javax.enterprise.lang.model.AnnotationInfo;
import javax.enterprise.lang.model.AnnotationMember;
import javax.enterprise.lang.model.declarations.ClassInfo;
import javax.enterprise.lang.model.types.Type;
import org.jboss.jandex.DotName;

class AnnotationBuilderImpl implements AnnotationBuilder {
    private final org.jboss.jandex.IndexView jandexIndex;
    private final AllAnnotationOverlays annotationOverlays;

    private final DotName jandexClassName;
    private final List<org.jboss.jandex.AnnotationValue> jandexAnnotationMembers = new ArrayList<>();

    AnnotationBuilderImpl(org.jboss.jandex.IndexView jandexIndex, AllAnnotationOverlays annotationOverlays, DotName jandexAnnotationName) {
        this.jandexIndex = jandexIndex;
        this.annotationOverlays = annotationOverlays;
        this.jandexClassName = jandexAnnotationName;
    }

    @Override
    public AnnotationBuilder member(String name, AnnotationMember value) {
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, boolean value) {
        jandexAnnotationMembers.add(org.jboss.jandex.AnnotationValue.createBooleanValue(name, value));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, boolean... values) {
        org.jboss.jandex.AnnotationValue[] array = new org.jboss.jandex.AnnotationValue[values.length];
        for (int i = 0; i < values.length; i++) {
            array[i] = org.jboss.jandex.AnnotationValue.createBooleanValue(name, values[i]);
        }
        jandexAnnotationMembers.add(org.jboss.jandex.AnnotationValue.createArrayValue(name, array));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, byte value) {
        jandexAnnotationMembers.add(org.jboss.jandex.AnnotationValue.createByteValue(name, value));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, byte... values) {
        org.jboss.jandex.AnnotationValue[] array = new org.jboss.jandex.AnnotationValue[values.length];
        for (int i = 0; i < values.length; i++) {
            array[i] = org.jboss.jandex.AnnotationValue.createByteValue(name, values[i]);
        }
        jandexAnnotationMembers.add(org.jboss.jandex.AnnotationValue.createArrayValue(name, array));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, short value) {
        jandexAnnotationMembers.add(org.jboss.jandex.AnnotationValue.createShortValue(name, value));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, short... values) {
        org.jboss.jandex.AnnotationValue[] array = new org.jboss.jandex.AnnotationValue[values.length];
        for (int i = 0; i < values.length; i++) {
            array[i] = org.jboss.jandex.AnnotationValue.createShortValue(name, values[i]);
        }
        jandexAnnotationMembers.add(org.jboss.jandex.AnnotationValue.createArrayValue(name, array));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, int value) {
        jandexAnnotationMembers.add(org.jboss.jandex.AnnotationValue.createIntegerValue(name, value));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, int... values) {
        org.jboss.jandex.AnnotationValue[] array = new org.jboss.jandex.AnnotationValue[values.length];
        for (int i = 0; i < values.length; i++) {
            array[i] = org.jboss.jandex.AnnotationValue.createIntegerValue(name, values[i]);
        }
        jandexAnnotationMembers.add(org.jboss.jandex.AnnotationValue.createArrayValue(name, array));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, long value) {
        jandexAnnotationMembers.add(org.jboss.jandex.AnnotationValue.createLongValue(name, value));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, long... values) {
        org.jboss.jandex.AnnotationValue[] array = new org.jboss.jandex.AnnotationValue[values.length];
        for (int i = 0; i < values.length; i++) {
            array[i] = org.jboss.jandex.AnnotationValue.createLongValue(name, values[i]);
        }
        jandexAnnotationMembers.add(org.jboss.jandex.AnnotationValue.createArrayValue(name, array));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, float value) {
        jandexAnnotationMembers.add(org.jboss.jandex.AnnotationValue.createFloatValue(name, value));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, float... values) {
        org.jboss.jandex.AnnotationValue[] array = new org.jboss.jandex.AnnotationValue[values.length];
        for (int i = 0; i < values.length; i++) {
            array[i] = org.jboss.jandex.AnnotationValue.createFloatValue(name, values[i]);
        }
        jandexAnnotationMembers.add(org.jboss.jandex.AnnotationValue.createArrayValue(name, array));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, double value) {
        jandexAnnotationMembers.add(org.jboss.jandex.AnnotationValue.createDoubleValue(name, value));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, double... values) {
        org.jboss.jandex.AnnotationValue[] array = new org.jboss.jandex.AnnotationValue[values.length];
        for (int i = 0; i < values.length; i++) {
            array[i] = org.jboss.jandex.AnnotationValue.createDoubleValue(name, values[i]);
        }
        jandexAnnotationMembers.add(org.jboss.jandex.AnnotationValue.createArrayValue(name, array));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, char value) {
        jandexAnnotationMembers.add(org.jboss.jandex.AnnotationValue.createCharacterValue(name, value));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, char... values) {
        org.jboss.jandex.AnnotationValue[] array = new org.jboss.jandex.AnnotationValue[values.length];
        for (int i = 0; i < values.length; i++) {
            array[i] = org.jboss.jandex.AnnotationValue.createCharacterValue(name, values[i]);
        }
        jandexAnnotationMembers.add(org.jboss.jandex.AnnotationValue.createArrayValue(name, array));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, String value) {
        jandexAnnotationMembers.add(org.jboss.jandex.AnnotationValue.createStringValue(name, value));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, String... values) {
        org.jboss.jandex.AnnotationValue[] array = new org.jboss.jandex.AnnotationValue[values.length];
        for (int i = 0; i < values.length; i++) {
            array[i] = org.jboss.jandex.AnnotationValue.createStringValue(name, values[i]);
        }
        jandexAnnotationMembers.add(org.jboss.jandex.AnnotationValue.createArrayValue(name, array));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, Enum<?> value) {
        DotName enumTypeName = DotName.createSimple(value.getDeclaringClass().getName());
        String enumValue = value.name();
        jandexAnnotationMembers.add(org.jboss.jandex.AnnotationValue.createEnumValue(name, enumTypeName, enumValue));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, Enum<?>... values) {
        org.jboss.jandex.AnnotationValue[] array = new org.jboss.jandex.AnnotationValue[values.length];
        for (int i = 0; i < values.length; i++) {
            DotName enumTypeName = DotName.createSimple(values[i].getDeclaringClass().getName());
            String enumValue = values[i].name();
            array[i] = org.jboss.jandex.AnnotationValue.createEnumValue(name, enumTypeName, enumValue);
        }
        jandexAnnotationMembers.add(org.jboss.jandex.AnnotationValue.createArrayValue(name, array));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, Class<? extends Enum<?>> enumType, String enumValue) {
        DotName enumTypeName = DotName.createSimple(enumType.getName());
        jandexAnnotationMembers.add(org.jboss.jandex.AnnotationValue.createEnumValue(name, enumTypeName, enumValue));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, Class<? extends Enum<?>> enumType, String... enumValues) {
        org.jboss.jandex.AnnotationValue[] array = new org.jboss.jandex.AnnotationValue[enumValues.length];
        DotName enumTypeName = DotName.createSimple(enumType.getName());
        for (int i = 0; i < enumValues.length; i++) {
            array[i] = org.jboss.jandex.AnnotationValue.createEnumValue(name, enumTypeName, enumValues[i]);
        }
        jandexAnnotationMembers.add(org.jboss.jandex.AnnotationValue.createArrayValue(name, array));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, ClassInfo<?> enumType, String enumValue) {
        DotName enumTypeName = ((ClassInfoImpl) enumType).jandexDeclaration.name();
        jandexAnnotationMembers.add(org.jboss.jandex.AnnotationValue.createEnumValue(name, enumTypeName, enumValue));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, ClassInfo<?> enumType, String... enumValues) {
        org.jboss.jandex.AnnotationValue[] array = new org.jboss.jandex.AnnotationValue[enumValues.length];
        DotName enumTypeName = ((ClassInfoImpl) enumType).jandexDeclaration.name();
        for (int i = 0; i < enumValues.length; i++) {
            array[i] = org.jboss.jandex.AnnotationValue.createEnumValue(name, enumTypeName, enumValues[i]);
        }
        jandexAnnotationMembers.add(org.jboss.jandex.AnnotationValue.createArrayValue(name, array));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, Class<?> value) {
        jandexAnnotationMembers.add(org.jboss.jandex.AnnotationValue.createClassValue(name, TypesReflection.jandexType(value)));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, Class<?>... values) {
        org.jboss.jandex.AnnotationValue[] array = new org.jboss.jandex.AnnotationValue[values.length];
        for (int i = 0; i < values.length; i++) {
            array[i] = org.jboss.jandex.AnnotationValue.createClassValue(name, TypesReflection.jandexType(values[i]));
        }
        jandexAnnotationMembers.add(org.jboss.jandex.AnnotationValue.createArrayValue(name, array));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, ClassInfo<?> value) {
        DotName className = ((ClassInfoImpl) value).jandexDeclaration.name();
        org.jboss.jandex.Type jandexClass = org.jboss.jandex.Type.create(className, org.jboss.jandex.Type.Kind.CLASS);
        jandexAnnotationMembers.add(org.jboss.jandex.AnnotationValue.createClassValue(name, jandexClass));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, ClassInfo<?>... values) {
        org.jboss.jandex.AnnotationValue[] array = new org.jboss.jandex.AnnotationValue[values.length];
        for (int i = 0; i < values.length; i++) {
            DotName className = ((ClassInfoImpl) values[i]).jandexDeclaration.name();
            org.jboss.jandex.Type jandexClass = org.jboss.jandex.Type.create(className, org.jboss.jandex.Type.Kind.CLASS);
            array[i] = org.jboss.jandex.AnnotationValue.createClassValue(name, jandexClass);
        }
        jandexAnnotationMembers.add(org.jboss.jandex.AnnotationValue.createArrayValue(name, array));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, Type value) {
        org.jboss.jandex.Type jandexClass = ((TypeImpl<?>) value).jandexType;
        jandexAnnotationMembers.add(org.jboss.jandex.AnnotationValue.createClassValue(name, jandexClass));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, Type... values) {
        org.jboss.jandex.AnnotationValue[] array = new org.jboss.jandex.AnnotationValue[values.length];
        for (int i = 0; i < values.length; i++) {
            org.jboss.jandex.Type jandexClass = ((TypeImpl<?>) values[i]).jandexType;
            array[i] = org.jboss.jandex.AnnotationValue.createClassValue(name, jandexClass);
        }
        jandexAnnotationMembers.add(org.jboss.jandex.AnnotationValue.createArrayValue(name, array));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, AnnotationInfo<?> value) {
        org.jboss.jandex.AnnotationInstance jandexAnnotation = ((AnnotationInfoImpl<?>) value).jandexAnnotation;
        jandexAnnotationMembers.add(org.jboss.jandex.AnnotationValue.createNestedAnnotationValue(name, jandexAnnotation));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, AnnotationInfo<?>... values) {
        org.jboss.jandex.AnnotationValue[] array = new org.jboss.jandex.AnnotationValue[values.length];
        for (int i = 0; i < values.length; i++) {
            org.jboss.jandex.AnnotationInstance jandexAnnotation = ((AnnotationInfoImpl<?>) values[i]).jandexAnnotation;
            array[i] = org.jboss.jandex.AnnotationValue.createNestedAnnotationValue(name, jandexAnnotation);
        }
        jandexAnnotationMembers.add(org.jboss.jandex.AnnotationValue.createArrayValue(name, array));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, Annotation value) {
        org.jboss.jandex.AnnotationInstance jandexAnnotation = AnnotationsReflection.jandexAnnotation(value);
        jandexAnnotationMembers.add(org.jboss.jandex.AnnotationValue.createNestedAnnotationValue(name, jandexAnnotation));
        return this;
    }

    @Override
    public AnnotationBuilder member(String name, Annotation... values) {
        org.jboss.jandex.AnnotationValue[] array = new org.jboss.jandex.AnnotationValue[values.length];
        for (int i = 0; i < values.length; i++) {
            org.jboss.jandex.AnnotationInstance jandexAnnotation = AnnotationsReflection.jandexAnnotation(values[i]);
            array[i] = org.jboss.jandex.AnnotationValue.createNestedAnnotationValue(name, jandexAnnotation);
        }
        jandexAnnotationMembers.add(org.jboss.jandex.AnnotationValue.createArrayValue(name, array));
        return this;
    }

    @Override
    public AnnotationInfo<?> build() {
        org.jboss.jandex.AnnotationInstance jandexAnnotation = org.jboss.jandex.AnnotationInstance.create(
                jandexClassName, null, jandexAnnotationMembers);
        return new AnnotationInfoImpl<>(jandexIndex, annotationOverlays, jandexAnnotation);
    }
}
