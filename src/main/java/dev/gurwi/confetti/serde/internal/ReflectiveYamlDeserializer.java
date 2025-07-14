package dev.gurwi.confetti.serde.internal;

import dev.gurwi.confetti.annotation.Path;
import dev.gurwi.confetti.element.YamlArray;
import dev.gurwi.confetti.element.YamlElement;
import dev.gurwi.confetti.element.YamlObject;
import dev.gurwi.confetti.element.YamlPrimitive;
import dev.gurwi.confetti.serde.YamlDeserializer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collection;

public class ReflectiveYamlDeserializer<T> implements YamlDeserializer<T> {

    private final Class<T> type;
    private final Type genericType;

    public ReflectiveYamlDeserializer(Class<T> type, Type genericType) {
        this.type = type;
        this.genericType = genericType;
    }

    public ReflectiveYamlDeserializer(Class<T> type) {
        this(type, type);
    }

    @Override
    public T deserialize(@NotNull YamlElement node) {
        return switch (node) {
            case YamlPrimitive primitive -> (T) primitive.getValue();

            case YamlArray array -> {
                if (type.isArray()) {
                    Class<?> componentType = type.getComponentType();
                    Object arr = Array.newInstance(componentType, array.size());

                    for (int i = 0; i < array.size(); i++) {
                        YamlElement elementNode = array.get(i);

                        Object value = new ReflectiveYamlDeserializer<>(componentType)
                                .deserialize(elementNode);

                        Array.set(arr, i, value);
                    }

                    yield (T) arr;
                }

                if (Collection.class.isAssignableFrom(type)) {
                    Class<?> elementType = Object.class;

                    if (genericType instanceof ParameterizedType parameterizedType) {
                        Type arg = parameterizedType.getActualTypeArguments()[0];
                        if (arg instanceof Class<?> clazz) {
                            elementType = clazz;
                        }
                    }

                    Collection<Object> collection = new ArrayList<>();
                    for (YamlElement elementNode : array) {
                        Object value = new ReflectiveYamlDeserializer<>(elementType, elementType)
                                .deserialize(elementNode);

                        collection.add(value);
                    }

                    yield (T) collection;
                }

                throw new IllegalArgumentException("Unsupported array/collection type: " + type.getName());
            }

            case YamlObject object -> {
                if (type.isRecord()) {
                    yield deserializeRecord(object);
                }

                yield deserializeClass(object);
            }

            default -> throw new IllegalStateException("Unexpected value: " + node);
        };
    }

    @Contract("null -> fail")
    private @NotNull T deserializeRecord(YamlElement node) {
        if (!(node instanceof YamlObject yamlObject)) {
            throw new IllegalArgumentException("Expected YamlObject to deserialize record " + type.getName());
        }

        try {
            RecordComponent[] components = type.getRecordComponents();
            Object[] args = new Object[components.length];
            Class<?>[] paramTypes = new Class<?>[components.length];

            for (int i = 0; i < components.length; i++) {
                RecordComponent comp = components[i];
                paramTypes[i] = comp.getType();

                Field backingField = type.getDeclaredField(comp.getName());
                Path pathAnno = backingField.getAnnotation(Path.class);

                String yamlKey = pathAnno != null ? pathAnno.value() : comp.getName();

                YamlElement valueNode = yamlObject.get(yamlKey);

                if (valueNode == null) {
                    throw new IllegalArgumentException("Missing key in YAML for: " + yamlKey);
                }

                Object value = new ReflectiveYamlDeserializer<>(comp.getType()).deserialize(valueNode);
                args[i] = value;
            }

            Constructor<T> ctor = type.getDeclaredConstructor(paramTypes);
            ctor.setAccessible(true);
            return ctor.newInstance(args);

        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize record: " + type.getName(), e);
        }
    }

    @Contract("null -> fail")
    private @NotNull T deserializeClass(YamlElement node) {
        if (!(node instanceof YamlObject yamlObject)) {
            throw new IllegalArgumentException("Expected YamlObject to deserialize class " + type.getName());
        }

        try {
            T instance = type.getDeclaredConstructor().newInstance();

            for (Field field : type.getDeclaredFields()) {
                if (!field.isAnnotationPresent(Path.class)) continue;

                Path pathAnno = field.getAnnotation(Path.class);
                String yamlKey = pathAnno.value();

                YamlElement valueNode = yamlObject.get(yamlKey);
                if (valueNode == null) {
                    throw new IllegalArgumentException("Missing key in YAML for: " + yamlKey);
                }

                boolean wasAccessible = field.canAccess(instance);
                if (!wasAccessible) field.setAccessible(true);

                Object value = new ReflectiveYamlDeserializer<>(field.getType()).deserialize(valueNode);
                field.set(instance, value);

                if (!wasAccessible) field.setAccessible(false);
            }

            return instance;

        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize class: " + type.getName(), e);
        }
    }

}
