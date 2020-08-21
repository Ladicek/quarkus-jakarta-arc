package io.quarkus.arc.processor.cdi.lite.ext;

import cdi.lite.extension.Types;
import cdi.lite.extension.model.declarations.ClassInfo;
import cdi.lite.extension.model.types.PrimitiveType;
import cdi.lite.extension.model.types.Type;
import org.jboss.jandex.DotName;

class TypesImpl implements Types {
    private final org.jboss.jandex.IndexView jandexIndex;

    TypesImpl(org.jboss.jandex.IndexView jandexIndex) {
        this.jandexIndex = jandexIndex;
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
                throw new IllegalStateException("Unknown primitive type " + clazz);
            }
        }

        org.jboss.jandex.Type jandexType = org.jboss.jandex.Type.create(DotName.createSimple(clazz.getName()),
                org.jboss.jandex.Type.Kind.CLASS);
        return new ClassTypeImpl(jandexIndex, jandexType.asClassType());

    }

    @Override
    public Type ofVoid() {
        org.jboss.jandex.Type jandexType = org.jboss.jandex.Type.create(DotName.createSimple("void"),
                org.jboss.jandex.Type.Kind.VOID);
        return new VoidTypeImpl(jandexIndex, jandexType.asVoidType());
    }

    @Override
    public Type ofPrimitive(PrimitiveType.PrimitiveKind kind) {
        org.jboss.jandex.Type jandexType = org.jboss.jandex.Type.create(DotName.createSimple(kind.name().toLowerCase()),
                org.jboss.jandex.Type.Kind.PRIMITIVE);
        return new PrimitiveTypeImpl(jandexIndex, jandexType.asPrimitiveType());
    }

    @Override
    public Type ofClass(ClassInfo<?> clazz) {
        org.jboss.jandex.Type jandexType = org.jboss.jandex.Type.create(((ClassInfoImpl) clazz).jandexDeclaration.name(),
                org.jboss.jandex.Type.Kind.CLASS);
        return new ClassTypeImpl(jandexIndex, jandexType.asClassType());
    }

    @Override
    public Type ofArray(Type componentType, int dimensions) {
        org.jboss.jandex.ArrayType jandexType = org.jboss.jandex.ArrayType.create(((TypeImpl<?>) componentType).jandexType,
                dimensions);
        return new ArrayTypeImpl(jandexIndex, jandexType);
    }
}
