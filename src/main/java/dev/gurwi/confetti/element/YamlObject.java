package dev.gurwi.confetti.element;

import dev.gurwi.confetti.serde.internal.ReflectiveYamlSerializer;

import java.util.HashMap;
import java.util.Map;

public class YamlObject implements YamlElement {

    private final Map<String, YamlElement> data;

    public YamlObject(Map<String, YamlElement> data) {
        this.data = data;
    }

    public YamlObject() {
        this(new HashMap<>());
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    public YamlObject set(String key, YamlElement value) {
        data.put(key, value);
        return this;
    }

    @Deprecated(forRemoval = true)
    public YamlObject set(String key, Object value) {
        data.put(key, new ReflectiveYamlSerializer<>(null).serialize(value, null));
        return this;
    }

    public YamlElement get(String key) {
        return data.get(key);
    }

    public Map<String, YamlElement> getData() {
        return data;
    }

}
