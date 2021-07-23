package javax.enterprise.inject.build.compatible.spi;

import javax.enterprise.lang.model.AnnotationInfo;
import javax.enterprise.lang.model.declarations.ClassInfo;
import javax.enterprise.lang.model.declarations.MethodInfo;
import javax.enterprise.lang.model.declarations.ParameterInfo;
import javax.enterprise.lang.model.types.Type;
import java.util.Collection;
import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;

/**
 * @param <T> type observed by the inspected observer
 */
public interface ObserverInfo<T> {
    // TODO remove the type parameter?

    String id(); // TODO remove entirely?

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
