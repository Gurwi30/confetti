package dev.gurwi.confetti.element;

public class YamlPrimitive implements YamlElement {

    private final Object value;

    public YamlPrimitive(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public String toString() {
        return String.valueOf(value);
    }

}
