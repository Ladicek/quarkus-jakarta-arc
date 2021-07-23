package javax.enterprise.inject.build.compatible.spi;

import javax.enterprise.lang.model.AnnotationInfo;
import java.util.Collection;

public interface StereotypeInfo {
    // TODO null if not present, or return Optional?
    ScopeInfo defaultScope();

    Collection<AnnotationInfo> interceptorBindings();

    boolean isAlternative();

    // TODO https://github.com/eclipse-ee4j/cdi/issues/495
    //int priority();

    boolean isNamed();
}
