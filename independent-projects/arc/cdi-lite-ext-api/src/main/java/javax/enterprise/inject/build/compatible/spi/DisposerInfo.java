package javax.enterprise.inject.build.compatible.spi;

import javax.enterprise.lang.model.declarations.MethodInfo;
import javax.enterprise.lang.model.declarations.ParameterInfo;

public interface DisposerInfo {
    MethodInfo<?> disposerMethod();

    ParameterInfo disposedParameter();
}
