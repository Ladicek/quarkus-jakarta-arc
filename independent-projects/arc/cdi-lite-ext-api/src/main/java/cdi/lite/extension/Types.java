package cdi.lite.extension;

import cdi.lite.extension.model.declarations.ClassInfo;
import cdi.lite.extension.model.types.PrimitiveType;
import cdi.lite.extension.model.types.Type;

public interface Types {
    /**
     * Returns a type from given class literal.
     * For example:
     * <ul>
     * <li>{@code of(void.class)}: same as {@code ofVoid()}</li>
     * <li>{@code of(int.class)}: same as {@code ofPrimitive(PrimitiveKind.INT)}</li>
     * <li>{@code of(String.class)}: same as {@code ofClass(... ClassInfo for String ...)}</li>
     * <li>{@code of(int[].class)}: same as {@code ofArray(ofPrimitive(PrimitiveKind.INT), 1)}</li>
     * <li>{@code of(String[][].class)}: same as {@code ofArray(ofClass(... ClassInfo for String ...), 2)}</li>
     * </ul>
     */
    Type of(Class<?> clazz);

    Type ofVoid();

    Type ofPrimitive(PrimitiveType.PrimitiveKind kind);

    Type ofClass(ClassInfo<?> clazz);

    Type ofArray(Type componentType, int dimensions);
}
