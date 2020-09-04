package cdi.lite.extension.phases.discovery;

import javax.enterprise.context.spi.Context;
import java.lang.annotation.Annotation;

public interface ContextBuilder {
    /**
     * If implementation class is defined using {@link #implementation(Class)},
     * this method doesn't have to be called. If it is, it overrides the scope annotation
     * as defined by the implementation class.
     */
    // if called multiple times, last call wins
    ContextBuilder scope(Class<? extends Annotation> scopeAnnotation);

    /**
     * If scope annotation is defined using {@link #scope(Class)},
     * this method doesn't have to be called. If it is, it overrides the normal/pseudo state
     * as defined by the scope annotation.
     */
    // if called multiple times, last call wins
    ContextBuilder normal(boolean isNormal);

    /**
     * Defines the context implementation class.
     * The implementation class typically provides the scope annotations, and therefore
     * also whether the scope is a normal scope or pseudoscope, but this can be overridden
     * using {@link #scope(Class)} and {@link #normal(boolean)}.
     * <p>
     * Implementations can impose additional restrictions on what valid parameters are;
     * they don't have to accept all implementations of {@link Context}.
     */
    // if called multiple times, last call wins
    ContextBuilder implementation(Class<? extends Context> implementationClass);

    // TODO another way to define the context implementation?

    void build();
}
