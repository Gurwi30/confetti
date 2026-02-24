package dev.gurwi.confetti.serde.internal;

import dev.gurwi.confetti.Confetti;
import dev.gurwi.confetti.annotation.Path;
import dev.gurwi.confetti.element.YamlArray;
import dev.gurwi.confetti.element.YamlElement;
import dev.gurwi.confetti.element.YamlObject;
import dev.gurwi.confetti.element.YamlPrimitive;
import dev.gurwi.confetti.serde.YamlDeserializationContext;
import dev.gurwi.confetti.serde.YamlDeserializer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;
import java.util.*;

public class ReflectiveYamlDeserializer<T> implements YamlDeserializer<T> {

    private final Confetti confetti;
    private final Class<T> type;
    private final Type genericType;

    public ReflectiveYamlDeserializer(Confetti confetti, Class<T> type, Type genericType) {
        this.confetti = confetti;
        this.type = type;
        this.genericType = genericType;
    }

    public ReflectiveYamlDeserializer(Confetti confetti, Class<T> type) {
        this(confetti, type, type);
    }

    @Override
    public T deserialize(@NotNull YamlElement node, YamlDeserializationContext ctx) {
        if (YamlElement.class.isAssignableFrom(type)) {
            //noinspection unchecked
            return (T) node;
        }

        return switch (node) {
            case YamlPrimitive primitive -> {
                if (type.isEnum()) {
                    String name = primitive.getValue().toString().toUpperCase();

                    try {
                        @SuppressWarnings({"unchecked", "rawtypes"})
                        Class<Enum> enumClass = (Class<Enum>) type;

                        @SuppressWarnings("unchecked")
                        T result = (T) Enum.valueOf(enumClass, name);

                        yield result;

                    } catch (IllegalArgumentException e) {
                        throw new RuntimeException("Invalid enum value '" + name + "' for enum " + type.getSimpleName(), e);
                    }
                }

                //noinspection unchecked
                yield (T) primitive.getValue();
            }

            case YamlArray array -> deserializeArray(array);

            case YamlObject object -> {
                if (type.isRecord()) {
                    yield deserializeRecord(object);
                }

                yield deserializeClass(object);
            }

            default -> throw new IllegalStateException("Unexpected value: " + node);
        };
    }

    private T deserializeArray(@NotNull YamlArray array) {
        if (type.isArray()) {
            Class<?> componentType = type.getComponentType();
            Object arr = Array.newInstance(componentType, array.size());

            for (int i = 0; i < array.size(); i++) {
                YamlElement elementNode = array.get(i);

                Object value = new ReflectiveYamlDeserializer<>(confetti, componentType)
                        .deserialize(elementNode, new YamlDeserializationContext(confetti));

                Array.set(arr, i, value);
            }

            //noinspection unchecked
            return (T) arr;
        }

        if (Collection.class.isAssignableFrom(type)) {
            Collection<Object> collection;

            if (type.isInterface()) {
                if (type == List.class) collection = new ArrayList<>();
                else if (type == Set.class) collection = new HashSet<>();
                else if (type == NavigableSet.class) collection = new TreeSet<>();
                else if (type == SortedSet.class) collection = new TreeSet<>();
                else if (type == Queue.class) collection = new LinkedList<>();
                else {
                    throw new IllegalArgumentException("Unsupported array/collection type: " + type.getName());
                }
            } else {
                try {
                    //noinspection unchecked
                    collection = (Collection<Object>) type.getDeclaredConstructor().newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {

                    collection = new ArrayList<>();
                }
            }

            Class<?> elementType = Object.class;

            if (genericType instanceof ParameterizedType parameterizedType) {
                Type arg = parameterizedType.getActualTypeArguments()[0];
                if (arg instanceof Class<?> clazz) {
                    elementType = clazz;
                }
            }

            for (YamlElement elementNode : array) {
                Object value = new ReflectiveYamlDeserializer<>(confetti, elementType)
                        .deserialize(elementNode, new YamlDeserializationContext(confetti));

                collection.add(value);
            }

            //noinspection ReassignedVariable,unchecked
            return (T) collection;
        }

        throw new IllegalArgumentException("Unsupported array/collection type: " + type.getName());
    }

    private @NotNull T deserializeRecord(YamlObject node) {
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

                YamlElement valueNode = node.get(yamlKey);

                Object value = null;
                if (valueNode == null && comp.getType().isPrimitive()) value = getPrimitiveDefault(comp.getType());
                else if (valueNode != null) {
                    value = new ReflectiveYamlDeserializer<>(confetti, comp.getType(), comp.getGenericType())
                            .deserialize(valueNode, new YamlDeserializationContext(confetti));
                }

                args[i] = value;
            }

            Constructor<T> ctor = type.getDeclaredConstructor(paramTypes);
            ctor.setAccessible(true);
            return ctor.newInstance(args);

        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize record " + node.getClass().getName() + " into " + type.getName(), e);
        }
    }

    private @NotNull T deserializeClass(YamlObject node) {
        try {
            T instance = type.getDeclaredConstructor().newInstance();

            for (Field field : type.getDeclaredFields()) {
                if (!field.isAnnotationPresent(Path.class)) continue;

                Path pathAnno = field.getAnnotation(Path.class);
                String yamlKey = pathAnno.value();

                YamlElement valueNode = node.get(yamlKey);

                boolean wasAccessible = field.canAccess(instance);
                if (!wasAccessible) field.setAccessible(true);

                Object value = null;
                if (valueNode == null && field.getType().isPrimitive()) value = getPrimitiveDefault(field.getType());
                else if (valueNode != null) {
                    value = new ReflectiveYamlDeserializer<>(confetti, field.getType(), field.getGenericType())
                            .deserialize(valueNode, new YamlDeserializationContext(confetti));
                }

                field.set(instance, value);

                if (!wasAccessible) field.setAccessible(false);
            }

            return instance;

        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize class " + node.getClass().getName() + " into " + type.getName(), e);
        }
    }

    @Contract(pure = true)
    private static @NotNull Object getPrimitiveDefault(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == char.class) return '\0';
        if (type == byte.class || type == short.class || type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0.0f;
        if (type == double.class) return 0.0d;

        throw new IllegalArgumentException("Unsupported primitive type: " + type);
    }

}
