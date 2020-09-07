package cdi.lite.extension.model.beans;

import cdi.lite.extension.model.declarations.ClassInfo;

public interface ScopeInfo {
    ClassInfo<?> annotation();

    boolean isNormal();
}
