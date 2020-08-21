package io.quarkus.arc.processor.cdi.lite.ext;

import org.jboss.jandex.DotName;

class ClassAnnotationTransformations extends AbstractAnnotationTransformations<DotName> {
    ClassAnnotationTransformations() {
        super(org.jboss.jandex.AnnotationTarget.Kind.CLASS);
    }

    @Override
    protected DotName extractKey(TransformationContext ctx) {
        return ctx.getTarget().asClass().name();
    }
}
