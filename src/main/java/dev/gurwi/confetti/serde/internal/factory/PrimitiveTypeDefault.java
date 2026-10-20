package dev.gurwi.confetti.serde.internal.factory;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface PrimitiveTypeDefault {

    @Contract(pure = true)
    static @NotNull Object of(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == char.class) return '\0';
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0.0f;
        if (type == double.class) return 0.0d;

        throw new IllegalArgumentException("Unsupported primitive type: " + type);
    }

}
