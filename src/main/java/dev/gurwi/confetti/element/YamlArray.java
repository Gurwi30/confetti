package dev.gurwi.confetti.element;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

public class YamlArray implements YamlElement, Iterable<YamlElement> {

    private final List<YamlElement> elements;

    public YamlArray(List<YamlElement> elements) {
        this.elements = elements;
    }

    @Override
    public @NotNull Iterator<YamlElement> iterator() {
        return elements.iterator();
    }

    public int size() {
        return elements.size();
    }

    public YamlElement get(int index) {
        return elements.get(index);
    }

    public List<YamlElement> getElements() {
        return elements;
    }

}
