package cdi.lite.extension.phases;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 2nd phase of CDI Lite extension processing.
 * Allows transforming annotations.
 * <p>
 * Extensions annotated {@code @Enhancement} can define parameters of these types:
 * <ul>
 * <li>information about classes ({@link cdi.lite.extension.model.declarations.ClassInfo ClassInfo}):</li>
 * <ul>
 * <li>{@code ClassInfo<MyService>}: information about one exact class</li>
 * <li>{@code Collection<ClassInfo<MyService>>}: information about one exact class, equivalent to previous line</li>
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
 * <li>information about fields ({@link cdi.lite.extension.model.declarations.FieldInfo FieldInfo}):</li>
 * <ul>
 * <li>{@code Collection<FieldInfo<MyService>>}: information about fields declared on one exact class</li>
 * <li>{@code Collection<FieldInfo<? extends MyService>>}: information about fields declared on all subclasses</li>
 * <li>{@code Collection<FieldInfo<? super MyService>>}: information about fields declared on all superclasses</li>
 * <li>{@code Collection<FieldInfo<?>>}: information about fields declared on all present classes</li>
 * <li>TODO {@code Stream} instead of / in addition to {@code Collection}?</li>
 * </ul>
 * <li>configuration of classes ({@link cdi.lite.extension.phases.enhancement.ClassConfig ClassConfig}):</li>
 * <ul>
 * <li>{@code ClassConfig<MyService>}: configurator for one exact class</li>
 * <li>{@code Collection<ClassConfig<MyService>>}: configurator for the one exact class, equivalent to previous line</li>
 * <li>{@code Collection<ClassConfig<? extends MyService>>}: configurators for all subclasses</li>
 * <li>{@code Collection<ClassConfig<? super MyService>>}: configurators for all superclasses</li>
 * <li>{@code Collection<ClassConfig<?>>}: configurators for all present classes</li>
 * <li>TODO {@code Stream} instead of / in addition to {@code Collection}?</li>
 * </ul>
 * <li>configuration of methods ({@link cdi.lite.extension.phases.enhancement.MethodConfig MethodConfig}):</li>
 * <ul>
 * <li>{@code Collection<MethodConfig<MyService>>}: configurator for methods declared on one exact class</li>
 * <li>{@code Collection<MethodConfig<? extends MyService>>}: configurators for methods declared on all subclasses</li>
 * <li>{@code Collection<MethodConfig<? super MyService>>}: configurators for methods declared on all superclasses</li>
 * <li>{@code Collection<MethodConfig<?>>}: configurators for methods declared on all present classes</li>
 * <li>TODO {@code Stream} instead of / in addition to {@code Collection}?</li>
 * </ul>
 * <li>configuration of fields ({@link cdi.lite.extension.phases.enhancement.FieldConfig FieldConfig}):</li>
 * <ul>
 * <li>{@code Collection<FieldConfig<MyService>>}: configurator for fields declared on one exact class</li>
 * <li>{@code Collection<FieldConfig<? extends MyService>>}: configurators for fields declared on all subclasses</li>
 * <li>{@code Collection<FieldConfig<? super MyService>>}: configurators for fields declared on all superclasses</li>
 * <li>{@code Collection<FieldConfig<?>>}: configurators for fields declared on all present classes</li>
 * <li>TODO {@code Stream} instead of / in addition to {@code Collection}?</li>
 * </ul>
 * </ul>
 * <p>
 * It is possible to further narrow down the queries by {@link cdi.lite.extension.WithAnnotations @WithAnnotations}.
 * <p>
 * For advanced use cases, where this kind of queries is not powerful enough, the extension can also declare
 * a parameter of type {@link cdi.lite.extension.AppArchive AppArchive} or
 * {@link cdi.lite.extension.phases.enhancement.AppArchiveConfig AppArchiveConfig}.
 * If you declare a parameter of shape {@code Collection<SomethingInfo<?>>} or {@code Collection<SomethingConfig<?>>},
 * that's a good sign you probably want to use {@code AppArchive} or {@code AppArchiveConfig}.
 * <p>
 * If you need to create instances of {@link cdi.lite.extension.model.types.Type Type}, you can also declare
 * a parameter of type {@link cdi.lite.extension.Types Types}. It provides factory methods for the void type,
 * primitive types, class types, array types, parameterized types and wildcard types.
 * <p>
 * If you need to create instances of {@link cdi.lite.extension.model.AnnotationAttribute AnnotationAttribute} or
 * {@link cdi.lite.extension.model.AnnotationAttributeValue AnnotationAttributeValue}, you can also declare
 * a parameter of type {@link cdi.lite.extension.phases.enhancement.Annotations Annotations}.
 * It provides factory values for all kinds of annotation attributes.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Enhancement {
}
