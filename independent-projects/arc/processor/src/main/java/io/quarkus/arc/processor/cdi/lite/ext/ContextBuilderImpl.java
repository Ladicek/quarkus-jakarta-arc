package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.phases.discovery.ContextBuilder;
import java.lang.annotation.Annotation;
import javax.enterprise.context.spi.AlterableContext;

class ContextBuilderImpl implements ContextBuilder {
    Class<? extends AlterableContext> implementationClass;
    Class<? extends Annotation> scopeAnnotation;
    Boolean isNormal; // null if not set, in which case it's derived from the scope annotation

    @Override
    public ContextBuilder scope(Class<? extends Annotation> scopeAnnotation) {
        this.scopeAnnotation = scopeAnnotation;
        return this;
    }

    @Override
    public ContextBuilder normal(boolean isNormal) {
        this.isNormal = isNormal;
        return this;
    }

    @Override
    public ContextBuilder implementation(Class<? extends AlterableContext> implementationClass) {
        this.implementationClass = implementationClass;
        return this;
    }
}
