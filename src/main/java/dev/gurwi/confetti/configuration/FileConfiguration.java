package dev.gurwi.confetti.configuration;

import dev.gurwi.confetti.Confetti;
import dev.gurwi.confetti.configuration.base.Configuration;
import dev.gurwi.confetti.configuration.base.ConfigurationSection;
import dev.gurwi.confetti.element.YamlElement;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

public class FileConfiguration extends Configuration {

    private final File file;

    private String defaultResource = null;
    private boolean autoSave = false;

    public FileConfiguration(Confetti confetti, @NotNull File file) {
        super(confetti, file.getPath());

        this.file = file;
    }

    public FileConfiguration withAutoSave(boolean autoSave) {
        this.autoSave = autoSave;
        return this;
    }

    public FileConfiguration withDefaultResource(String defaultResource) {
        this.defaultResource = defaultResource;
        return this;
    }

    @Override
    protected @NotNull Map<String, YamlElement> loadData() {
        createFile();

        try (InputStream in = new FileInputStream(file)) {
            return YamlElement.adapt(yaml.load(in));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void reload() {
        this.data = loadData();
        this.section = new Section(confetti, this, data);

        syncAnnotationClasses();
    }

    public void save() {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8)) {
            yaml.dump(data, writer);
            syncAnnotationClasses();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ConfigurationSection set(String path, Object value) {
        super.set(path, value);
        runAutoSave();

        return this;
    }

    @Override
    public ConfigurationSection createSection(String path) {
        ConfigurationSection ret = super.createSection(path);
        runAutoSave();

        return ret;
    }

    private void createFile() {
        File parent = file.getParentFile();

        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        if (!file.exists()) {
            if (defaultResource != null) {
                copyDefaultResource(defaultResource, file);
            } else {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException("Failed to create empty config file: " + file, e);
                }
            }
        }
    }

    private void copyDefaultResource(String resourceName, File targetFile) {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (in == null) {
                throw new FileNotFoundException("Resource not found in classpath: " + resourceName);
            }

            Files.copy(in, targetFile.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to copy default resource: " + resourceName, e);
        }
    }

    private void runAutoSave() {
        if (autoSave) save();
    }

    private void syncAnnotationClasses() {
        getLoadedClass().ifPresent(data -> {
            data.restoreInitialValues();
            confetti.loadIntoClass(getClass(), this);
        });
    }

}
