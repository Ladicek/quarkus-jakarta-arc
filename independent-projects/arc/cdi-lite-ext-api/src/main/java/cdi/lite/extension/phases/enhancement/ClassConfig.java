package cdi.lite.extension.phases.enhancement;

import cdi.lite.extension.model.declarations.ClassInfo;

import java.util.Collection;

/**
 * @param <T> the configured class
 */
public interface ClassConfig<T> extends ClassInfo<T>, AnnotationConfig {
    // only constructors declared by this class, not inherited ones
    // no static initializers
    @Override
    Collection<? extends MethodConfig<T>> constructors();

    // only methods declared by this class, not inherited ones
    // no constructors nor static initializers
    @Override
    Collection<? extends MethodConfig<T>> methods();

    // only fields declared by this class, not inherited ones
    @Override
    Collection<? extends FieldConfig<T>> fields();
}
