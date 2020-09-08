package cdi.lite.extension.phases;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 3rd phase of CDI Lite extension processing.
 * Allows registering synthetic beans and observers.
 * <p>
 * Extensions annotated {@code @Synthesis} can define parameters of these types:
 * <ul>
 * <li>{@link cdi.lite.extension.phases.synthesis.SyntheticComponents SyntheticComponents}: to register synthetic
 * beans or observers</li>
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
 * <li>information about beans ({@link cdi.lite.extension.beans.BeanInfo BeanInfo}):</li>
 * <ul>
 * <li>{@code Collection<BeanInfo<MyService>>}: information about beans with one exact type</li>
 * <li>{@code Collection<BeanInfo<? extends MyService>>}: information about beans with at least one of the subtypes</li>
 * <li>{@code Collection<BeanInfo<? super MyService>>}: information about beans with at least one of the supertypes</li>
 * <li>{@code Collection<BeanInfo<?>>}: information about all present beans</li>
 * <li>TODO {@code Stream} instead of / in addition to {@code Collection}?</li>
 * </ul>
 * <li>information about observers ({@link cdi.lite.extension.beans.ObserverInfo ObserverInfo}):</li>
 * <ul>
 * <li>{@code Collection<ObserverInfo<MyService>>}: information about observers observing one exact type</li>
 * <li>{@code Collection<ObserverInfo<? extends MyService>>}: information about observers observing at least one of the
 * subtypes</li>
 * <li>{@code Collection<ObserverInfo<? super MyService>>}: information about observers observing at least one of the
 * supertypes</li>
 * <li>{@code Collection<ObserverInfo<?>>}: information about all present observers</li>
 * <li>TODO {@code Stream} instead of / in addition to {@code Collection}?</li>
 * </ul>
 * </ul>
 * It is possible to further narrow down the {@code ClassInfo}, {@code MethodInfo} and {@code FieldInfo} queries
 * by {@link cdi.lite.extension.WithAnnotations @WithAnnotations}.
 * <p>
 * For advanced use cases, where this kind of queries is not powerful enough, the extension can also declare
 * a parameter of type {@link cdi.lite.extension.AppArchive AppArchive} or
 * {@link cdi.lite.extension.AppDeployment AppDeployment}.
 * If you declare a parameter of shape {@code Collection<SomethingInfo<?>>}, that's a good sign you probably want
 * to use {@code AppArchive} or {@code AppDeployment}.
 * <p>
 * If you need to create instances of {@link cdi.lite.extension.model.types.Type Type}, you can also declare
 * a parameter of type {@link cdi.lite.extension.Types Types}. It provides factory methods for the void type,
 * primitive types, class types, array types, parameterized types and wildcard types.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Synthesis {
}
