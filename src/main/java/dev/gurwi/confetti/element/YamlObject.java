package dev.gurwi.confetti.element;

import java.util.Map;

public class YamlObject implements YamlElement {

    private final Map<String, YamlElement> data;

    public YamlObject(Map<String, YamlElement> data) {
        this.data = data;
    }

    public YamlObject set(String key, YamlElement value) {
        data.put(key, value);
        return this;
    }

    public YamlElement get(String key) {
        return data.get(key);
    }

    public Map<String, YamlElement> getData() {
        return data;
    }

}
