package dev.gurwi.confetti.element;

import java.util.Map;

public class YamlObject implements YamlElement {

    private final Map<String, YamlElement> elements;

    public YamlObject(Map<String, YamlElement> elements) {
        this.elements = elements;
    }

    public YamlElement get(String key) {
        return elements.get(key);
    }

}
