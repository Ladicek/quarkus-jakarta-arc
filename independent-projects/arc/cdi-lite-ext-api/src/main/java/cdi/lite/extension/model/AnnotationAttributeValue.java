package cdi.lite.extension.model;

import cdi.lite.extension.model.declarations.ClassInfo;
import cdi.lite.extension.model.types.Type;
import java.util.List;

public interface AnnotationAttributeValue {
    enum Kind {
        BOOLEAN,
        BYTE,
        SHORT,
        INT,
        LONG,
        FLOAT,
        DOUBLE,
        CHAR,
        STRING,
        ENUM,
        CLASS,
        ARRAY,
        NESTED_ANNOTATION,
    }

    Kind kind();

    default boolean isBoolean() {
        return kind() == Kind.BOOLEAN;
    }

    default boolean isByte() {
        return kind() == Kind.BYTE;
    }

    default boolean isShort() {
        return kind() == Kind.SHORT;
    }

    default boolean isInt() {
        return kind() == Kind.INT;
    }

    default boolean isLong() {
        return kind() == Kind.LONG;
    }

    default boolean isFloat() {
        return kind() == Kind.FLOAT;
    }

    default boolean isDouble() {
        return kind() == Kind.DOUBLE;
    }

    default boolean isChar() {
        return kind() == Kind.CHAR;
    }

    default boolean isString() {
        return kind() == Kind.STRING;
    }

    default boolean isEnum() {
        return kind() == Kind.ENUM;
    }

    default boolean isClass() {
        return kind() == Kind.CLASS;
    }

    default boolean isArray() {
        return kind() == Kind.ARRAY;
    }

    default boolean isNestedAnnotation() {
        return kind() == Kind.NESTED_ANNOTATION;
    }

    boolean asBoolean();

    byte asByte();

    short asShort();

    int asInt();

    long asLong();

    float asFloat();

    double asDouble();

    char asChar();

    String asString();

    String asEnumValue();

    ClassInfo<?> asEnumClass();

    Type asClass(); // can be a VoidType, PrimitiveType or ClassType

    List<AnnotationAttributeValue> asArray();

    AnnotationInfo asNestedAnnotation();
}
