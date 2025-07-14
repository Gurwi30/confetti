package dev.gurwi.confetti.configuration;

import dev.gurwi.confetti.Confetti;
import dev.gurwi.confetti.configuration.base.Configuration;
import dev.gurwi.confetti.element.YamlElement;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class ResourceConfiguration extends Configuration {

    public ResourceConfiguration(Confetti confetti, String path) {
        super(confetti, path);
    }

    @Override
    protected @NotNull Map<String, YamlElement> loadData() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(path)) {
            return YamlElement.adapt(yaml.load(in));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void reload() {

    }

    @Override
    public void save() {

    }

}
