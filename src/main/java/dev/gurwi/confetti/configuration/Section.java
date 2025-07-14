package dev.gurwi.confetti.configuration;

import dev.gurwi.confetti.Confetti;
import dev.gurwi.confetti.configuration.base.Configuration;
import dev.gurwi.confetti.configuration.base.ConfigurationSection;
import dev.gurwi.confetti.element.YamlElement;

import java.lang.reflect.Type;
import java.util.Map;

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
        return false;
    }

    @Override
    public boolean exists(String path) {
        return false;
    }

    @Override
    public String getString(String path) {
        return "";
    }

    @Override
    public int getInt(String path) {
        return 0;
    }

    @Override
    public long getLong(String path) {
        return 0;
    }

    @Override
    public double getDouble(String path) {
        return 0;
    }

    @Override
    public float getFloat(String path) {
        return 0;
    }

    @Override
    public boolean getBoolean(String path) {
        return false;
    }

    @Override
    public Object get(String path, Object defaultValue) {
        return data.get(path).asPrimitive().getValue();
    }

    @Override
    public <T> T get(String path, Class<T> type) {
        YamlElement value = data.get(path);

        if (value == null) return null;

        return confetti.getDeserializer(type)
                .deserialize(value);
    }

    @Override
    public <T> T get(String path, Class<T> type, Type genericType) {
        YamlElement value = data.get(path);

        if (value == null) return null;

        return confetti.getDeserializer(type, genericType)
                .deserialize(value);
    }

    @Override
    public ConfigurationSection set(String path, Object value) {
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

}
