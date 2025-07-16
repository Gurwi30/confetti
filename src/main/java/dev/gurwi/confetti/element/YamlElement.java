package dev.gurwi.confetti.element;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public interface YamlElement {

    static @NotNull Map<String, YamlElement> adapt(@NotNull Map<String, Object> map) {
        Map<String, YamlElement> elements = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            elements.put(entry.getKey(), adapt(entry.getValue()));
        }

        return elements;
    }

    static @NotNull YamlElement adapt(@Nullable Object obj) {
        if (obj == null) return YamlPrimitive.NULL;

        return switch (obj) {
            case Collection<?> collection -> new YamlArray(
                    collection.stream()
                            .map(YamlElement::adapt)
                            .collect(Collectors.toList())

            );

            case Map<?, ?> map -> {
                Map<String, YamlElement> elements = new LinkedHashMap<>();

                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    String path = (String) entry.getKey();
                    elements.put(path, adapt(entry.getValue()));
                }

                yield new YamlObject(elements);
            }

            default -> {
                if (obj.getClass().isArray()) {
                    YamlArray array = new YamlArray();
                    int length = Array.getLength(obj);

                    for (int i = 0; i < length; i++) {
                        Object element = Array.get(obj, i);
                        array.add(adapt(element));
                    }

                    yield array;
                }

                if (isPrimitiveOrWrapper(obj.getClass()) || obj instanceof String) {
                    yield new YamlPrimitive(obj);
                }

                throw new IllegalArgumentException("Cannot adapt " + obj.getClass().getName() + " to " + YamlElement.class);
            }
        };
    }

    private Object unwrap(@NotNull YamlElement element) {
        return switch (element) {
            case YamlPrimitive primitive -> primitive.getValue();

            case YamlArray array -> array.getElements().stream()
                    .map(this::unwrap)
                    .collect(Collectors.toList());

            case YamlObject object -> {
                Map<String, Object> map = new LinkedHashMap<>();
                for (Map.Entry<String, YamlElement> entry : object.getData().entrySet()) {
                    map.put(entry.getKey(), unwrap(entry.getValue()));
                }

                yield map;
            }

            default -> throw new IllegalStateException("Unexpected value: " + element);
        };
    }

    boolean isEmpty();

    default boolean isObject() {
        return this instanceof YamlObject;
    }

    default boolean isArray() {
        return this instanceof YamlArray;
    }

    default boolean isPrimitive() {
        return this instanceof YamlPrimitive;
    }

    default YamlObject asObject() {
        if (isObject()) return (YamlObject) this;
        return null;
    }

    default YamlArray asArray() {
        if (isArray()) return (YamlArray) this;
        return null;
    }

    default YamlPrimitive asPrimitive() {
        if (isPrimitive()) return (YamlPrimitive) this;
        return null;
    }

    @Contract(pure = true)
    private static boolean isPrimitiveOrWrapper(@NotNull Class<?> clazz) {
        return clazz.isPrimitive()
                || clazz == Boolean.class
                || clazz == Integer.class
                || clazz == Long.class
                || clazz == Double.class
                || clazz == Float.class
                || clazz == Short.class
                || clazz == Byte.class
                || clazz == Character.class;
    }

}
