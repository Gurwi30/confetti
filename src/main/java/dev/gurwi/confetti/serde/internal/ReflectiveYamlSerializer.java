package dev.gurwi.confetti.serde.internal;

import dev.gurwi.confetti.element.YamlElement;
import dev.gurwi.confetti.serde.YamlSerializer;

public class ReflectiveYamlSerializer<T> implements YamlSerializer<T> {

    private final Class<T> type;

    public ReflectiveYamlSerializer(Class<T> type) {
        this.type = type;
    }

    @Override
    public YamlElement serialize(T obj) {
        return null;
    }

}
