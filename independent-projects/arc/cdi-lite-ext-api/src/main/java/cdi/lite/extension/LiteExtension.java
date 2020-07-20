package cdi.lite.extension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An extension is a {@code public}, non-{@code static}, {@code void}-returning method without type parameters,
 * annotated {@code LiteExtension} and declared on a {@code public} class with a {@code public} zero-parameter constructor.
 * This class should not be a CDI bean and should not be used by the application at runtime.
 * <p>
 * Extension method can declare arbitrary number of parameters that take one of the following forms:
 * <ul>
 * <li>{@code ClassConfig<MyService>}: configurator for the one exact type</li>
 * <li>{@code Collection<ClassConfig<MyService>>}: configurator for the one exact type, equivalent to previous line</li>
 * <li>{@code Collection<ClassConfig<? extends MyService>>}: configurators for all subtypes</li>
 * <li>{@code Collection<ClassConfig<? super MyService>>}: configurators for all supertypes</li>
 * <li>{@code Collection<ClassConfig<?>>}: configurators for all present types</li>
 * <li>TODO {@code Stream} instead of / in addition to {@code Collection}?</li>
 * </ul>
 * <p>
 * Same for {@code MethodConfig}, {@code ConstructorConfig}, {@code FieldConfig} and {@code ParameterConfig}.
 * In these cases, the type parameter always expresses a query for the types that declare the configured method,
 * constructor, field or parameter.
 * For example, if a class {@code MyService} has 2 methods and each of them has 2 parameters, then
 * {@code Collection<ParameterConfig<MyService>>} has 4 elements.
 * <p>
 * It is possible to further narrow down the query by using {@link WithAnnotations} and {@link WithTypes}.
 * <p>
 * For advanced use cases, where this kind of queries is not powerful enough, the extension method can also declare
 * a parameter of type {@link World}. If you declare a parameter of type {@code Collection<TypeConfigurator<?>>},
 * that's a good sign you probably want to use {@code World}.
 * <p>
 * All the parameters will be provided by the container when the extension is invoked.
 * <p>
 * If a class declares multiple extensions, they are all invoked on the same instance of the class.
 * Extension can be assigned a priority via {@link ExtensionPriority}.
 * TODO use @javax.annotation.Priority instead?
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME) // must be RUNTIME, because Jandex ignores class-only annotations
public @interface LiteExtension {
}
