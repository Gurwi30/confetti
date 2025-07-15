package dev.gurwi.confetti.configuration.base;

import java.lang.reflect.Type;

public interface ConfigurationSection {

    boolean isEmpty(String path);

    boolean exists(String path);

    String getString(String path);

    int getInt(String path);

    long getLong(String path);

    double getDouble(String path);

    float getFloat(String path);

    boolean getBoolean(String path);

    Object get(String path, Object defaultValue);

    default Object get(String path) {
        return get(path, Object.class);
    }

    <T> T get(String path, Class<T> type);

    ConfigurationSection set(String path, Object value);

    <T> T get(String path, Class<T> type, Type genericType);

    ConfigurationSection getConfigurationSection(String path);

    ConfigurationSection createSection(String path);

}
