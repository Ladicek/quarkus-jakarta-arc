package cdi.lite.extension.beans;

import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.declarations.ClassInfo;
import cdi.lite.extension.model.declarations.MethodInfo;
import cdi.lite.extension.model.declarations.ParameterInfo;
import cdi.lite.extension.model.types.Type;
import java.util.Collection;
import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;

/**
 * @param <T> type observed by the inspected observer
 */
public interface ObserverInfo<T> {
    String id(); // ???

    Type observedType();

    // TODO method(s) for getting AnnotationInfo for given qualifier class?
    Collection<AnnotationInfo> qualifiers();

    ClassInfo<?> declaringClass(); // never null, even if synthetic

    MethodInfo<?> observerMethod(); // TODO null for synthetic observers, or return Optional? see also isSynthetic below

    ParameterInfo eventParameter(); // TODO null for synthetic observers, or return Optional? see also isSynthetic below

    BeanInfo<?> bean(); // TODO null for synthetic observers, or return Optional? see also isSynthetic below

    default boolean isSynthetic() {
        return bean() == null;
    }

    int priority();

    boolean isAsync();

    Reception reception();

    TransactionPhase transactionPhase();
}
