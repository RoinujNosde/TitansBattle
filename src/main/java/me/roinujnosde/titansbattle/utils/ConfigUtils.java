package me.roinujnosde.titansbattle.utils;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigUtils {

    private ConfigUtils() {
    }

    public static void deserialize(@NotNull Object instance, @NotNull Map<String, Object> data) {
        for (Field declaredField : instance.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(declaredField.getModifiers())) {
                continue;
            }
            try {
                declaredField.setAccessible(true);
                declaredField.set(instance, data.get(ConfigUtils.getPath(declaredField)));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public static Map<String, Object> serialize(@NotNull Object instance) {
        HashMap<String, Object> data = new HashMap<>();
        for (Field declaredField : instance.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(declaredField.getModifiers())) {
                continue;
            }
            try {
                declaredField.setAccessible(true);
                data.put(ConfigUtils.getPath(declaredField), declaredField.get(instance));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    public static boolean setValue(@NotNull Object object, @NotNull String fieldName, @NotNull String value) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
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
        List<String> fields = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers()) ||
                    field.getType().isAssignableFrom(ConfigurationSerializable.class)) {
                continue;
            }
            fields.add(field.getName());
        }
        return fields;
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

}
