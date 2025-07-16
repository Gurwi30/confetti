package dev.gurwi.confetti;

import dev.gurwi.confetti.annotation.Config;
import dev.gurwi.confetti.annotation.Path;
import dev.gurwi.confetti.annotation.ResourceConfig;
import dev.gurwi.confetti.configuration.base.Configuration;
import dev.gurwi.confetti.configuration.FileConfiguration;
import dev.gurwi.confetti.configuration.ResourceConfiguration;
import dev.gurwi.confetti.serde.YamlDeserializer;
import dev.gurwi.confetti.serde.internal.ReflectiveYamlDeserializer;
import dev.gurwi.confetti.serde.internal.ReflectiveYamlSerializer;
import dev.gurwi.confetti.serde.internal.YamlMapperRegistry;
import dev.gurwi.confetti.serde.YamlSerDe;
import dev.gurwi.confetti.serde.YamlSerializer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public final class Confetti {

    private final YamlMapperRegistry mapperRegistry = new YamlMapperRegistry();
    private final Map<String, Configuration> configurations = new HashMap<>();
    private final Map<Class<?>, FileConfiguration> fileConfigurationClasses = new HashMap<>();

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

    public <T> YamlSerializer<T> getSerializer(Class<T> type) {
        return mapperRegistry.getSerializer(type).orElse(new ReflectiveYamlSerializer<>());
    }

    public <T> YamlDeserializer<T> getDeserializer(Class<T> type, Type genericType) {
        return mapperRegistry.getDeserializer(type)
                .orElse(new ReflectiveYamlDeserializer<>(type, genericType));
    }

    public <T> YamlDeserializer<T> getDeserializer(Class<T> type) {
        return mapperRegistry.getDeserializer(type)
                .orElse(new ReflectiveYamlDeserializer<>(type));
    }

    @Contract("_ -> new")
    public @NotNull ResourceConfiguration fromResource(String path) {
        return new ResourceConfiguration(this, path);
    }

    @Contract("_ -> new")
    public @NotNull FileConfiguration fromFile(File file) {
        return new FileConfiguration(this, file);
    }

    @Contract("_ -> new")
    public @NotNull FileConfiguration fromFile(String path) {
        return fromFile(new File(path));
    }

    @Contract("_, _ -> new")
    public @NotNull FileConfiguration fromFile(String path, File parent) {
        return fromFile(new File(parent, path));
    }

    public Confetti loadIntoClass(@NotNull Class<?> clazz, Configuration config) {
        for (Field field : clazz.getFields()) {
            int fieldModifiers = field.getModifiers();

            if (Modifier.isFinal(fieldModifiers)) continue;
            if (!Modifier.isStatic(fieldModifiers)) continue;

            if (!field.isAnnotationPresent(Path.class)) continue;

            boolean isPublic = Modifier.isPublic(fieldModifiers);

            if (!isPublic) field.setAccessible(true);

            Path pathAnno = field.getAnnotation(Path.class);
            Class<?> type = field.getType();
            Object value = config.get(pathAnno.value(), type, field.getGenericType());

            if (value != null) {
                try {
                    field.set(null, value);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }

            if (!isPublic) field.setAccessible(false);
        }

        if (config instanceof FileConfiguration fileConfig) {
            fileConfigurationClasses.put(config.getClass(), fileConfig);
        }

        return this;
    }

    public @NotNull Configuration load(@NotNull Class<?> clazz, @Nullable File parentFolder) {
        Config configAnno = clazz.getAnnotation(Config.class);
        ResourceConfig resourceConfigAnno = clazz.getAnnotation(ResourceConfig.class);

        if (configAnno == null && resourceConfigAnno == null) {
            throw new IllegalArgumentException("Class " + clazz.getName() +
                    " must be annotated with either @Config or @ResourceConfig");
        }

        if (configAnno != null && resourceConfigAnno != null) {
            throw new IllegalArgumentException("Class " + clazz.getName() +
                    " cannot be annotated with both @Config and @ResourceConfig");
        }

        Configuration config = resourceConfigAnno != null
                ? fromResource(resourceConfigAnno.value())
                : (parentFolder != null ? fromFile(new File(parentFolder, configAnno.value())) : fromFile(configAnno.value()))
                .withAutoSave(configAnno.autoSave())
                .withDefaultResource(configAnno.defaultResource())
                .load();

        configurations.put(config.getPath(), config);
        loadIntoClass(clazz, config);

        return config;
    }

    public @NotNull Configuration load(@NotNull Class<?> clazz) {
        return load(clazz, null);
    }

    public void reloadAll() {
        getFromType(FileConfiguration.class)
                .forEach(FileConfiguration::reload);

        fileConfigurationClasses.forEach(this::loadIntoClass);
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
