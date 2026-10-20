package dev.gurwi.confetti.serde;

import dev.gurwi.confetti.Confetti;
import dev.gurwi.confetti.element.YamlElement;

import java.util.Objects;

public final class YamlDeserializationContext {

    private final Confetti confetti;

    public YamlDeserializationContext(Confetti confetti) {
        this.confetti = confetti;
    }

    public <T> T deserialize(YamlElement element, Class<T> typeOfT) {
        return confetti.getDeserializer(typeOfT).deserialize(element, this);
    }

    public Confetti confetti() {
        return confetti;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;

        YamlDeserializationContext that = (YamlDeserializationContext) obj;
        return Objects.equals(this.confetti, that.confetti);
    }

    @Override
    public int hashCode() {
        return Objects.hash(confetti);
    }

    @Override
    public String toString() {
        return "YamlDeserializationContext[" +
                "confetti=" + confetti + ']';
    }

}
