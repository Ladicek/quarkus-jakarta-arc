package cdi.lite.extension;

import cdi.lite.extension.beans.BeanInfo;
import cdi.lite.extension.beans.ObserverInfo;
import cdi.lite.extension.model.declarations.ClassInfo;
import cdi.lite.extension.model.types.Type;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.stream.Stream;

public interface AppDeployment {
    BeanQuery beans();

    ObserverQuery observers();

    /**
     * The {@code scope} methods are additive.
     * When called multiple times, they form a union of requested scope types (not an intersection).
     * For example,
     * <pre>{@code
     * appDeployment.beans()
     *     .scope(Foo.class)
     *     .scope(Bar.class)
     *     .find()
     * }</pre>
     * returns all beans with the {@code @Foo} scope or the {@code @Bar} scope.
     * <p>
     * The {@code type} methods are additive.
     * When called multiple times, they form a union of requested bean types (not an intersection).
     * For example,
     * <pre>{@code
     * appDeployment.beans()
     *     .type(Foo.class)
     *     .type(Bar.class)
     *     .find()
     * }</pre>
     * returns all beans with the {@code Foo} type or the {@code Bar} type (or both).
     * Note that bean type is not just the class which declares the bean (or return type of a producer method,
     * or type of producer field). All superclasses and superinterfaces are also included in the set of bean types.
     * <p>
     * The {@code qualifier} methods are additive.
     * When called multiple times, they form a union of requested qualifiers (not an intersection).
     * For example,
     * <pre>{@code
     * appDeployment.beans()
     *     .qualifier(Foo.class)
     *     .qualifier(Bar.class)
     *     .find()
     * }</pre>
     * returns all beans with the {@code @Foo} qualifier or the {@code @Bar} qualifier (or both).
     * <p>
     * The {@code declaringClass} methods are additive.
     * When called multiple times, they form a union of requested declaration classes (not an intersection).
     * For example,
     * <pre>{@code
     * appDeployment.beans()
     *     .declaringClass(Foo.class)
     *     .declaringClass(Bar.class)
     *     .find()
     * }</pre>
     * returns all beans declared on the {@code Foo} class or the {@code Bar} class.
     */
    interface BeanQuery {
        BeanQuery scope(Class<? extends Annotation> scopeAnnotation);

        BeanQuery scope(ClassInfo<?> scopeAnnotation);

        BeanQuery type(Class<?> beanType);

        BeanQuery type(ClassInfo<?> beanType);

        BeanQuery type(Type beanType);

        BeanQuery qualifier(Class<? extends Annotation> qualifierAnnotation);

        BeanQuery qualifier(ClassInfo<?> qualifierAnnotation);

        BeanQuery declaringClass(Class<?> declarationClass);

        BeanQuery declaringClass(ClassInfo<?> declarationClass);

        Collection<BeanInfo<?>> find();

        Stream<BeanInfo<?>> stream();
    }

    /**
     * The {@code observedType} methods are additive.
     * When called multiple times, they form a union of requested observer types (not an intersection).
     * For example,
     * <pre>{@code
     * appDeployment.observers()
     *     .observedType(Foo.class)
     *     .observedType(Bar.class)
     *     .find()
     * }</pre>
     * returns all observers that observe the {@code Foo} event type or the {@code Bar} event type.
     * <p>
     * The {@code qualifier} methods are additive.
     * When called multiple times, they form a union of requested qualifiers (not an intersection).
     * For example,
     * <pre>{@code
     * appDeployment.observers()
     *     .qualifier(Foo.class)
     *     .qualifier(Bar.class)
     *     .find()
     * }</pre>
     * returns all observers with the {@code @Foo} qualifier or the {@code @Bar} qualifier (or both).
     * <p>
     * The {@code declaringClass} methods are additive.
     * When called multiple times, they form a union of requested declaration classes (not an intersection).
     * For example,
     * <pre>{@code
     * appDeployment.observers()
     *     .declaringClass(Foo.class)
     *     .declaringClass(Bar.class)
     *     .find()
     * }</pre>
     * returns all observers declared on the {@code Foo} class or the {@code Bar} class.
     */
    interface ObserverQuery {
        ObserverQuery observedType(Class<?> beanType);

        ObserverQuery observedType(ClassInfo<?> beanType);

        ObserverQuery observedType(Type beanType);

        ObserverQuery qualifier(Class<? extends Annotation> qualifierAnnotation);

        ObserverQuery qualifier(ClassInfo<?> qualifierAnnotation);

        ObserverQuery declaringClass(Class<?> declarationClass);

        ObserverQuery declaringClass(ClassInfo<?> declarationClass);

        Collection<ObserverInfo<?>> find();

        Stream<ObserverInfo<?>> stream();
    }
}
