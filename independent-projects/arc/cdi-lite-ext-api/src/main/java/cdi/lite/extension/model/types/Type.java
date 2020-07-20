package cdi.lite.extension.model.types;

import cdi.lite.extension.model.AnnotationTarget;

public interface Type extends AnnotationTarget {
    enum Kind {
        /** E.g. when method returns {@code void}. */
        VOID,
        /** E.g. when method returns {@code int}. */
        PRIMITIVE,
        /** E.g. when method returns {@code String}. */
        CLASS,
        /** E.g. when method returns {@code int[]} or {@code String[][]}. */
        ARRAY,
        /** E.g. when method returns {@code List<String>}. */
        PARAMETERIZED_TYPE,
        /** E.g. when method returns {@code T} and {@code T} is a type parameter of the declaring class. */
        TYPE_VARIABLE,
        /**
         * E.g. when method returns {@code List<? extends Number>}. On the first level, we have a {@code PARAMETERIZED_TYPE},
         * but on the second level, the first (and only) type argument is a {@code WILDCARD_TYPE}.
         */
        WILDCARD_TYPE,
    }

    Kind kind();

    default boolean isVoid() {
        return kind() == Kind.VOID;
    }

    default boolean isPrimitive() {
        return kind() == Kind.PRIMITIVE;
    }

    default boolean isClass() {
        return kind() == Kind.CLASS;
    }

    default boolean isArray() {
        return kind() == Kind.ARRAY;
    }

    default boolean isParameterizedType() {
        return kind() == Kind.PARAMETERIZED_TYPE;
    }

    default boolean isTypeVariable() {
        return kind() == Kind.TYPE_VARIABLE;
    }

    default boolean isWildcardType() {
        return kind() == Kind.WILDCARD_TYPE;
    }

    default VoidType asVoid() {
        throw new IllegalStateException("Not a void");
    }

    default PrimitiveType asPrimitive() {
        throw new IllegalStateException("Not a primitive");
    }

    default ClassType asClass() {
        throw new IllegalStateException("Not a class");
    }

    default ArrayType asArray() {
        throw new IllegalStateException("Not an array");
    }

    default ParameterizedType asParameterizedType() {
        throw new IllegalStateException("Not a parameterized type");
    }

    default TypeVariable asTypeVariable() {
        throw new IllegalStateException("Not a type variable");
    }

    default WildcardType asWildcardType() {
        throw new IllegalStateException("Not a wildcard type");
    }
}
