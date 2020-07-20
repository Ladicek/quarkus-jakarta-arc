package cdi.lite.extension.model.declarations;

import cdi.lite.extension.model.AnnotationTarget;

public interface DeclarationInfo extends AnnotationTarget {
    enum Kind {
        PACKAGE,
        CLASS,
        METHOD,
        PARAMETER,
        FIELD,
    }

    Kind kind();

    default boolean isPackage() {
        return kind() == Kind.PACKAGE;
    }

    default boolean isClass() {
        return kind() == Kind.CLASS;
    }

    default boolean isMethod() {
        return kind() == Kind.METHOD;
    }

    default boolean isParameter() {
        return kind() == Kind.PARAMETER;
    }

    default boolean isField() {
        return kind() == Kind.FIELD;
    }

    default PackageInfo asPackage() {
        throw new IllegalStateException("Not a package");
    }

    default ClassInfo<?> asClass() {
        throw new IllegalStateException("Not a class");
    }

    default MethodInfo<?> asMethod() {
        throw new IllegalStateException("Not a method");
    }

    default ParameterInfo<?> asParameter() {
        throw new IllegalStateException("Not a parameter");
    }

    default FieldInfo<?> asField() {
        throw new IllegalStateException("Not a field");
    }
}
