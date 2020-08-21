package cdi.lite.extension.model.configs;

import cdi.lite.extension.model.declarations.MethodInfo;

/**
 * @param <T> type of whomever declares the configured method or constructor
 */
public interface MethodConfig<T> extends MethodInfo<T>, AnnotationConfig {
}
