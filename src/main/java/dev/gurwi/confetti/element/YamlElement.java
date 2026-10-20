package dev.gurwi.confetti.element;

import org.jetbrains.annotations.ApiStatus;
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

        if (obj instanceof Collection<?>) {
            Collection<?> collection = (Collection<?>) obj;
            return new YamlArray(
                    collection.stream()
                            .map(YamlElement::adapt)
                            .collect(Collectors.toList())
            );
        }

        if (obj instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) obj;
            Map<String, YamlElement> elements = new LinkedHashMap<>();

            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String path = (String) entry.getKey();
                elements.put(path, adapt(entry.getValue()));
            }

            return new YamlObject(elements);
        }

        if (obj.getClass().isArray()) {
            YamlArray array = new YamlArray();
            int length = Array.getLength(obj);

            for (int i = 0; i < length; i++) {
                Object element = Array.get(obj, i);
                array.add(adapt(element));
            }

            return array;
        }

        if (isPrimitiveOrWrapper(obj.getClass()) || obj instanceof String) {
            return new YamlPrimitive(obj);
        }

        throw new IllegalArgumentException("Cannot adapt " + obj.getClass().getName() + " to " + YamlElement.class);
    }

    @ApiStatus.Internal
    default Object unwrap(@NotNull YamlElement element) {
        if (element instanceof YamlPrimitive) {
            return ((YamlPrimitive) element).getValue();
        }

        if (element instanceof YamlArray) {
            YamlArray array = (YamlArray) element;
            return array.getElements().stream()
                    .map(this::unwrap)
                    .collect(Collectors.toList());
        }

        if (element instanceof YamlObject) {
            YamlObject object = (YamlObject) element;
            Map<String, Object> map = new LinkedHashMap<>();

            for (Map.Entry<String, YamlElement> entry : object.getData().entrySet()) {
                map.put(entry.getKey(), unwrap(entry.getValue()));
            }

            return map;
        }

        throw new IllegalStateException("Unexpected value: " + element);
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
    @ApiStatus.Internal
    static boolean isPrimitiveOrWrapper(@NotNull Class<?> clazz) {
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
