package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.types.Type;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import org.jboss.jandex.DotName;

// TODO all subclasses must have equals, hashCode and perhaps also their own toString (though the current one is fine)
abstract class TypeImpl<JandexType extends org.jboss.jandex.Type> implements Type {
    final org.jboss.jandex.IndexView jandexIndex;
    final JandexType jandexType;

    TypeImpl(org.jboss.jandex.IndexView jandexIndex, JandexType jandexType) {
        this.jandexIndex = jandexIndex;
        this.jandexType = jandexType;
    }

    static Type fromJandexType(org.jboss.jandex.IndexView jandexIndex, org.jboss.jandex.Type jandexType) {
        switch (jandexType.kind()) {
            case VOID:
                return new VoidTypeImpl(jandexIndex, jandexType.asVoidType());
            case PRIMITIVE:
                return new PrimitiveTypeImpl(jandexIndex, jandexType.asPrimitiveType());
            case CLASS:
                return new ClassTypeImpl(jandexIndex, jandexType.asClassType());
            case ARRAY:
                return new ArrayTypeImpl(jandexIndex, jandexType.asArrayType());
            case PARAMETERIZED_TYPE:
                return new ParameterizedTypeImpl(jandexIndex, jandexType.asParameterizedType());
            case TYPE_VARIABLE:
                return new TypeVariableImpl(jandexIndex, jandexType.asTypeVariable());
            case UNRESOLVED_TYPE_VARIABLE:
                return new UnresolvedTypeVariableImpl(jandexIndex, jandexType.asUnresolvedTypeVariable());
            case WILDCARD_TYPE:
                return new WildcardTypeImpl(jandexIndex, jandexType.asWildcardType());
            default:
                throw new IllegalStateException("Unknown type " + jandexType);
        }
    }

    @Override
    public boolean hasAnnotation(Class<? extends Annotation> annotationType) {
        return jandexType.hasAnnotation(DotName.createSimple(annotationType.getName()));
    }

    @Override
    public AnnotationInfo annotation(Class<? extends Annotation> annotationType) {
        return new AnnotationInfoImpl(jandexIndex, jandexType.annotation(DotName.createSimple(annotationType.getName())));
    }

    @Override
    public Collection<AnnotationInfo> repeatableAnnotation(Class<? extends Annotation> annotationType) {
        return annotationsWithRepeatable(jandexType, DotName.createSimple(annotationType.getName()), jandexIndex)
                .stream()
                .map(it -> new AnnotationInfoImpl(jandexIndex, it))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<AnnotationInfo> annotations() {
        return jandexType.annotations()
                .stream()
                .map(it -> new AnnotationInfoImpl(jandexIndex, it))
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return jandexType.toString();
    }

    // ---
    // Jandex doesn't have `annotationsWithRepeatable` method for types

    private static final DotName REPEATABLE = DotName.createSimple("java.lang.annotation.Repeatable");

    private static Collection<org.jboss.jandex.AnnotationInstance> annotationsWithRepeatable(org.jboss.jandex.Type type,
            DotName name, org.jboss.jandex.IndexView index) {

        org.jboss.jandex.AnnotationInstance ret = type.annotation(name);
        if (ret != null) {
            // Annotation present - no need to try to find repeatable annotations
            return Collections.singletonList(ret);
        }
        org.jboss.jandex.ClassInfo annotationClass = index.getClassByName(name);
        if (annotationClass == null) {
            throw new IllegalArgumentException("Index does not contain the annotation definition: " + name);
        }
        if (!annotationClass.isAnnotation()) {
            throw new IllegalArgumentException("Not an annotation type: " + annotationClass);
        }
        org.jboss.jandex.AnnotationInstance repeatable = annotationClass.classAnnotation(REPEATABLE);
        if (repeatable == null) {
            return Collections.emptyList();
        }
        org.jboss.jandex.Type containingType = repeatable.value().asClass();
        org.jboss.jandex.AnnotationInstance containing = type.annotation(containingType.name());
        if (containing == null) {
            return Collections.emptyList();
        }
        org.jboss.jandex.AnnotationInstance[] values = containing.value().asNestedArray();
        return Arrays.asList(values);
    }
}
