package dev.gurwi.confetti.element;

import java.util.List;

public class YamlArray {

    private final List<YamlElement> elements;

    public YamlArray(List<YamlElement> elements) {
        this.elements = elements;
    }

    public List<YamlElement> getElements() {
        return elements;
    }

}
