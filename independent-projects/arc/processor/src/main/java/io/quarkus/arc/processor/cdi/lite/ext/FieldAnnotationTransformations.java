package io.quarkus.arc.processor.cdi.lite.ext;

import java.util.Objects;
import org.jboss.jandex.DotName;

class FieldAnnotationTransformations extends AbstractAnnotationTransformations<FieldAnnotationTransformations.Key> {
    FieldAnnotationTransformations() {
        super(org.jboss.jandex.AnnotationTarget.Kind.FIELD);
    }

    @Override
    protected Key extractKey(TransformationContext ctx) {
        org.jboss.jandex.FieldInfo field = ctx.getTarget().asField();
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
