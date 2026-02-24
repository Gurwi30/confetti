package dev.gurwi.confetti.serde.internal;

import dev.gurwi.confetti.Confetti;
import dev.gurwi.confetti.annotation.Path;
import dev.gurwi.confetti.element.YamlElement;
import dev.gurwi.confetti.element.YamlObject;
import dev.gurwi.confetti.element.YamlPrimitive;
import dev.gurwi.confetti.serde.YamlSerializationContext;
import dev.gurwi.confetti.serde.YamlSerializer;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;

public class ReflectiveYamlSerializer<T> implements YamlSerializer<T> {

    private final Confetti confetti;

    public ReflectiveYamlSerializer(Confetti confetti) {
        this.confetti = confetti;
    }

    @Override
    public YamlElement serialize(T obj, YamlSerializationContext ctx) {
        if (obj == null) return YamlPrimitive.NULL;

        try {
            return YamlElement.adapt(obj);
        } catch (IllegalArgumentException e) {
            return serializeObject(obj);
        }

    }

    private YamlElement serializeObject(@NotNull T obj) {
        if (obj.getClass().isRecord()) {
            return serializeRecord(obj);
        }

        if (obj.getClass().isEnum()) {
            return new YamlPrimitive(((Enum<?>) obj).name());
        }

        return serializeClass(obj);
    }

    private @NotNull YamlObject serializeRecord(@NotNull T obj) {
        YamlObject yamlObject = new YamlObject();
        Class<?> type = obj.getClass();

        try {
            for (RecordComponent component : type.getRecordComponents()) {
                Method accessor = component.getAccessor();
                Object value = accessor.invoke(obj);

                String key = component.getName();
                Field field = type.getDeclaredField(component.getName());

                if (field.isAnnotationPresent(Path.class)) {
                    key = field.getAnnotation(Path.class).value();
                }

                yamlObject.set(key, serializeValue(value));
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize record: " + type.getName(), e);
        }

        return yamlObject;
    }

    private @NotNull YamlObject serializeClass(@NotNull T obj) {
        YamlObject yamlObject = new YamlObject();
        Class<?> type = obj.getClass();

        for (Field field : type.getDeclaredFields()) {
            if (!field.isAnnotationPresent(Path.class)) continue;

            String key = field.getAnnotation(Path.class).value();

            boolean wasAccessible = field.canAccess(obj);
            if (!wasAccessible) field.setAccessible(true);

            try {
                Object value = field.get(obj);
                yamlObject.set(key, serializeValue(value));
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to serialize field: " + field.getName(), e);
            }

            if (!wasAccessible) field.setAccessible(false);
        }

        return yamlObject;
    }

    private YamlElement serializeValue(Object value) {
        if (value == null) return YamlPrimitive.NULL;
        return new ReflectiveYamlSerializer<>(confetti).serialize(value, new YamlSerializationContext(confetti));
    }

}
