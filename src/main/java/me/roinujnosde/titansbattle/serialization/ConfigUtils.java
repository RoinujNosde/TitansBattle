package me.roinujnosde.titansbattle.serialization;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ConfigUtils {

    private ConfigUtils() {
    }

    public static void deserialize(@NotNull Object instance, @NotNull Map<String, Object> data) {
        for (Field field : getFields(instance.getClass())) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            try {
                field.setAccessible(true);
                Object value = data.get(getPath(field));
                if (value != null) {
                    field.set(instance, value);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public static Map<String, Object> serialize(@NotNull Object instance) {
        TreeMap<String, Object> data = new TreeMap<>();
        for (Field field : getFields(instance.getClass())) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            try {
                field.setAccessible(true);
                data.put(getPath(field), field.get(instance));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    public static boolean setValue(@NotNull Object object, @NotNull String fieldName, @NotNull String value) {
        try {
            Field field = getField(object.getClass(), fieldName);
            Class<?> fieldType = field.getType();
            Object valueOf;
            if (fieldType.isAssignableFrom(String.class)) {
                valueOf = value;
            } else {
                valueOf = fieldType.getMethod("valueOf", String.class).invoke(null, value);
            }
            field.setAccessible(true);
            field.set(object, valueOf);
            return true;
        } catch (NoSuchMethodException | InvocationTargetException | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<String> getEditableFields(@NotNull Class<?> clazz) {
        return getFields(clazz).stream().filter(ConfigUtils::isEditable).map(Field::getName)
                .collect(Collectors.toList());
    }

    public static String getPath(Field field) {
        Path path = field.getAnnotation(Path.class);
        if (path != null) {
            return path.value();
        }
        String name = field.getName();
        for (int i = 0; i < name.toCharArray().length; i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c)) {
                name = name.replaceFirst(String.valueOf(c), "_" + Character.toLowerCase(c));
            }
        }
        return name;
    }

    private static List<Field> getFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (Modifier.isTransient(field.getModifiers())) {
                    continue;
                }
                fields.add(field);
            }
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    private static Field getField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        for (Field field : getFields(clazz)) {
            if (field.getName().equals(fieldName)) {
                return field;
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    private static boolean isEditable(Field field) {
        if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {
            return false;
        }
        try {
            field.getType().getMethod("valueOf", String.class);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

}
