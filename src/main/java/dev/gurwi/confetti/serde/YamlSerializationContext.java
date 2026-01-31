package dev.gurwi.confetti.serde;

import dev.gurwi.confetti.Confetti;
import dev.gurwi.confetti.element.YamlElement;
import org.jetbrains.annotations.NotNull;

public record YamlSerializationContext(Confetti confetti) {

    public <T> YamlElement serialize(@NotNull T obj) {
        @SuppressWarnings("unchecked") YamlSerializer<Object> serializer = (YamlSerializer<Object>) confetti.getSerializer(obj.getClass());
        return serializer.serialize(obj, this);
    }

}
