package cdi.lite.extension.model.types;

import cdi.lite.extension.model.declarations.ClassInfo;

public interface ClassType extends Type {
    ClassInfo<?> declaration();

    // ---

    @Override
    default Kind kind() {
        return Kind.CLASS;
    }

    @Override
    default ClassType asClass() {
        return this;
    }
}
