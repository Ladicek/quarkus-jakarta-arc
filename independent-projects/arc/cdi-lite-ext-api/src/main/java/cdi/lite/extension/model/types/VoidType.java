package cdi.lite.extension.model.types;

public interface VoidType extends Type {
    String name();

    // ---

    @Override
    default Kind kind() {
        return Kind.VOID;
    }

    @Override
    default VoidType asVoid() {
        return this;
    }
}
