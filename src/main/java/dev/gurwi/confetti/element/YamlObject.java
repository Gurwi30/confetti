package dev.gurwi.confetti.element;

import dev.gurwi.confetti.configuration.base.ConfigurationSection;

import java.lang.reflect.Type;
import java.util.Map;

public class YamlObject implements YamlElement, ConfigurationSection {

    private final Map<String, YamlElement> elements;

    public YamlObject(Map<String, YamlElement> elements) {
        this.elements = elements;
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
        return null;
    }

    public YamlElement get(String key) {
        return elements.get(key);
    }

    @Override
    public <T> T get(String path, Class<T> type) {
        return null;
    }

    @Override
    public ConfigurationSection set(String path, Object value) {
        return null;
    }

    @Override
    public <T> T get(String path, Class<T> type, Type genericType) {
        return null;
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
