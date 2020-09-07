package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.Types;
import cdi.lite.extension.model.declarations.ClassInfo;
import cdi.lite.extension.model.types.PrimitiveType;
import cdi.lite.extension.model.types.Type;
import java.util.Arrays;
import org.jboss.jandex.DotName;

class TypesImpl implements Types {
    private final org.jboss.jandex.IndexView jandexIndex;
    private final AllAnnotationOverlays annotationOverlays;

    TypesImpl(org.jboss.jandex.IndexView jandexIndex, AllAnnotationOverlays annotationOverlays) {
        this.jandexIndex = jandexIndex;
        this.annotationOverlays = annotationOverlays;
    }

    @Override
    public Type of(Class<?> clazz) {
        if (clazz.isArray()) {
            int dimensions = 1;
            Class<?> componentType = clazz.getComponentType();
            while (componentType.isArray()) {
                dimensions++;
                componentType = componentType.getComponentType();
            }
            return ofArray(of(componentType), dimensions);
        }

        if (clazz.isPrimitive()) {
            if (clazz == Void.TYPE) {
                return ofVoid();
            } else if (clazz == Boolean.TYPE) {
                return ofPrimitive(PrimitiveType.PrimitiveKind.BOOLEAN);
            } else if (clazz == Byte.TYPE) {
                return ofPrimitive(PrimitiveType.PrimitiveKind.BYTE);
            } else if (clazz == Short.TYPE) {
                return ofPrimitive(PrimitiveType.PrimitiveKind.SHORT);
            } else if (clazz == Integer.TYPE) {
                return ofPrimitive(PrimitiveType.PrimitiveKind.INT);
            } else if (clazz == Long.TYPE) {
                return ofPrimitive(PrimitiveType.PrimitiveKind.LONG);
            } else if (clazz == Float.TYPE) {
                return ofPrimitive(PrimitiveType.PrimitiveKind.FLOAT);
            } else if (clazz == Double.TYPE) {
                return ofPrimitive(PrimitiveType.PrimitiveKind.DOUBLE);
            } else if (clazz == Character.TYPE) {
                return ofPrimitive(PrimitiveType.PrimitiveKind.CHAR);
            } else {
                throw new IllegalArgumentException("Unknown primitive type " + clazz);
            }
        }

        org.jboss.jandex.Type jandexType = org.jboss.jandex.Type.create(DotName.createSimple(clazz.getName()),
                org.jboss.jandex.Type.Kind.CLASS);
        return new ClassTypeImpl(jandexIndex, annotationOverlays, jandexType.asClassType());

    }

    @Override
    public Type ofVoid() {
        org.jboss.jandex.Type jandexType = org.jboss.jandex.Type.create(DotName.createSimple("void"),
                org.jboss.jandex.Type.Kind.VOID);
        return new VoidTypeImpl(jandexIndex, annotationOverlays, jandexType.asVoidType());
    }

    @Override
    public Type ofPrimitive(PrimitiveType.PrimitiveKind kind) {
        org.jboss.jandex.Type jandexType = org.jboss.jandex.Type.create(DotName.createSimple(kind.name().toLowerCase()),
                org.jboss.jandex.Type.Kind.PRIMITIVE);
        return new PrimitiveTypeImpl(jandexIndex, annotationOverlays, jandexType.asPrimitiveType());
    }

    @Override
    public Type ofClass(ClassInfo<?> clazz) {
        org.jboss.jandex.Type jandexType = org.jboss.jandex.Type.create(((ClassInfoImpl) clazz).jandexDeclaration.name(),
                org.jboss.jandex.Type.Kind.CLASS);
        return new ClassTypeImpl(jandexIndex, annotationOverlays, jandexType.asClassType());
    }

    @Override
    public Type ofArray(Type componentType, int dimensions) {
        org.jboss.jandex.ArrayType jandexType = org.jboss.jandex.ArrayType.create(((TypeImpl<?>) componentType).jandexType,
                dimensions);
        return new ArrayTypeImpl(jandexIndex, annotationOverlays, jandexType);
    }

    @Override
    public Type parameterized(Class<?> parameterizedType, Class<?>... typeArguments) {
        DotName parameterizedTypeName = DotName.createSimple(parameterizedType.getName());
        Type[] transformedTypeArguments = Arrays.stream(typeArguments).map(this::of).toArray(Type[]::new);
        return parameterizedType(parameterizedTypeName, transformedTypeArguments);
    }

    @Override
    public Type parameterized(Class<?> parameterizedType, Type... typeArguments) {
        DotName parameterizedTypeName = DotName.createSimple(parameterizedType.getName());
        return parameterizedType(parameterizedTypeName, typeArguments);
    }

    @Override
    public Type parameterized(Type parameterizedType, Type... typeArguments) {
        DotName parameterizedTypeName = ((TypeImpl<?>) parameterizedType).jandexType.name();
        return parameterizedType(parameterizedTypeName, typeArguments);
    }

    private Type parameterizedType(DotName parameterizedTypeName, Type... typeArguments) {
        org.jboss.jandex.Type[] jandexTypeArguments = Arrays.stream(typeArguments)
                .map(it -> ((TypeImpl<?>) it).jandexType)
                .toArray(org.jboss.jandex.Type[]::new);

        org.jboss.jandex.ParameterizedType jandexType = org.jboss.jandex.ParameterizedType.create(parameterizedTypeName,
                jandexTypeArguments, null);
        return new ParameterizedTypeImpl(jandexIndex, annotationOverlays, jandexType);
    }

    @Override
    public Type wildcardWithUpperBound(Type upperBound) {
        org.jboss.jandex.WildcardType jandexType = org.jboss.jandex.WildcardType.create(((TypeImpl<?>) upperBound).jandexType,
                true);
        return new WildcardTypeImpl(jandexIndex, annotationOverlays, jandexType);
    }

    @Override
    public Type wildcardWithLowerBound(Type lowerBound) {
        org.jboss.jandex.WildcardType jandexType = org.jboss.jandex.WildcardType.create(((TypeImpl<?>) lowerBound).jandexType,
                false);
        return new WildcardTypeImpl(jandexIndex, annotationOverlays, jandexType);
    }

    @Override
    public Type wildcardUnbounded() {
        org.jboss.jandex.WildcardType jandexType = org.jboss.jandex.WildcardType.create(null, true);
        return new WildcardTypeImpl(jandexIndex, annotationOverlays, jandexType);
    }
}
