package dev.gurwi.confetti.serde.internal;

import dev.gurwi.confetti.serde.YamlDeserializer;
import dev.gurwi.confetti.serde.YamlSerDe;
import dev.gurwi.confetti.serde.YamlSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class YamlMapperRegistry {

    private final Map<Class<?>, YamlSerializer<?>> serializers = new HashMap<>();
    private final Map<Class<?>, YamlDeserializer<?>> deserializers = new HashMap<>();

    public <T> void registerSerializer(Class<T> type, YamlSerializer<T> serializer) {
        serializers.put(type, serializer);
    }

    public <T> void registerDeserializer(Class<T> type, YamlDeserializer<T> deserializer) {
        deserializers.put(type, deserializer);
    }

    public <T> void registerSerDe(Class<T> type, YamlSerDe<T> serDe) {
        deserializers.put(type, serDe);
        serializers.put(type, serDe);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<YamlSerializer<T>> getSerializer(Class<T> type) {
        return Optional.ofNullable((YamlSerializer<T>) serializers.get(type));
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<YamlDeserializer<T>> getDeserializer(Class<T> type) {
        return Optional.ofNullable((YamlDeserializer<T>) serializers.get(type));
    }

}
