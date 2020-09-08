package cdi.lite.extension.beans;

import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.declarations.DeclarationInfo;
import cdi.lite.extension.model.types.Type;
import java.util.Collection;

public interface InjectionPointInfo {
    Type type();

    // TODO method(s) for getting AnnotationInfo for given qualifier class?
    Collection<AnnotationInfo> qualifiers();

    /**
     * Returns a {@code FieldInfo} for field injection, or {@code ParameterInfo} for:
     * <ul>
     * <li>constructor injection,</li>
     * <li>initializer method,</li>
     * <li>disposer method,</li>
     * <li>producer method,</li>
     * <li>observer method.</li>
     */
    DeclarationInfo declaration();
}
