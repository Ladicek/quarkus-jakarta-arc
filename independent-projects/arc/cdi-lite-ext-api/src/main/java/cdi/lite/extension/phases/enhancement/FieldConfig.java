package cdi.lite.extension.phases.enhancement;

import cdi.lite.extension.model.declarations.FieldInfo;

/**
 * @param <T> type of whomever declares the configured field
 */
public interface FieldConfig<T> extends FieldInfo<T>, AnnotationConfig {
}
