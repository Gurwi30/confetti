package dev.gurwi.confetti.configuration.base;

import dev.gurwi.confetti.Confetti;
import dev.gurwi.confetti.configuration.Section;
import dev.gurwi.confetti.element.YamlElement;
import dev.gurwi.confetti.element.rapresenter.YamlElementRepresenter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.lang.reflect.Type;
import java.util.Map;

public abstract class Configuration implements ConfigurationSection {

    protected final Confetti confetti;
    protected final String path;
    protected final Yaml yaml;

    protected Map<String, YamlElement> data;
    protected Section section;

    public Configuration(Confetti confetti, String path) {
        this.confetti = confetti;
        this.path = path;

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        this.yaml = new Yaml(new YamlElementRepresenter(options), options);
    }

    protected abstract @NotNull Map<String, YamlElement> loadData();

    public Configuration load() {
        this.data = loadData();
        this.section = new Section(confetti, this, data);

        return this;
    }

    @Override
    public boolean isEmpty(String path) {
        return section.isEmpty(path);
    }

    @Override
    public boolean exists(String path) {
        return section.exists(path);
    }

    @Override
    public @Nullable String getString(String path) {
        return section.getString(path);
    }

    @Override
    public @Nullable Integer getInt(String path) {
        return section.getInt(path);
    }

    @Override
    public @Nullable Long getLong(String path) {
        return section.getLong(path);
    }

    @Override
    public @Nullable Double getDouble(String path) {
        return section.getDouble(path);
    }

    @Override
    public @Nullable Float getFloat(String path) {
        return section.getFloat(path);
    }

    @Override
    public @Nullable Boolean getBool(String path) {
        return section.getBool(path);
    }

    @Override
    public @Nullable Object get(String path) {
        return section.get(path);
    }

    @Override
    public @Nullable <T> T get(String path, Class<T> type) {
        return get(path, type, type);
    }

    @Override
    public @Nullable <T> T get(String path, Class<T> type, Type genericType) {
        return section.get(path, type, genericType);
    }

    @Override
    public Object get(String path, Object defaultValue) {
        return section.get(path, defaultValue);
    }

    @Override
    public @Nullable ConfigurationSection getConfigurationSection(String path) {
        return section.getConfigurationSection(path);
    }

    @Override
    public ConfigurationSection createSection(String path) {
        return section.createSection(path);
    }

    @Override
    public ConfigurationSection set(String path, Object value) {
        return section.set(path, value);
    }

    public String getPath() {
        return this.path;
    }

}
