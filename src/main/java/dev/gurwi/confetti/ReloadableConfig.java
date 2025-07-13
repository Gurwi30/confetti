package dev.gurwi.confetti;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public class ReloadableConfig extends Configuration {

    private final File file;

    public ReloadableConfig(Confetti confetti, @NotNull File file) {
        super(confetti, file.getPath());
        this.file = file;
    }

    public void save() {

    }

    public void reload() {

    }

}
