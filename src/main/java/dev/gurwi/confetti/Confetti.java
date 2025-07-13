package dev.gurwi.confetti;

import dev.gurwi.confetti.annotation.Config;
import dev.gurwi.confetti.annotation.Path;
import dev.gurwi.confetti.serde.YamlDeserializer;
import dev.gurwi.confetti.serde.internal.YamlMapperRegistry;
import dev.gurwi.confetti.serde.YamlSerDe;
import dev.gurwi.confetti.serde.YamlSerializer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public final class Confetti {

    private final YamlMapperRegistry mapperRegistry = new YamlMapperRegistry();
    private final Map<String, Configuration> configurations = new HashMap<>();

    public <T> Confetti registerSerializer(Class<T> type, YamlSerializer<T> serializer) {
        mapperRegistry.registerSerializer(type, serializer);
        return this;
    }

    public <T> Confetti registerDeserializer(Class<T> type, YamlDeserializer<T> deserializer) {
        mapperRegistry.registerDeserializer(type, deserializer);
        return this;
    }

    public <T> Confetti registerSerDe(Class<T> type, YamlSerDe<T> serDe) {
        mapperRegistry.registerSerDe(type, serDe);
        return this;
    }

    @Contract("_ -> new")
    public @NotNull ResourceConfig fromResource(String path) {
        return new ResourceConfig(this, path);
    }

    @Contract("_ -> new")
    public @NotNull ReloadableConfig fromFile(File file) {
        return new ReloadableConfig(this, file);
    }

    @Contract("_ -> new")
    public @NotNull ReloadableConfig fromFile(String path) {
        return fromFile(new File(path));
    }

    public @NotNull Configuration load(@NotNull Class<?> clazz) {
        Config configAnno = clazz.getAnnotation(Config.class);

        if (configAnno == null) {
            throw new IllegalArgumentException("Class " + clazz + " has no @Config annotation");
        }

        Configuration config = configAnno.resourceConfig()
                ? fromResource(configAnno.value())
                : fromFile(configAnno.value());

        for (Field field : clazz.getFields()) {
            if (Modifier.isFinal(field.getModifiers())) continue;
            if (!Modifier.isStatic(field.getModifiers())) continue;

            if (!field.isAnnotationPresent(Path.class)) continue;

            boolean isPublic = Modifier.isPublic(field.getModifiers());

            if (!isPublic) field.setAccessible(true);

            Path pathAnno = field.getAnnotation(Path.class);
            Class<?> type = field.getType();
            Object value = config.get(pathAnno.value(), type);

            try {
                field.set(null, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            if (!isPublic) field.setAccessible(false);
        }

        configurations.put(config.getPath(), config);
        return config;
    }

    public void reloadAll() {
        getFromType(ReloadableConfig.class)
                .forEach(ReloadableConfig::reload);
    }

    public @NotNull Optional<Configuration> get(String path) {
        return Optional.ofNullable(configurations.get(path));
    }

    public @NotNull @Unmodifiable Set<Configuration> getAll() {
        return Set.copyOf(configurations.values());
    }

    public <T extends Configuration> @NotNull @Unmodifiable Set<T> getFromType(@NotNull Class<T> type) {
        return configurations.values().stream()
                .filter(configuration -> type.isAssignableFrom(configuration.getClass()))
                .map(type::cast)
                .collect(Collectors.toUnmodifiableSet());
    }

    public YamlMapperRegistry getMapperRegistry() {
        return mapperRegistry;
    }

}
