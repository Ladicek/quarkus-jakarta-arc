package cdi.lite.extension.model.configs;

import cdi.lite.extension.model.declarations.ClassInfo;

/**
 * @param <T> the configured class
 */
public interface ClassConfig<T> extends ClassInfo<T>, AnnotationConfig {
}
