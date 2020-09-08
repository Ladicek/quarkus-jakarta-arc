package cdi.lite.extension.beans;

import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.declarations.ClassInfo;
import cdi.lite.extension.model.declarations.FieldInfo;
import cdi.lite.extension.model.declarations.MethodInfo;
import cdi.lite.extension.model.types.Type;
import java.util.Collection;

/**
 * @param <T> type of the inspected bean (that is, not the declaring class, but one of the types the bean has)
 */
public interface BeanInfo<T> {
    ScopeInfo scope();

    Collection<Type> types();

    // TODO method(s) for getting AnnotationInfo for given qualifier class?
    Collection<AnnotationInfo> qualifiers();

    /**
     * Returns the class that declares the bean.
     * In case of a bean defined by a class, that is the bean class directly.
     * In case of a producer method or field, that is the class that declares the producer method or field.
     * TODO null for synthetic beans, or return Optional?
     */
    ClassInfo<?> declaringClass();

    boolean isClassBean();

    boolean isProducerMethod();

    boolean isProducerField();

    boolean isSynthetic();

    MethodInfo<?> producerMethod(); // TODO null if not producer method, or return Optional?

    FieldInfo<?> producerField(); // TODO null if not producer field, or return Optional?

    boolean isAlternative();

    int priority();

    // EL name (from @Named), IIUC
    String getName();

    DisposerInfo disposer(); // TODO is this necessary?

    Collection<StereotypeInfo> stereotypes();

    // TODO interceptors?

    Collection<InjectionPointInfo> injectionPoints();
}
