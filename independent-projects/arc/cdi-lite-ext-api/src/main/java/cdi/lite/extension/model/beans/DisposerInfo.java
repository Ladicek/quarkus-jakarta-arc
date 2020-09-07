package cdi.lite.extension.model.beans;

import cdi.lite.extension.model.declarations.MethodInfo;
import cdi.lite.extension.model.declarations.ParameterInfo;

public interface DisposerInfo {
    MethodInfo<?> disposerMethod();

    ParameterInfo<?> disposedParameter();
}
