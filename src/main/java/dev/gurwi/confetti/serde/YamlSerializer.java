package dev.gurwi.confetti.serde;

import dev.gurwi.confetti.element.YamlElement;

@FunctionalInterface
public interface YamlSerializer<T> {

    YamlElement serialize(T obj);

}
