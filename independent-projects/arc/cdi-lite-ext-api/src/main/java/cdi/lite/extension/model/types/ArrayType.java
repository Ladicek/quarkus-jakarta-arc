package cdi.lite.extension.model.types;

public interface ArrayType extends Type {
    int dimensions();

    Type componentType();

    // ---

    @Override
    default Kind kind() {
        return Kind.ARRAY;
    }

    @Override
    default ArrayType asArray() {
        return this;
    }
}
