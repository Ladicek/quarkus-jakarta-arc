package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.model.AnnotationAttributeValue;
import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.declarations.ClassInfo;
import cdi.lite.extension.model.types.Type;
import java.util.ArrayList;
import java.util.List;

class AnnotationAttributeValueImpl implements AnnotationAttributeValue {
    private final org.jboss.jandex.IndexView jandexIndex;
    private final org.jboss.jandex.AnnotationValue jandexAnnotationAttribute;

    AnnotationAttributeValueImpl(org.jboss.jandex.IndexView jandexIndex,
            org.jboss.jandex.AnnotationValue jandexAnnotationAttribute) {
        this.jandexIndex = jandexIndex;
        this.jandexAnnotationAttribute = jandexAnnotationAttribute;
    }

    @Override
    public Kind kind() {
        switch (jandexAnnotationAttribute.kind()) {
            case BOOLEAN:
                return Kind.BOOLEAN;
            case BYTE:
                return Kind.BYTE;
            case SHORT:
                return Kind.SHORT;
            case INTEGER:
                return Kind.INT;
            case LONG:
                return Kind.LONG;
            case FLOAT:
                return Kind.FLOAT;
            case DOUBLE:
                return Kind.DOUBLE;
            case CHARACTER:
                return Kind.CHAR;
            case STRING:
                return Kind.STRING;
            case ENUM:
                return Kind.ENUM;
            case CLASS:
                return Kind.CLASS;
            case ARRAY:
                return Kind.ARRAY;
            case NESTED:
                return Kind.NESTED_ANNOTATION;
            default:
                throw new IllegalStateException("Unknown annotation attribute " + jandexAnnotationAttribute);
        }
    }

    @Override
    public boolean asBoolean() {
        return jandexAnnotationAttribute.asBoolean();
    }

    @Override
    public byte asByte() {
        return jandexAnnotationAttribute.asByte();
    }

    @Override
    public short asShort() {
        return jandexAnnotationAttribute.asShort();
    }

    @Override
    public int asInt() {
        return jandexAnnotationAttribute.asInt();
    }

    @Override
    public long asLong() {
        return jandexAnnotationAttribute.asLong();
    }

    @Override
    public float asFloat() {
        return jandexAnnotationAttribute.asFloat();
    }

    @Override
    public double asDouble() {
        return jandexAnnotationAttribute.asDouble();
    }

    @Override
    public char asChar() {
        return jandexAnnotationAttribute.asChar();
    }

    @Override
    public String asString() {
        return jandexAnnotationAttribute.asString();
    }

    // TODO make this part of public API?
    public <T extends Enum<T>> T asEnum() {
        try {
            @SuppressWarnings("unchecked")
            Class<T> enumClass = (Class<T>) Class.forName(jandexAnnotationAttribute.asEnumType().toString(), true,
                    Thread.currentThread().getContextClassLoader());
            return Enum.valueOf(enumClass, jandexAnnotationAttribute.asEnum());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String asEnumValue() {
        return jandexAnnotationAttribute.asEnum();
    }

    @Override
    public ClassInfo<?> asEnumClass() {
        return new ClassInfoImpl(jandexIndex, jandexIndex.getClassByName(jandexAnnotationAttribute.asEnumType()));
    }

    @Override
    public Type asClass() {
        return TypeImpl.fromJandexType(jandexIndex, jandexAnnotationAttribute.asClass());
    }

    @Override
    public List<AnnotationAttributeValue> asArray() {
        org.jboss.jandex.AnnotationValue[] array = new org.jboss.jandex.HackAnnotationValue(jandexAnnotationAttribute)
                .asArray();
        List<AnnotationAttributeValue> result = new ArrayList<>(array.length);
        for (org.jboss.jandex.AnnotationValue value : array) {
            result.add(new AnnotationAttributeValueImpl(jandexIndex, value));
        }
        return result;
    }

    @Override
    public AnnotationInfo asNestedAnnotation() {
        return new AnnotationInfoImpl(jandexIndex, jandexAnnotationAttribute.asNested());
    }
}
