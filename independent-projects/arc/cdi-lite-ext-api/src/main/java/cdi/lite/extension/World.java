package cdi.lite.extension;

import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.declarations.ClassInfo;
import cdi.lite.extension.model.declarations.FieldInfo;
import cdi.lite.extension.model.declarations.MethodInfo;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.stream.Stream;

// TODO better name, e.g. BeanDeployment?
public interface World {
    ClassQuery classes();

    MethodQuery methods();

    FieldQuery fields();

    AnnotationQuery annotations();

    interface BaseClassQuery<Q> {
        BaseClassQuery<Q> exactType(Class<?> clazz);

        BaseClassQuery<Q> exactType(ClassInfo<?> clazz);

        BaseClassQuery<Q> subtypeOf(Class<?> clazz);

        BaseClassQuery<Q> subtypeOf(ClassInfo<?> clazz);

        BaseClassQuery<Q> supertypeOf(Class<?> clazz);

        BaseClassQuery<Q> supertypeOf(ClassInfo<?> clazz);

        Q andThen();
    }

    interface ClassQuery extends BaseClassQuery<ClassQuery> {
        ClassQuery annotatedWith(Class<? extends Annotation> annotationType);

        ClassQuery annotatedWith(ClassInfo<?> annotationType);

        Collection<ClassInfo<?>> find();

        default Stream<ClassInfo<?>> stream() {
            return find().stream();
        };
    }

    interface MethodQuery {
        MethodQuery annotatedWith(Class<? extends Annotation> annotationType);

        MethodQuery annotatedWith(ClassInfo<?> annotationType);

        BaseClassQuery<MethodQuery> declaredOn();

        BaseClassQuery<MethodQuery> withReturnType();

        // TODO parameters?

        Collection<MethodInfo<?>> find();

        default Stream<MethodInfo<?>> stream() {
            return find().stream();
        };
    }

    interface FieldQuery {
        FieldQuery annotatedWith(Class<? extends Annotation> annotationType);

        FieldQuery annotatedWith(ClassInfo<?> annotationType);

        BaseClassQuery<FieldQuery> declaredOn();

        BaseClassQuery<FieldQuery> ofType();

        Collection<FieldInfo<?>> find();

        default Stream<FieldInfo<?>> stream() {
            return find().stream();
        };
    }

    interface AnnotationQuery {
        AnnotationQuery of(Class<? extends Annotation> annotationType);

        AnnotationQuery of(ClassInfo<?> annotationType);

        Collection<AnnotationInfo> find();

        default Stream<AnnotationInfo> stream() {
            return find().stream();
        };
    }
}
