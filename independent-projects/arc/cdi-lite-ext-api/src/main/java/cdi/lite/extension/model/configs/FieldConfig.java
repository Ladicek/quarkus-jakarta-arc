package cdi.lite.extension.model.configs;

import cdi.lite.extension.model.declarations.FieldInfo;

/**
 * @param <T> type of whomever declares the configured field
 */
public interface FieldConfig<T> extends FieldInfo<T>, AnnotationConfig {
}
