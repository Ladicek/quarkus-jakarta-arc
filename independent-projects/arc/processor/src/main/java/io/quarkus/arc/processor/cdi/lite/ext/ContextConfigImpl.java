package io.quarkus.arc.processor.cdi.lite.ext;

import java.lang.annotation.Annotation;
import javax.enterprise.context.spi.AlterableContext;
import javax.enterprise.inject.build.compatible.spi.ContextConfig;

class ContextConfigImpl implements ContextConfig {
    Class<? extends AlterableContext> implementationClass;
    Class<? extends Annotation> scopeAnnotation;
    Boolean isNormal; // null if not set, in which case it's derived from the scope annotation

    @Override
    public ContextConfig scope(Class<? extends Annotation> scopeAnnotation) {
        this.scopeAnnotation = scopeAnnotation;
        return this;
    }

    @Override
    public ContextConfig normal(boolean isNormal) {
        this.isNormal = isNormal;
        return this;
    }

    @Override
    public ContextConfig implementation(Class<? extends AlterableContext> implementationClass) {
        this.implementationClass = implementationClass;
        return this;
    }
}
