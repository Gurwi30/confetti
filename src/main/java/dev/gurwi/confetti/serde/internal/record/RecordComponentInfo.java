package dev.gurwi.confetti.serde.internal.record;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

public final class RecordComponentInfo {

    private final String name;
    private final Class<?> type;
    private final Type genericType;
    private final Method accessor;

    RecordComponentInfo(String name, Class<?> type, Type genericType, Method accessor) {
        this.name = name;
        this.type = type;
        this.genericType = genericType;
        this.accessor = accessor;
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }

    public Type getGenericType() {
        return genericType;
    }

    public Method getAccessor() {
        return accessor;
    }

}