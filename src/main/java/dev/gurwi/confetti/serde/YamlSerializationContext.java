package dev.gurwi.confetti.serde;

import dev.gurwi.confetti.Confetti;
import dev.gurwi.confetti.element.YamlElement;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class YamlSerializationContext {

    private final Confetti confetti;

    public YamlSerializationContext(Confetti confetti) {
        this.confetti = confetti;
    }

    public <T> YamlElement serialize(@NotNull T obj) {
        @SuppressWarnings("unchecked") YamlSerializer<Object> serializer = (YamlSerializer<Object>) confetti.getSerializer(obj.getClass());
        return serializer.serialize(obj, this);
    }

    public Confetti confetti() {
        return confetti;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;

        YamlSerializationContext that = (YamlSerializationContext) obj;
        return Objects.equals(this.confetti, that.confetti);
    }

    @Override
    public int hashCode() {
        return Objects.hash(confetti);
    }

    @Override
    public String toString() {
        return "YamlSerializationContext[" +
                "confetti=" + confetti + ']';
    }

}
