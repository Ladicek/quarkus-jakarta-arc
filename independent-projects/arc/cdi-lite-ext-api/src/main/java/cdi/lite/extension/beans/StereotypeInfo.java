package cdi.lite.extension.beans;

import cdi.lite.extension.model.AnnotationInfo;
import java.util.Collection;

public interface StereotypeInfo {
    // TODO null if not present, or return Optional?
    ScopeInfo defaultScope();

    Collection<AnnotationInfo> interceptorBindings();

    boolean isAlternative();

    // TODO CDI-695
    //int priority();

    boolean isNamed();
}
