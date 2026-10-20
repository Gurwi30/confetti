package dev.gurwi.confetti.serde.internal;

import dev.gurwi.confetti.Confetti;
import dev.gurwi.confetti.annotation.Path;
import dev.gurwi.confetti.element.YamlArray;
import dev.gurwi.confetti.element.YamlElement;
import dev.gurwi.confetti.element.YamlObject;
import dev.gurwi.confetti.element.YamlPrimitive;
import dev.gurwi.confetti.serde.YamlDeserializationContext;
import dev.gurwi.confetti.serde.YamlDeserializer;
import dev.gurwi.confetti.serde.internal.factory.CollectionFactory;
import dev.gurwi.confetti.serde.internal.factory.PrimitiveTypeDefault;
import dev.gurwi.confetti.serde.internal.record.RecordComponentInfo;
import dev.gurwi.confetti.serde.internal.record.RecordSupport;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

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
    @SuppressWarnings("unchecked")
    public T deserialize(@NotNull YamlElement node, YamlDeserializationContext ctx) {
        if (YamlElement.class.isAssignableFrom(type)) {
            return (T) node;
        }

        if (node instanceof YamlPrimitive) {
            return deserializePrimitive((YamlPrimitive) node);
        }

        if (node instanceof YamlArray) {
            return deserializeArray((YamlArray) node);
        }

        if (node instanceof YamlObject) {
            YamlObject object = (YamlObject) node;

            if (RecordSupport.isRecord(type)) {
                return deserializeRecord(object);
            }

            return deserializeClass(object);
        }

        throw new IllegalStateException("Unexpected value: " + node);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private T deserializePrimitive(YamlPrimitive primitive) {
        if (type.isEnum()) {
            String name = primitive.getValue().toString().toUpperCase();

            try {
                return (T) Enum.valueOf((Class<Enum>) type, name);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid enum value '" + name + "' for enum " + type.getSimpleName(), e);
            }
        }

        return (T) primitive.getValue();
    }

    @SuppressWarnings("unchecked")
    private T deserializeArray(@NotNull YamlArray array) {
        if (type.isArray()) {
            Class<?> componentType = type.getComponentType();
            Object result = Array.newInstance(componentType, array.size());

            for (int i = 0; i < array.size(); i++) {
                Array.set(result, i, deserializeValue(componentType, componentType, array.get(i)));
            }

            return (T) result;
        }

        if (Collection.class.isAssignableFrom(type)) {
            Collection<Object> collection = CollectionFactory.create(type);
            Class<?> elementType = resolveElementType();

            for (YamlElement elementNode : array) {
                collection.add(deserializeValue(elementType, elementType, elementNode));
            }

            return (T) collection;
        }

        throw new IllegalArgumentException("Unsupported array/collection type: " + type.getName());
    }

    private @NotNull T deserializeRecord(YamlObject node) {
        try {
            List<RecordComponentInfo> components = RecordSupport.components(type);
            Object[] args = new Object[components.size()];
            Class<?>[] paramTypes = new Class<?>[components.size()];

            for (int i = 0; i < components.size(); i++) {
                RecordComponentInfo component = components.get(i);
                paramTypes[i] = component.getType();

                Field backingField = type.getDeclaredField(component.getName());
                Path pathAnnotation = backingField.getAnnotation(Path.class);
                String yamlKey = pathAnnotation != null ? pathAnnotation.value() : component.getName();

                args[i] = deserializeValue(component.getType(), component.getGenericType(), node.get(yamlKey));
            }

            Constructor<T> constructor = type.getDeclaredConstructor(paramTypes);
            constructor.setAccessible(true);
            return constructor.newInstance(args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize record " + node.getClass().getName() + " into " + type.getName(), e);
        }
    }

    private @NotNull T deserializeClass(YamlObject node) {
        try {
            T instance = type.getDeclaredConstructor().newInstance();

            for (Field field : type.getDeclaredFields()) {
                Path pathAnnotation = field.getAnnotation(Path.class);
                if (pathAnnotation == null) continue;

                Object value = deserializeValue(field.getType(), field.getGenericType(), node.get(pathAnnotation.value()));

                boolean wasAccessible = field.isAccessible();
                if (!wasAccessible) field.setAccessible(true);

                field.set(instance, value);

                if (!wasAccessible) field.setAccessible(false);
            }

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize class " + node.getClass().getName() + " into " + type.getName(), e);
        }
    }

    private @Nullable Object deserializeValue(Class<?> valueType, Type valueGenericType, YamlElement valueNode) {
        if (valueNode == null) {
            return valueType.isPrimitive() ? PrimitiveTypeDefault.of(valueType) : null;
        }

        return new ReflectiveYamlDeserializer<>(confetti, valueType, valueGenericType)
                .deserialize(valueNode, new YamlDeserializationContext(confetti));
    }

    private Class<?> resolveElementType() {
        if (genericType instanceof ParameterizedType) {
            Type argument = ((ParameterizedType) genericType).getActualTypeArguments()[0];

            if (argument instanceof Class<?>) {
                return (Class<?>) argument;
            }
        }

        return Object.class;
    }

}