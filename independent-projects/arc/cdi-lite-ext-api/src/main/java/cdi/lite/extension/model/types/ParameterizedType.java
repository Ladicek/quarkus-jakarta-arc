package cdi.lite.extension.model.types;

import cdi.lite.extension.model.declarations.ClassInfo;
import java.util.List;

public interface ParameterizedType extends Type {
    ClassInfo<?> declaration();

    List<Type> typeArguments();

    // ---

    @Override
    default Kind kind() {
        return Kind.PARAMETERIZED_TYPE;
    }

    @Override
    default ParameterizedType asParameterizedType() {
        return this;
    }
}
