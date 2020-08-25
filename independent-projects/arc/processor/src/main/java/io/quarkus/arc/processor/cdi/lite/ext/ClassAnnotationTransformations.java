package io.quarkus.arc.processor.cdi.lite.ext;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;

class ClassAnnotationTransformations extends AbstractAnnotationTransformations<DotName, org.jboss.jandex.ClassInfo> {
    ClassAnnotationTransformations() {
        super(org.jboss.jandex.AnnotationTarget.Kind.CLASS);
    }

    @Override
    protected ClassInfo extractJandexDeclaration(TransformationContext ctx) {
        return ctx.getTarget().asClass();
    }

    @Override
    protected DotName extractKey(org.jboss.jandex.ClassInfo classInfo) {
        return classInfo.name();
    }
}
