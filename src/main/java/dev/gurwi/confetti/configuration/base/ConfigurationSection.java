package dev.gurwi.confetti.configuration.base;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

public interface ConfigurationSection {

    boolean isEmpty(String path);

    boolean exists(String path);

    @Nullable String getString(String path);

    @Nullable Integer getInt(String path);

    @Nullable Long getLong(String path);

    @Nullable Double getDouble(String path);

    @Nullable Float getFloat(String path);

    @Nullable Boolean getBool(String path);

    Object get(String path, Object defaultValue);

    default Object get(String path) {
        return get(path, Object.class);
    }

    @Nullable <T> T get(String path, Class<T> type);

    ConfigurationSection set(String path, Object value);

    @Nullable <T> T get(String path, Class<T> type, Type genericType);

    @Nullable ConfigurationSection getConfigurationSection(String path);

    ConfigurationSection createSection(String path);

}
