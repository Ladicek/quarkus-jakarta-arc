package cdi.lite.extension;

import cdi.lite.extension.model.declarations.ClassInfo;
import cdi.lite.extension.model.declarations.FieldInfo;
import cdi.lite.extension.model.declarations.MethodInfo;
import cdi.lite.extension.model.types.Type;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.stream.Stream;

// TODO better name, e.g. BeanArchive or BeanDeployment?
public interface World {
    ClassQuery classes();

    MethodQuery constructors(); // no static initializers

    MethodQuery methods(); // no constructors nor static initializers

    FieldQuery fields();

    interface ClassQuery {
        ClassQuery exactly(Class<?> clazz);

        ClassQuery exactly(ClassInfo<?> clazz);

        ClassQuery subtypeOf(Class<?> clazz);

        ClassQuery subtypeOf(ClassInfo<?> clazz);

        ClassQuery supertypeOf(Class<?> clazz);

        ClassQuery supertypeOf(ClassInfo<?> clazz);

        ClassQuery annotatedWith(Class<? extends Annotation> annotationType);

        ClassQuery annotatedWith(ClassInfo<?> annotationType);

        Collection<ClassInfo<?>> find();

        Stream<ClassInfo<?>> stream();

        // TODO also return ClassConfig<?> somehow
    }

    interface MethodQuery {
        MethodQuery declaredOn(ClassQuery classes);

        MethodQuery withReturnType(Type type);

        // TODO parameters?

        MethodQuery annotatedWith(Class<? extends Annotation> annotationType);

        MethodQuery annotatedWith(ClassInfo<?> annotationType);

        Collection<? extends MethodInfo<?>> find();

        Stream<? extends MethodInfo<?>> stream();

        // TODO also return MethodConfig<?> somehow
    }

    interface FieldQuery {
        FieldQuery declaredOn(ClassQuery classes);

        FieldQuery ofType(Type type);

        FieldQuery annotatedWith(Class<? extends Annotation> annotationType);

        FieldQuery annotatedWith(ClassInfo<?> annotationType);

        Collection<FieldInfo<?>> find();

        Stream<FieldInfo<?>> stream();

        // TODO also return FieldConfig<?> somehow
    }
}
