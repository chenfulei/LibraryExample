package com.library.db.table;

import android.text.TextUtils;

import com.library.db.annotation.FLCheck;
import com.library.db.annotation.FLColumn;
import com.library.db.annotation.FLFinder;
import com.library.db.annotation.FLForeign;
import com.library.db.annotation.FLId;
import com.library.db.annotation.FLNotNull;
import com.library.db.annotation.FLTransient;
import com.library.db.annotation.FLUnique;
import com.library.db.converter.FLColumnConverter;
import com.library.db.converter.FLColumnConverterFactory;
import com.library.db.sqlite.FLFinderLazyLoader;
import com.library.db.sqlite.FLForeignLazyLoader;
import com.library.utils.Debug;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.HashSet;
import java.util.List;

/**
 * Created by chen_fulei on 2015/8/29.
 */
public class ColumnUtils {

    private ColumnUtils() {
    }

    private static final HashSet<String> DB_PRIMITIVE_TYPES = new HashSet<String>(14);

    static {
        DB_PRIMITIVE_TYPES.add(int.class.getName());
        DB_PRIMITIVE_TYPES.add(long.class.getName());
        DB_PRIMITIVE_TYPES.add(short.class.getName());
        DB_PRIMITIVE_TYPES.add(byte.class.getName());
        DB_PRIMITIVE_TYPES.add(float.class.getName());
        DB_PRIMITIVE_TYPES.add(double.class.getName());

        DB_PRIMITIVE_TYPES.add(Integer.class.getName());
        DB_PRIMITIVE_TYPES.add(Long.class.getName());
        DB_PRIMITIVE_TYPES.add(Short.class.getName());
        DB_PRIMITIVE_TYPES.add(Byte.class.getName());
        DB_PRIMITIVE_TYPES.add(Float.class.getName());
        DB_PRIMITIVE_TYPES.add(Double.class.getName());
        DB_PRIMITIVE_TYPES.add(String.class.getName());
        DB_PRIMITIVE_TYPES.add(byte[].class.getName());
    }

    public static boolean isDbPrimitiveType(Class<?> fieldType) {
        return DB_PRIMITIVE_TYPES.contains(fieldType.getName());
    }

    public static Method getColumnGetMethod(Class<?> entityType, Field field) {
        String fieldName = field.getName();
        Method getMethod = null;
        if (field.getType() == boolean.class) {
            getMethod = getBooleanColumnGetMethod(entityType, fieldName);
        }
        if (getMethod == null) {
            String methodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            try {
                getMethod = entityType.getDeclaredMethod(methodName);
            } catch (NoSuchMethodException e) {
                Debug.Log(e);
            }
        }

        if (getMethod == null && !Object.class.equals(entityType.getSuperclass())) {
            return getColumnGetMethod(entityType.getSuperclass(), field);
        }
        return getMethod;
    }

    public static Method getColumnSetMethod(Class<?> entityType, Field field) {
        String fieldName = field.getName();
        Method setMethod = null;
        if (field.getType() == boolean.class) {
            setMethod = getBooleanColumnSetMethod(entityType, field);
        }
        if (setMethod == null) {
            String methodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            try {
                setMethod = entityType.getDeclaredMethod(methodName, field.getType());
            } catch (NoSuchMethodException e) {
                Debug.Log(e);
            }
        }

        if (setMethod == null && !Object.class.equals(entityType.getSuperclass())) {
            return getColumnSetMethod(entityType.getSuperclass(), field);
        }
        return setMethod;
    }


    public static String getColumnNameByField(Field field) {
        FLColumn column = field.getAnnotation(FLColumn.class);
        if (column != null && !TextUtils.isEmpty(column.column())) {
            return column.column();
        }

        FLId id = field.getAnnotation(FLId.class);
        if (id != null && !TextUtils.isEmpty(id.column())) {
            return id.column();
        }

        FLForeign foreign = field.getAnnotation(FLForeign.class);
        if (foreign != null && !TextUtils.isEmpty(foreign.column())) {
            return foreign.column();
        }

        FLForeign finder = field.getAnnotation(FLForeign.class);
        if (finder != null) {
            return field.getName();
        }

        return field.getName();
    }

    public static String getForeignColumnNameByField(Field field) {

        FLForeign foreign = field.getAnnotation(FLForeign.class);
        if (foreign != null) {
            return foreign.foreign();
        }

        return field.getName();
    }

    public static String getColumnDefaultValue(Field field) {
        FLColumn column = field.getAnnotation(FLColumn.class);
        if (column != null && !TextUtils.isEmpty(column.defaultValue())) {
            return column.defaultValue();
        }
        return null;
    }

    public static boolean isTransient(Field field) {
        return field.getAnnotation(FLTransient.class) != null;
    }

    public static boolean isForeign(Field field) {
        return field.getAnnotation(FLForeign.class) != null;
    }

    public static boolean isFinder(Field field) {
        return field.getAnnotation(FLFinder.class) != null;
    }

    public static boolean isUnique(Field field) {
        return field.getAnnotation(FLUnique.class) != null;
    }

    public static boolean isNotNull(Field field) {
        return field.getAnnotation(FLNotNull.class) != null;
    }

    /**
     * @param field
     * @return check.value or null
     */
    public static String getCheck(Field field) {
        FLCheck check = field.getAnnotation(FLCheck.class);
        if (check != null) {
            return check.value();
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static Class<?> getForeignEntityType(Foreign foreignColumn) {
        Class<?> result = foreignColumn.getColumnField().getType();
        if (result.equals(FLForeignLazyLoader.class) || result.equals(List.class)) {
            result = (Class<?>) ((ParameterizedType) foreignColumn.getColumnField().getGenericType()).getActualTypeArguments()[0];
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static Class<?> getFinderTargetEntityType(Finder finderColumn) {
        Class<?> result = finderColumn.getColumnField().getType();
        if (result.equals(FLFinderLazyLoader.class) || result.equals(List.class)) {
            result = (Class<?>) ((ParameterizedType) finderColumn.getColumnField().getGenericType()).getActualTypeArguments()[0];
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static Object convert2DbColumnValueIfNeeded(final Object value) {
        Object result = value;
        if (value != null) {
            Class<?> valueType = value.getClass();
            if (!isDbPrimitiveType(valueType)) {
                FLColumnConverter converter = FLColumnConverterFactory.getColumnConverter(valueType);
                if (converter != null) {
                    result = converter.fieldValue2ColumnValue(value);
                } else {
                    result = value;
                }
            }
        }
        return result;
    }

    private static boolean isStartWithIs(final String fieldName) {
        return fieldName != null && fieldName.startsWith("is");
    }

    private static Method getBooleanColumnGetMethod(Class<?> entityType, final String fieldName) {
        String methodName = "is" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        if (isStartWithIs(fieldName)) {
            methodName = fieldName;
        }
        try {
            return entityType.getDeclaredMethod(methodName);
        } catch (NoSuchMethodException e) {
            Debug.LogE(methodName + " not exist");
        }
        return null;
    }

    private static Method getBooleanColumnSetMethod(Class<?> entityType, Field field) {
        String fieldName = field.getName();
        String methodName = null;
        if (isStartWithIs(field.getName())) {
            methodName = "set" + fieldName.substring(2, 3).toUpperCase() + fieldName.substring(3);
        } else {
            methodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        }
        try {
            return entityType.getDeclaredMethod(methodName, field.getType());
        } catch (NoSuchMethodException e) {
            Debug.LogE(methodName + " not exist");
        }
        return null;
    }


}
