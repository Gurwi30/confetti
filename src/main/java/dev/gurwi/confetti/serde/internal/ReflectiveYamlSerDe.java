package dev.gurwi.confetti.serde.internal;

import dev.gurwi.confetti.element.YamlElement;
import dev.gurwi.confetti.serde.YamlSerDe;

public class ReflectiveYamlSerDe<T> implements YamlSerDe<T> {

    @Override
    public T deserialize(YamlElement node) {
        return null;
    }

    @Override
    public YamlElement serialize(T obj) {
        return null;
    }
    
}
