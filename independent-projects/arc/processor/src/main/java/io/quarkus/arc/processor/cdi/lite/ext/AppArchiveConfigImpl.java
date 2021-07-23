package io.quarkus.arc.processor.cdi.lite.ext;

import java.lang.annotation.Annotation;
import java.util.function.Consumer;
import javax.enterprise.inject.build.compatible.spi.AppArchiveConfig;
import javax.enterprise.inject.build.compatible.spi.ClassConfig;
import javax.enterprise.inject.build.compatible.spi.FieldConfig;
import javax.enterprise.inject.build.compatible.spi.MethodConfig;
import javax.enterprise.lang.model.types.Type;

class AppArchiveConfigImpl extends AppArchiveImpl implements AppArchiveConfig {
    final AllAnnotationTransformations annotationTransformations;

    AppArchiveConfigImpl(org.jboss.jandex.IndexView jandexIndex, AllAnnotationTransformations annotationTransformations) {
        super(jandexIndex, annotationTransformations.annotationOverlays);
        this.annotationTransformations = annotationTransformations;
    }

    @Override
    public ClassConfigQuery classes() {
        return new ClassConfigQueryImpl();
    }

    @Override
    public MethodConfigQuery constructors() {
        return new MethodConfigQueryImpl(true);
    }

    @Override
    public MethodConfigQuery methods() {
        return new MethodConfigQueryImpl(false);
    }

    @Override
    public FieldConfigQuery fields() {
        return new FieldConfigQueryImpl();
    }

    private class ClassConfigQueryImpl extends ClassQueryImpl implements ClassConfigQuery {
        @Override
        public ClassConfigQuery exactly(Class<?> clazz) {
            super.exactly(clazz);
            return this;
        }

        @Override
        public ClassConfigQuery subtypeOf(Class<?> clazz) {
            super.subtypeOf(clazz);
            return this;
        }

        @Override
        public ClassConfigQuery annotatedWith(Class<? extends Annotation> annotationType) {
            super.annotatedWith(annotationType);
            return this;
        }

        @Override
        public void configure(Consumer<ClassConfig<?>> consumer) {
            stream()
                    .map(it -> new ClassConfigImpl(jandexIndex, annotationTransformations,
                            ((ClassInfoImpl) it).jandexDeclaration))
                    .forEach(consumer);
        }
    }

    private class MethodConfigQueryImpl extends MethodQueryImpl implements MethodConfigQuery {
        MethodConfigQueryImpl(boolean constructors) {
            super(constructors);
        }

        @Override
        public MethodConfigQuery declaredOn(ClassQuery classes) {
            super.declaredOn(classes);
            return this;
        }

        @Override
        public MethodConfigQuery withReturnType(Class<?> type) {
            super.withReturnType(type);
            return this;
        }

        @Override
        public MethodConfigQuery withReturnType(Type type) {
            super.withReturnType(type);
            return this;
        }

        @Override
        public MethodConfigQuery annotatedWith(Class<? extends Annotation> annotationType) {
            super.annotatedWith(annotationType);
            return this;
        }

        @Override
        public void configure(Consumer<MethodConfig<?>> consumer) {
            stream()
                    .map(it -> new MethodConfigImpl(jandexIndex, annotationTransformations.methods,
                            ((MethodInfoImpl) it).jandexDeclaration))
                    .forEach(consumer);
        }
    }

    private class FieldConfigQueryImpl extends FieldQueryImpl implements FieldConfigQuery {
        @Override
        public FieldConfigQuery declaredOn(ClassQuery classes) {
            super.declaredOn(classes);
            return this;
        }

        @Override
        public FieldConfigQuery ofType(Class<?> type) {
            super.ofType(type);
            return this;
        }

        @Override
        public FieldConfigQuery ofType(Type type) {
            super.ofType(type);
            return this;
        }

        @Override
        public FieldConfigQuery annotatedWith(Class<? extends Annotation> annotationType) {
            super.annotatedWith(annotationType);
            return this;
        }

        @Override
        public void configure(Consumer<FieldConfig<?>> consumer) {
            stream()
                    .map(it -> new FieldConfigImpl(jandexIndex, annotationTransformations.fields,
                            ((FieldInfoImpl) it).jandexDeclaration))
                    .forEach(consumer);
        }
    }
}
