package cdi.lite.extension.model.declarations;

import cdi.lite.extension.model.types.Type;

/**
 * @param <T> type of whomever declares the method or constructor that has the inspected parameter
 */
public interface ParameterInfo<T> extends DeclarationInfo {
    String name(); // TODO doesn't have to be present

    Type type();

    // ---

    @Override
    default Kind kind() {
        return Kind.PARAMETER;
    }

    @Override
    default ParameterInfo<?> asParameter() {
        return this;
    }
}
