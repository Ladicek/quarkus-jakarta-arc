package cdi.lite.extension.phases.enhancement;

import cdi.lite.extension.model.declarations.MethodInfo;

/**
 * @param <T> type of whomever declares the configured method or constructor
 */
public interface MethodConfig<T> extends MethodInfo<T>, AnnotationConfig {
}
