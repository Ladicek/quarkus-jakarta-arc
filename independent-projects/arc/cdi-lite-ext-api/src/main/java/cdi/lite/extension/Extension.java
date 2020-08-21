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
 * <li>information about classes:</li>
 * <ul>
 * <li>{@code ClassInfo<MyService>}: information about the one exact class</li>
 * <li>{@code Collection<ClassInfo<MyService>>}: information about the one exact class, equivalent to previous line</li>
 * <li>{@code Collection<ClassInfo<? extends MyService>>}: information about all subclasses</li>
 * <li>{@code Collection<ClassInfo<? super MyService>>}: information about all superclasses</li>
 * <li>{@code Collection<ClassInfo<?>>}: information about all present classes</li>
 * <li>TODO {@code Stream} instead of / in addition to {@code Collection}?</li>
 * </ul>
 * <li>information about methods:</li>
 * <ul>
 * <li>{@code Collection<MethodInfo<MyService>>}: information about methods declared on one exact class</li>
 * <li>{@code Collection<MethodInfo<? extends MyService>>}: information about methods declared on all subclasses</li>
 * <li>{@code Collection<MethodInfo<? super MyService>>}: information about methods declared on all superclasses</li>
 * <li>{@code Collection<MethodInfo<?>>}: information about methods on all present classes</li>
 * <li>TODO {@code Stream} instead of / in addition to {@code Collection}?</li>
 * </ul>
 * <li>information about method parameters:</li>
 * <ul>
 * <li>{@code Collection<ParameterInfo<MyService>>}: information about method parameters declared on one exact class</li>
 * <li>{@code Collection<ParameterInfo<? extends MyService>>}: information about method parameters declared on all
 * subclasses</li>
 * <li>{@code Collection<ParameterInfo<? super MyService>>}: information about method parameters declared on all
 * superclasses</li>
 * <li>{@code Collection<ParameterInfo<?>>}: information about method parameters declared on all present classes</li>
 * <li>TODO {@code Stream} instead of / in addition to {@code Collection}?</li>
 * </ul>
 * <li>information about fields:</li>
 * <ul>
 * <li>{@code Collection<FieldInfo<MyService>>}: information about fields declared on one exact class</li>
 * <li>{@code Collection<FieldInfo<? extends MyService>>}: information about fields declared on all subclasses</li>
 * <li>{@code Collection<FieldInfo<? super MyService>>}: information about fields declared on all superclasses</li>
 * <li>{@code Collection<FieldInfo<?>>}: information about fields declared on all present classes</li>
 * <li>TODO {@code Stream} instead of / in addition to {@code Collection}?</li>
 * </ul>
 * <li>configuration of classes:</li>
 * <ul>
 * <li>{@code ClassConfig<MyService>}: configurator for the one exact class</li>
 * <li>{@code Collection<ClassConfig<MyService>>}: configurator for the one exact class, equivalent to previous line</li>
 * <li>{@code Collection<ClassConfig<? extends MyService>>}: configurators for all subclasses</li>
 * <li>{@code Collection<ClassConfig<? super MyService>>}: configurators for all superclasses</li>
 * <li>{@code Collection<ClassConfig<?>>}: configurators for all present classes</li>
 * <li>TODO {@code Stream} instead of / in addition to {@code Collection}?</li>
 * </ul>
 * </ul>
 * <p>
 * It is possible to further narrow down these queries by {@link WithAnnotations} and {@link WithTypes}.
 * <p>
 * For advanced use cases, where this kind of queries is not powerful enough, the extension method can also declare
 * a parameter of type {@link World}. If you declare a parameter of shape {@code Collection<SomethingInfo<?>>}
 * or {@code Collection<SomethingConfig<?>>}, that's a good sign you probably want to use {@code World}.
 * <p>
 * If you need to create instances of {@link cdi.lite.extension.model.types.Type Type}, you can also declare
 * a parameter of type {@link Types}. It provides factory methods for common types, such as {@code void},
 * primitive types, class types and array types. Other types, such as parameterized types or type variables,
 * can't be created like this and can only be retrieved from declarations that use them.
 * <p>
 * All the parameters will be provided by the container when the extension is invoked.
 * <p>
 * If a class declares multiple extensions, they are all invoked on the same instance of the class.
 * Extension can be assigned a priority via {@link ExtensionPriority}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME) // runtime implementations should be possible; also Jandex ignores class-only annotations
public @interface Extension {
}
