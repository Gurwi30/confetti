package dev.gurwi.confetti;

public interface ConfigurationSection {

    boolean isEmpty(String path);

    boolean exists(String path);

    String getString(String path);

    int getInt(String path);

    long getLong(String path);

    double getDouble(String path);

    float getFloat(String path);

    boolean getBoolean(String path);

    Object get(String path);

    <T> T get(String path, Class<T> type);

    ConfigurationSection getConfigurationSection(String path);

    ConfigurationSection createSection(String path);

}
