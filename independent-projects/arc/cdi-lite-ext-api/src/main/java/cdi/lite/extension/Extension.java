package cdi.lite.extension;

import cdi.lite.extension.phases.enhancement.Annotations;
import cdi.lite.extension.phases.enhancement.ClassConfig;
import cdi.lite.extension.phases.enhancement.FieldConfig;
import cdi.lite.extension.phases.enhancement.MethodConfig;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * TODO this will go away, will be replaced by the annotations in {@code phases} package.
 *  Leaving it intact for now, because the javadoc should be copied elsewhere, as appropriate
 * <hr>
 *
 * An extension is a {@code public}, non-{@code static}, {@code void}-returning method without type parameters,
 * annotated with {@code @Extension}. The method must be declared on a {@code public} class without type parameters
 * and with a {@code public} zero-parameter constructor.
 * This class should not be a CDI bean and should not be used by the application at runtime.
 * <p>
 * Extension methods can declare arbitrary number of parameters that take one of the following forms:
 * <ul>
 * <li>information about classes ({@link cdi.lite.extension.model.declarations.ClassInfo ClassInfo}):</li>
 * <ul>
 * <li>{@code ClassInfo<MyService>}: information about one exact class</li>
 * <li>{@code Collection<ClassInfo<MyService>>}: information about the one exact class, equivalent to previous line</li>
 * <li>{@code Collection<ClassInfo<? extends MyService>>}: information about all subclasses</li>
 * <li>{@code Collection<ClassInfo<? super MyService>>}: information about all superclasses</li>
 * <li>{@code Collection<ClassInfo<?>>}: information about all present classes</li>
 * <li>TODO {@code Stream} instead of / in addition to {@code Collection}?</li>
 * </ul>
 * <li>information about methods ({@link cdi.lite.extension.model.declarations.MethodInfo MethodInfo}):</li>
 * <ul>
 * <li>{@code Collection<MethodInfo<MyService>>}: information about methods declared on one exact class</li>
 * <li>{@code Collection<MethodInfo<? extends MyService>>}: information about methods declared on all subclasses</li>
 * <li>{@code Collection<MethodInfo<? super MyService>>}: information about methods declared on all superclasses</li>
 * <li>{@code Collection<MethodInfo<?>>}: information about methods on all present classes</li>
 * <li>TODO {@code Stream} instead of / in addition to {@code Collection}?</li>
 * </ul>
 * <li>information about method parameters ({@link cdi.lite.extension.model.declarations.ParameterInfo ParameterInfo}):</li>
 * <ul>
 * <li>{@code Collection<ParameterInfo<MyService>>}: information about method parameters declared on one exact class</li>
 * <li>{@code Collection<ParameterInfo<? extends MyService>>}: information about method parameters declared on all
 * subclasses</li>
 * <li>{@code Collection<ParameterInfo<? super MyService>>}: information about method parameters declared on all
 * superclasses</li>
 * <li>{@code Collection<ParameterInfo<?>>}: information about method parameters declared on all present classes</li>
 * <li>TODO {@code Stream} instead of / in addition to {@code Collection}?</li>
 * <li>TODO is this actually useful? perhaps remove this, as ParameterInfo is accessible from MethodInfo</li>
 * </ul>
 * <li>information about fields ({@link cdi.lite.extension.model.declarations.FieldInfo FieldInfo}):</li>
 * <ul>
 * <li>{@code Collection<FieldInfo<MyService>>}: information about fields declared on one exact class</li>
 * <li>{@code Collection<FieldInfo<? extends MyService>>}: information about fields declared on all subclasses</li>
 * <li>{@code Collection<FieldInfo<? super MyService>>}: information about fields declared on all superclasses</li>
 * <li>{@code Collection<FieldInfo<?>>}: information about fields declared on all present classes</li>
 * <li>TODO {@code Stream} instead of / in addition to {@code Collection}?</li>
 * </ul>
 * <li>configuration of classes ({@link ClassConfig ClassConfig}):</li>
 * <ul>
 * <li>{@code ClassConfig<MyService>}: configurator for one exact class</li>
 * <li>{@code Collection<ClassConfig<MyService>>}: configurator for the one exact class, equivalent to previous line</li>
 * <li>{@code Collection<ClassConfig<? extends MyService>>}: configurators for all subclasses</li>
 * <li>{@code Collection<ClassConfig<? super MyService>>}: configurators for all superclasses</li>
 * <li>{@code Collection<ClassConfig<?>>}: configurators for all present classes</li>
 * <li>TODO {@code Stream} instead of / in addition to {@code Collection}?</li>
 * </ul>
 * <li>configuration of methods ({@link MethodConfig MethodConfig}):</li>
 * <ul>
 * <li>{@code Collection<MethodConfig<MyService>>}: configurator for methods declared on one exact class</li>
 * <li>{@code Collection<MethodConfig<? extends MyService>>}: configurators for methods declared on all subclasses</li>
 * <li>{@code Collection<MethodConfig<? super MyService>>}: configurators for methods declared on all superclasses</li>
 * <li>{@code Collection<MethodConfig<?>>}: configurators for methods declared on all present classes</li>
 * <li>TODO {@code Stream} instead of / in addition to {@code Collection}?</li>
 * </ul>
 * <li>configuration of fields ({@link FieldConfig FieldConfig}):</li>
 * <ul>
 * <li>{@code Collection<FieldConfig<MyService>>}: configurator for fields declared on one exact class</li>
 * <li>{@code Collection<FieldConfig<? extends MyService>>}: configurators for fields declared on all subclasses</li>
 * <li>{@code Collection<FieldConfig<? super MyService>>}: configurators for fields declared on all superclasses</li>
 * <li>{@code Collection<FieldConfig<?>>}: configurators for fields declared on all present classes</li>
 * <li>TODO {@code Stream} instead of / in addition to {@code Collection}?</li>
 * </ul>
 * </ul>
 * <p>
 * It is possible to further narrow down the queries by {@link WithAnnotations}.
 * <p>
 * For advanced use cases, where this kind of queries is not powerful enough, the extension method can also declare
 * a parameter of type {@link World}. If you declare a parameter of shape {@code Collection<SomethingInfo<?>>}
 * or {@code Collection<SomethingConfig<?>>}, that's a good sign you probably want to use {@code World}.
 * <p>
 * If you need to create instances of {@link cdi.lite.extension.model.types.Type Type}, you can also declare
 * a parameter of type {@link Types}. It provides factory methods for the void type, primitive types, class types,
 * array types, parameterized types and wildcard types.
 * <p>
 * If you need to create instances of {@link cdi.lite.extension.model.AnnotationAttribute AnnotationAttribute} or
 * {@link cdi.lite.extension.model.AnnotationAttributeValue AnnotationAttributeValue}, you can also declare
 * a parameter of type {@link Annotations}. It provides factory values for all kinds of annotation attributes.
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
