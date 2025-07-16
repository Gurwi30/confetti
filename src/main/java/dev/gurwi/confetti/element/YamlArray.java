package dev.gurwi.confetti.element;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class YamlArray implements YamlElement, Iterable<YamlElement> {

    private final List<YamlElement> elements;

    public YamlArray(List<YamlElement> elements) {
        this.elements = elements;
    }

    public YamlArray() {
        this(new ArrayList<>());
    }

    @Override
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    @Override
    public @NotNull Iterator<YamlElement> iterator() {
        return elements.iterator();
    }

    public YamlArray add(@NotNull YamlElement element) {
        elements.add(element);
        return this;
    }

    public YamlArray insert(int index, @NotNull YamlElement element) {
        elements.add(index, element);
        return this;
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
