package cdi.lite.extension.beans;

import cdi.lite.extension.model.declarations.ClassInfo;

public interface ScopeInfo {
    ClassInfo<?> annotation();

    /**
     * Equivalent to {@code annotation().name()}.
     */
    default String name() {
        return annotation().name();
    }

    boolean isNormal();
}
