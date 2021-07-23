package javax.enterprise.lang.model.types;

import javax.enterprise.lang.model.declarations.ClassInfo;

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
