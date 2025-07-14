package dev.gurwi.confetti.element;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        if (obj == null) return new YamlPrimitive(null);

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

            default -> new YamlPrimitive(obj);
        };
    }

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

}
