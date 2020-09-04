package cdi.lite.extension.phases.enhancement;

import cdi.lite.extension.model.declarations.ClassInfo;

/**
 * @param <T> the configured class
 */
public interface ClassConfig<T> extends ClassInfo<T>, AnnotationConfig {
}
