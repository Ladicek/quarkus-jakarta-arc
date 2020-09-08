package cdi.lite.extension.beans;

import cdi.lite.extension.model.declarations.MethodInfo;
import cdi.lite.extension.model.declarations.ParameterInfo;

public interface DisposerInfo {
    MethodInfo<?> disposerMethod();

    ParameterInfo disposedParameter();
}
