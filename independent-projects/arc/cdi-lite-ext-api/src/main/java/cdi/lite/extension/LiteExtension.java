package cdi.lite.extension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotated methods must be {@code public}, must not be {@code static} and must have return type of {@code void}.
 * Classes with annotated methods must be {@code public} and have a zero-parameter constructor.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LiteExtension {
}
