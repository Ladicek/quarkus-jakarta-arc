package cdi.lite.extension.model.declarations;

import cdi.lite.extension.model.types.Type;

public interface ParameterInfo extends DeclarationInfo {
    String name(); // TODO doesn't have to be present

    Type type();

    // ---

    @Override
    default Kind kind() {
        return Kind.PARAMETER;
    }

    @Override
    default ParameterInfo asParameter() {
        return this;
    }
}
