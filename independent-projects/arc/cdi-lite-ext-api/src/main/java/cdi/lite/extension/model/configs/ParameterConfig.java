package cdi.lite.extension.model.configs;

import cdi.lite.extension.model.declarations.ParameterInfo;

/**
 * @param <T> type of whomever declares the method or constructor that has the configured parameter
 */
public interface ParameterConfig<T> extends ParameterInfo<T>, AnnotationConfig {
}
