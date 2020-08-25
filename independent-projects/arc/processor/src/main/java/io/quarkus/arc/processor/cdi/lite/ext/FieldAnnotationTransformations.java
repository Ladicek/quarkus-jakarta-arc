package io.quarkus.arc.processor.cdi.lite.ext;

import java.util.Objects;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;

class FieldAnnotationTransformations
        extends AbstractAnnotationTransformations<FieldAnnotationTransformations.Key, org.jboss.jandex.FieldInfo> {
    FieldAnnotationTransformations() {
        super(org.jboss.jandex.AnnotationTarget.Kind.FIELD);
    }

    @Override
    protected org.jboss.jandex.FieldInfo extractJandexDeclaration(TransformationContext ctx) {
        return ctx.getTarget().asField();
    }

    @Override
    protected Key extractKey(org.jboss.jandex.FieldInfo field) {
        return new Key(field.declaringClass().name(), field.name());
    }

    static final class Key {
        private final DotName className;
        private final String fieldName;

        Key(DotName className, String fieldName) {
            this.className = className;
            this.fieldName = fieldName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof Key))
                return false;
            Key key = (Key) o;
            return Objects.equals(className, key.className)
                    && Objects.equals(fieldName, key.fieldName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(className, fieldName);
        }
    }
}
