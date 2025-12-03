package com.cqcloud.platform.utils;


import lombok.extern.slf4j.Slf4j;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 反射工具类
 * @author weimeilayer@gmail.com ✨
 * @date 💓💕 2025-12-01 16:18:08 🐬🐇 💓💕
 */
@Slf4j
public class ReflectionUtil {

    /**
     * 获取类的所有字段（包括父类）
     */
    public static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    /**
     * 获取带有指定注解的字段
     */
    public static List<Field> getFieldsWithAnnotation(Class<?> clazz,
                                                      Class<? extends Annotation> annotationClass) {
        List<Field> fields = new ArrayList<>();
        for (Field field : getAllFields(clazz)) {
            if (field.isAnnotationPresent(annotationClass)) {
                fields.add(field);
            }
        }
        return fields;
    }

    /**
     * 判断是否是基本类型或包装类
     */
    public static boolean isBasicType(Class<?> clazz) {
        return clazz.isPrimitive() ||
                clazz == String.class ||
                clazz == Integer.class ||
                clazz == Long.class ||
                clazz == Double.class ||
                clazz == Float.class ||
                clazz == Boolean.class ||
                clazz == Character.class ||
                clazz == Byte.class ||
                clazz == Short.class;
    }

    /**
     * 设置字段值
     */
    public static void setFieldValue(Object obj, String fieldName, Object value) {
        try {
            Field field = getField(obj.getClass(), fieldName);
            if (field != null) {
                field.setAccessible(true);
                field.set(obj, value);
            }
        } catch (Exception e) {
            log.error("设置字段值失败: {}.{}", obj.getClass().getName(), fieldName, e);
        }
    }

    /**
     * 获取字段值
     */
    public static Object getFieldValue(Object obj, String fieldName) {
        try {
            Field field = getField(obj.getClass(), fieldName);
            if (field != null) {
                field.setAccessible(true);
                return field.get(obj);
            }
        } catch (Exception e) {
            log.error("获取字段值失败: {}.{}", obj.getClass().getName(), fieldName, e);
        }
        return null;
    }

    /**
     * 获取字段（包括父类）
     */
    public static Field getField(Class<?> clazz, String fieldName) {
        while (clazz != null && clazz != Object.class) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

    /**
     * 判断字段是否被final修饰
     */
    public static boolean isFinalField(Field field) {
        return Modifier.isFinal(field.getModifiers());
    }

    /**
     * 判断字段是否被static修饰
     */
    public static boolean isStaticField(Field field) {
        return Modifier.isStatic(field.getModifiers());
    }

    /**
     * 判断字段是否可访问
     */
    public static boolean isAccessibleField(Field field) {
        return !isStaticField(field) && !isFinalField(field);
    }
}
