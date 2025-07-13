package dev.gurwi.confetti;

public abstract class Configuration implements ConfigurationSection {

    protected final Confetti confetti;
    private final String path;

    public Configuration(Confetti confetti, String path) {
        this.confetti = confetti;
        this.path = path;
    }

    public void load() {

    }

    public boolean isEmpty(String path) {
        return false;
    }

    public boolean exists(String path) {
        return false;
    }

    public String getString(String path) {
        return null;
    }

    public int getInt(String path) {
        return 0;
    }

    public long getLong(String path) {
        return 0;
    }

    public double getDouble(String path) {
        return 0;
    }

    public float getFloat(String path) {
        return 0;
    }

    public boolean getBoolean(String path) {
        return false;
    }

    public Object get(String path) {
        return null;
    }

    public <T> T get(String path, Class<T> type) {
        return null;
    }

    public ConfigurationSection getConfigurationSection(String path) {
        return null;
    }

    public ConfigurationSection createSection(String path) {
        return null;
    }

    public String getPath() {
        return this.path;
    }

}
