package dev.gurwi.confetti.configuration;

import dev.gurwi.confetti.Confetti;
import dev.gurwi.confetti.configuration.base.Configuration;
import dev.gurwi.confetti.configuration.base.ConfigurationSection;
import dev.gurwi.confetti.element.YamlElement;
import dev.gurwi.confetti.element.YamlObject;
import dev.gurwi.confetti.serde.YamlSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.*;

public class Section implements ConfigurationSection {

    private final Confetti confetti;
    private final Configuration root;
    private final Map<String, YamlElement> data;

    public Section(Confetti confetti, Configuration root, Map<String, YamlElement> data) {
        this.confetti = confetti;
        this.root = root;
        this.data = data;
    }

    @Override
    public boolean isEmpty(String path) {
        YamlElement element = resolvePath(path);
        return element == null || element.isEmpty();
    }

    @Override
    public boolean exists(String path) {
        return resolvePath(path) != null;
    }

    @Override
    public @Nullable String getString(String path) {
        YamlElement element = resolvePath(path);
        if (element == null) return null;

        Object value = element.asPrimitive().getUnchecked();
        return value != null ? value.toString() : null;
    }

    @Override
    public @Nullable Integer getInt(String path) {
        YamlElement element = resolvePath(path);
        if (element == null) return null;

        Object value = element.asPrimitive().getUnchecked();
        return value instanceof Number number ? number.intValue() : null;
    }

    @Override
    public @Nullable Long getLong(String path) {
        YamlElement element = resolvePath(path);
        if (element == null) return null;

        Object value = element.asPrimitive().getUnchecked();
        return value instanceof Number number ? number.longValue() : null;
    }

    @Override
    public @Nullable Double getDouble(String path) {
        YamlElement element = resolvePath(path);
        if (element == null) return null;

        Object value = element.asPrimitive().getUnchecked();
        return value instanceof Number number ? number.doubleValue() : null;
    }

    @Override
    public @Nullable Float getFloat(String path) {
        YamlElement element = resolvePath(path);
        if (element == null) return null;

        Object value = element.asPrimitive().getUnchecked();
        return value instanceof Number number ? number.floatValue() : null;
    }

    @Override
    public @Nullable Boolean getBool(String path) {
        YamlElement element = resolvePath(path);
        if (element == null) return null;

        Object value = element.asPrimitive().getUnchecked();
        return value instanceof Boolean bool ? bool : null;
    }

    @Override
    public Object get(String path, Object defaultValue) {
        Object value = get(path);
        return value == null ? defaultValue : value;
    }

    @Override
    public <T> T get(String path, Class<T> type) {
        return get(path, type, type);
    }

    @Override
    public <T> T get(String path, Class<T> type, Type genericType) {
        YamlElement value = resolvePath(path);

        if (value == null) return null;

        return confetti.getDeserializer(type, genericType)
                .deserialize(value);
    }

    @Override
    public ConfigurationSection set(String path, Object value) {
        if (value == null) {
            data.remove(path);
            return this;
        }

        if (YamlElement.class.isAssignableFrom(value.getClass())) data.put(path, (YamlElement) value);
        else {
            @SuppressWarnings("unchecked")
            YamlSerializer<Object> serializer = (YamlSerializer<Object>) confetti.getSerializer(value.getClass());

            data.put(path, serializer.serialize(value));
        }

        return this;
    }

    @Override
    public ConfigurationSection getConfigurationSection(String path) {
        return null;
    }

    @Override
    public ConfigurationSection createSection(String path) {
        return null;
    }

    public Configuration getRoot() {
        return root;
    }

    private @Nullable YamlElement resolvePath(@NotNull String path) {
        String[] parts = path.split("\\.");
        YamlElement current = data.get(parts[0]);

        for (int i = 1; i < parts.length; i++) {
            if (!(current instanceof YamlObject obj)) {
                return null;
            }

            current = obj.get(parts[i]);
            if (current == null) {
                return null;
            }
        }

        return current;
    }

}
