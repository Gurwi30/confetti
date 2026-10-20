package dev.gurwi.confetti.serde.internal.factory;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public interface CollectionFactory {

    @SuppressWarnings("unchecked")
    static @NotNull Collection<Object> create(@NotNull Class<?> type) {
        if (type.isInterface()) {
            if (type == List.class) return new ArrayList<>();
            if (type == Set.class) return new HashSet<>();
            if (type == NavigableSet.class) return new TreeSet<>();
            if (type == SortedSet.class) return new TreeSet<>();
            if (type == Queue.class) return new LinkedList<>();

            throw new IllegalArgumentException("Unsupported array/collection type: " + type.getName());
        }

        try {
            return (Collection<Object>) type.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            return new ArrayList<>();
        }
    }

}
