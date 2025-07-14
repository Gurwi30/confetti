package dev.gurwi.confetti.configuration;

import dev.gurwi.confetti.Confetti;
import dev.gurwi.confetti.configuration.base.Configuration;
import dev.gurwi.confetti.element.YamlElement;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Map;

public class FileConfiguration extends Configuration {

    private final File file;

    public FileConfiguration(Confetti confetti, @NotNull File file) {
        super(confetti, file.getPath());
        this.file = file;
    }

    @Override
    protected @NotNull Map<String, YamlElement> loadData() {
        try (InputStream in = new FileInputStream(file)) {
            return YamlElement.adapt(yaml.load(in));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
