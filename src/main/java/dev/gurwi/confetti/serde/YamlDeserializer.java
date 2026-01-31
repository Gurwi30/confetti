package dev.gurwi.confetti.serde;

import dev.gurwi.confetti.element.YamlElement;

public interface YamlDeserializer<T> {

    T deserialize(YamlElement node, YamlDeserializationContext ctx);

}
