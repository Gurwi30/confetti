package dev.gurwi.confetti;

import dev.gurwi.confetti.annotation.Path;
import dev.gurwi.confetti.configuration.FileConfiguration;
import dev.gurwi.confetti.configuration.base.Configuration;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class LoadedClassData {

    private final Map<Field, Object> originalValues = new HashMap<>();

    private final Class<?> clazz;
    private final Configuration config;

    public LoadedClassData(Class<?> clazz, Configuration config) {
        this.clazz = clazz;
        this.config = config;
    }

    private void captureInitialValues() {
        for (Field field : clazz.getFields()) {
            if (!Modifier.isStatic(field.getModifiers())) continue;
            if (!field.isAnnotationPresent(Path.class)) continue;

            boolean wasAccessible = field.canAccess(null);
            if (!wasAccessible) field.setAccessible(true);

            try {
                Object value = field.get(null);
                originalValues.put(field, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to capture field value", e);
            }

            if (!wasAccessible) field.setAccessible(false);
        }
    }

    public void restoreInitialValues() {
        for (Map.Entry<Field, Object> entry : originalValues.entrySet()) {
            Field field = entry.getKey();
            Object originalValue = entry.getValue();

            boolean wasAccessible = field.canAccess(null);
            if (!wasAccessible) field.setAccessible(true);

            try {
                field.set(null, originalValue);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to restore field value", e);
            }

            if (!wasAccessible) field.setAccessible(false);
        }
    }

    public Configuration getConfig() {
        return config;
    }

}
