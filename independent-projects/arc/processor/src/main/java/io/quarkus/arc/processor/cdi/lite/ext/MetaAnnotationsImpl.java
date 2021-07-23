package io.quarkus.arc.processor.cdi.lite.ext;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.enterprise.inject.build.compatible.spi.ClassConfig;
import javax.enterprise.inject.build.compatible.spi.ContextBuilder;
import javax.enterprise.inject.build.compatible.spi.MetaAnnotations;

class MetaAnnotationsImpl implements MetaAnnotations {
    final Map<Class<? extends Annotation>, Consumer<ClassConfig<?>>> qualifiers;
    final Map<Class<? extends Annotation>, Consumer<ClassConfig<?>>> interceptorBindings;
    final Map<Class<? extends Annotation>, Consumer<ClassConfig<?>>> stereotypes;
    final List<ContextBuilderImpl> contexts;

    MetaAnnotationsImpl(Map<Class<? extends Annotation>, Consumer<ClassConfig<?>>> qualifiers,
            Map<Class<? extends Annotation>, Consumer<ClassConfig<?>>> interceptorBindings,
            Map<Class<? extends Annotation>, Consumer<ClassConfig<?>>> stereotypes,
            List<ContextBuilderImpl> contexts) {
        this.qualifiers = qualifiers;
        this.interceptorBindings = interceptorBindings;
        this.stereotypes = stereotypes;
        this.contexts = contexts;
    }

    @Override
    public void addQualifier(Class<? extends Annotation> annotation, Consumer<ClassConfig<?>> config) {
        qualifiers.put(annotation, config);
    }

    @Override
    public void addInterceptorBinding(Class<? extends Annotation> annotation, Consumer<ClassConfig<?>> config) {
        interceptorBindings.put(annotation, config);
    }

    @Override
    public void addStereotype(Class<? extends Annotation> annotation, Consumer<ClassConfig<?>> config) {
        stereotypes.put(annotation, config);
    }

    @Override
    public ContextBuilder addContext() {
        ContextBuilderImpl contextBuilder = new ContextBuilderImpl();
        contexts.add(contextBuilder);
        return contextBuilder;
    }
}
