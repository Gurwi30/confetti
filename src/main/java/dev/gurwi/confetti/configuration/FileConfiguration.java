package dev.gurwi.confetti.configuration;

import dev.gurwi.confetti.Confetti;
import dev.gurwi.confetti.configuration.base.Configuration;
import dev.gurwi.confetti.element.YamlElement;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class FileConfiguration extends Configuration {

    private final File file;
    private boolean autoSave = false;

    public FileConfiguration(Confetti confetti, @NotNull File file) {
        super(confetti, file.getPath());
        this.file = file;
    }

    public FileConfiguration withAutoSave(boolean autoSave) {
        this.autoSave = autoSave;
        return this;
    }

    @Override
    protected @NotNull Map<String, YamlElement> loadData() {
        try (InputStream in = new FileInputStream(file)) {
            return YamlElement.adapt(yaml.load(in));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void reload() {
        this.data = loadData();
        this.section = new Section(confetti, this, data);
    }

    public void save() {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8)) {
            yaml.dump(data, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
