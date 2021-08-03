package io.quarkus.arc.processor.cdi.lite.ext;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.enterprise.lang.model.AnnotationInfo;
import javax.enterprise.lang.model.AnnotationMember;
import javax.enterprise.lang.model.declarations.ClassInfo;
import javax.enterprise.lang.model.types.Type;

class AnnotationMemberImpl implements AnnotationMember {
    final org.jboss.jandex.IndexView jandexIndex;
    final AllAnnotationOverlays annotationOverlays;
    final org.jboss.jandex.AnnotationValue jandexAnnotationMember;

    AnnotationMemberImpl(org.jboss.jandex.IndexView jandexIndex, AllAnnotationOverlays annotationOverlays,
            org.jboss.jandex.AnnotationValue jandexAnnotationMember) {
        this.jandexIndex = jandexIndex;
        this.annotationOverlays = annotationOverlays;
        this.jandexAnnotationMember = jandexAnnotationMember;
    }

    @Override
    public Kind kind() {
        switch (jandexAnnotationMember.kind()) {
            case BOOLEAN:
                return Kind.BOOLEAN;
            case BYTE:
                return Kind.BYTE;
            case SHORT:
                return Kind.SHORT;
            case INTEGER:
                return Kind.INT;
            case LONG:
                return Kind.LONG;
            case FLOAT:
                return Kind.FLOAT;
            case DOUBLE:
                return Kind.DOUBLE;
            case CHARACTER:
                return Kind.CHAR;
            case STRING:
                return Kind.STRING;
            case ENUM:
                return Kind.ENUM;
            case CLASS:
                return Kind.CLASS;
            case NESTED:
                return Kind.NESTED_ANNOTATION;
            case ARRAY:
                return Kind.ARRAY;
            default:
                throw new IllegalStateException("Unknown annotation member " + jandexAnnotationMember);
        }
    }

    @Override
    public boolean asBoolean() {
        return jandexAnnotationMember.asBoolean();
    }

    @Override
    public byte asByte() {
        return jandexAnnotationMember.asByte();
    }

    @Override
    public short asShort() {
        return jandexAnnotationMember.asShort();
    }

    @Override
    public int asInt() {
        return jandexAnnotationMember.asInt();
    }

    @Override
    public long asLong() {
        return jandexAnnotationMember.asLong();
    }

    @Override
    public float asFloat() {
        return jandexAnnotationMember.asFloat();
    }

    @Override
    public double asDouble() {
        return jandexAnnotationMember.asDouble();
    }

    @Override
    public char asChar() {
        return jandexAnnotationMember.asChar();
    }

    @Override
    public String asString() {
        return jandexAnnotationMember.asString();
    }

    @Override
    public <E extends Enum<E>> E asEnum(Class<E> enumType) {
        return Enum.valueOf(enumType, jandexAnnotationMember.asEnum());
    }

    @Override
    public String asEnumConstant() {
        return jandexAnnotationMember.asEnum();
    }

    @Override
    public ClassInfo<?> asEnumClass() {
        return new ClassInfoImpl(jandexIndex, annotationOverlays,
                jandexIndex.getClassByName(jandexAnnotationMember.asEnumType()));
    }

    @Override
    public Type asType() {
        return TypeImpl.fromJandexType(jandexIndex, annotationOverlays, jandexAnnotationMember.asClass());
    }

    @Override
    public AnnotationInfo<?> asNestedAnnotation() {
        return new AnnotationInfoImpl(jandexIndex, annotationOverlays, jandexAnnotationMember.asNested());
    }

    @Override
    public List<AnnotationMember> asArray() {
        org.jboss.jandex.AnnotationValue[] array = new org.jboss.jandex.HackAnnotationValue(jandexAnnotationMember)
                .asArray();
        return Arrays.stream(array)
                .map(it -> new AnnotationMemberImpl(jandexIndex, annotationOverlays, it))
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AnnotationMemberImpl that = (AnnotationMemberImpl) o;
        return Objects.equals(jandexAnnotationMember.value(), that.jandexAnnotationMember.value());
    }

    @Override
    public int hashCode() {
        return Objects.hash(jandexAnnotationMember.value());
    }

    @Override
    public String toString() {
        return "" + jandexAnnotationMember.value();
    }
}
