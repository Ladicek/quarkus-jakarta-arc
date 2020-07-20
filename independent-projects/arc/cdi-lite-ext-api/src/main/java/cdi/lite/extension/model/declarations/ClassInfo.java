package cdi.lite.extension.model.declarations;

import cdi.lite.extension.model.types.Type;
import cdi.lite.extension.model.types.TypeVariable;
import java.util.Collection;
import java.util.List;

/**
 * @param <T> the inspected class
 */
public interface ClassInfo<T> extends DeclarationInfo {
    String name();

    String simpleName();

    PackageInfo packageInfo();

    List<TypeVariable> typeParameters();

    Type superClass();

    ClassInfo<?> superClassDeclaration();

    List<Type> superInterfaces();

    List<ClassInfo<?>> superInterfacesDeclarations();

    boolean isPlainClass();

    boolean isInterface();

    boolean isEnum();

    boolean isAnnotation();

    boolean isAbstract();

    boolean isFinal();

    int modifiers();

    Collection<MethodInfo<T>> constructors(); // no static initializers

    Collection<MethodInfo<T>> methods(); // no constructors nor static initializers

    Collection<FieldInfo<T>> fields();

    // ---

    @Override
    default Kind kind() {
        return Kind.CLASS;
    }

    @Override
    default ClassInfo<?> asClass() {
        return this;
    }
}
