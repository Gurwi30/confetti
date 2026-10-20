package dev.gurwi.confetti.serde.internal.record;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class RecordSupport {

    private static final Method IS_RECORD;
    private static final Method GET_RECORD_COMPONENTS;
    private static final Method COMPONENT_NAME;
    private static final Method COMPONENT_TYPE;
    private static final Method COMPONENT_GENERIC_TYPE;
    private static final Method COMPONENT_ACCESSOR;

    private static final Map<Class<?>, List<RecordComponentInfo>> CACHE = new ConcurrentHashMap<>();

    static {
        Method isRecord;
        Method getRecordComponents;
        Method componentName;
        Method componentType;
        Method componentGenericType;
        Method componentAccessor = null;

        try {
            Class<?> componentClass = Class.forName("java.lang.reflect.RecordComponent");

            //noinspection JavaReflectionMemberAccess
            isRecord = Class.class.getMethod("isRecord");
            //noinspection JavaReflectionMemberAccess
            getRecordComponents = Class.class.getMethod("getRecordComponents");
            componentName = componentClass.getMethod("getName");
            componentType = componentClass.getMethod("getType");
            componentGenericType = componentClass.getMethod("getGenericType");
            componentAccessor = componentClass.getMethod("getAccessor");
        } catch (ReflectiveOperationException e) {
            isRecord = null;
            getRecordComponents = null;
            componentName = null;
            componentType = null;
            componentGenericType = null;
        }

        IS_RECORD = isRecord;
        GET_RECORD_COMPONENTS = getRecordComponents;
        COMPONENT_NAME = componentName;
        COMPONENT_TYPE = componentType;
        COMPONENT_GENERIC_TYPE = componentGenericType;
        COMPONENT_ACCESSOR = componentAccessor;
    }

    private RecordSupport() {
    }

    public static boolean isRecord(Class<?> type) {
        if (IS_RECORD == null) return false;

        try {
            return (Boolean) IS_RECORD.invoke(type);
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    public static List<RecordComponentInfo> components(Class<?> type) {
        if (GET_RECORD_COMPONENTS == null) return Collections.emptyList();

        List<RecordComponentInfo> cached = CACHE.get(type);
        if (cached != null) return cached;

        try {
            Object[] raw = (Object[]) GET_RECORD_COMPONENTS.invoke(type);
            List<RecordComponentInfo> components = new ArrayList<>(raw.length);

            for (Object component : raw) {
                components.add(new RecordComponentInfo(
                        (String) COMPONENT_NAME.invoke(component),
                        (Class<?>) COMPONENT_TYPE.invoke(component),
                        (Type) COMPONENT_GENERIC_TYPE.invoke(component),
                        (Method) COMPONENT_ACCESSOR.invoke(component)
                ));
            }

            List<RecordComponentInfo> result = Collections.unmodifiableList(components);
            CACHE.put(type, result);
            return result;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to read record components of " + type.getName(), e);
        }
    }

}