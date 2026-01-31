package dev.gurwi.confetti.serde;

import dev.gurwi.confetti.Confetti;
import dev.gurwi.confetti.element.YamlElement;

public record YamlDeserializationContext(Confetti confetti) {

    public <T> T deserialize(YamlElement element, Class<T> typeOfT) {
        return confetti.getDeserializer(typeOfT).deserialize(element, this);
    }

}
