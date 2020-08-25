package io.quarkus.arc.processor.cdi.lite.ext;

import java.util.List;
import java.util.Objects;
import org.jboss.jandex.DotName;

class MethodAnnotationTransformations
        extends AbstractAnnotationTransformations<MethodAnnotationTransformations.Key, org.jboss.jandex.MethodInfo> {
    MethodAnnotationTransformations() {
        super(org.jboss.jandex.AnnotationTarget.Kind.METHOD);
    }

    @Override
    protected org.jboss.jandex.MethodInfo extractJandexDeclaration(TransformationContext ctx) {
        return ctx.getTarget().asMethod();
    }

    @Override
    protected Key extractKey(org.jboss.jandex.MethodInfo method) {
        return new Key(method.declaringClass().name(), method.name(), method.parameters());
    }

    static final class Key {
        private final DotName className;
        private final String methodName;
        private final List<org.jboss.jandex.Type> parameterTypes;

        Key(DotName className, String methodName, List<org.jboss.jandex.Type> parameterTypes) {
            this.className = className;
            this.methodName = methodName;
            this.parameterTypes = parameterTypes;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof Key))
                return false;
            Key key = (Key) o;
            return Objects.equals(className, key.className)
                    && Objects.equals(methodName, key.methodName)
                    && Objects.equals(parameterTypes, key.parameterTypes);
        }

        @Override
        public int hashCode() {
            return Objects.hash(className, methodName, parameterTypes);
        }
    }
}
