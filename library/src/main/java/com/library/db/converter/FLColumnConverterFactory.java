package com.library.db.converter;

import com.library.db.sqlite.FLColumnDbType;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chen_fulei on 2015/8/29.
 */
public class FLColumnConverterFactory {

    public FLColumnConverterFactory(){
    }

    public static FLColumnConverter getColumnConverter(Class columnType) {
        if (columnType_columnConverter_map.containsKey(columnType.getName())) {
            return columnType_columnConverter_map.get(columnType.getName());
        } else if (FLColumnConverter.class.isAssignableFrom(columnType)) {
            try {
                FLColumnConverter columnConverter = (FLColumnConverter) columnType.newInstance();
                if (columnConverter != null) {
                    columnType_columnConverter_map.put(columnType.getName(), columnConverter);
                }
                return columnConverter;
            } catch (Throwable e) {
            }
        }
        return null;
    }

    public static FLColumnDbType getDbColumnType(Class columnType) {
        FLColumnConverter converter = getColumnConverter(columnType);
        if (converter != null) {
            return converter.getColumnDbType();
        }
        return FLColumnDbType.TEXT;
    }

    public static void registerColumnConverter(Class columnType, FLColumnConverter columnConverter) {
        columnType_columnConverter_map.put(columnType.getName(), columnConverter);
    }

    public static boolean isSupportColumnConverter(Class columnType) {
        if (columnType_columnConverter_map.containsKey(columnType.getName())) {
            return true;
        } else if (FLColumnConverter.class.isAssignableFrom(columnType)) {
            try {
                FLColumnConverter columnConverter = (FLColumnConverter) columnType.newInstance();
                if (columnConverter != null) {
                    columnType_columnConverter_map.put(columnType.getName(), columnConverter);
                }
                return columnConverter == null;
            } catch (Throwable e) {
            }
        }
        return false;
    }


    private static final ConcurrentHashMap<String, FLColumnConverter> columnType_columnConverter_map;
    static {
        columnType_columnConverter_map = new ConcurrentHashMap<String, FLColumnConverter>();

        FLBooleanColumnConverter booleanColumnConverter = new FLBooleanColumnConverter();
        columnType_columnConverter_map.put(boolean.class.getName(), booleanColumnConverter);
        columnType_columnConverter_map.put(Boolean.class.getName(), booleanColumnConverter);

        FLByteArrayColumnConverter byteArrayColumnConverter = new FLByteArrayColumnConverter();
        columnType_columnConverter_map.put(byte[].class.getName(), byteArrayColumnConverter);

        FLByteColumnConverter byteColumnConverter = new FLByteColumnConverter();
        columnType_columnConverter_map.put(byte.class.getName(), byteColumnConverter);
        columnType_columnConverter_map.put(Byte.class.getName(), byteColumnConverter);

        FLCharColumnConverter charColumnConverter = new FLCharColumnConverter();
        columnType_columnConverter_map.put(char.class.getName(), charColumnConverter);
        columnType_columnConverter_map.put(Character.class.getName(), charColumnConverter);

        FLDateColumnConverter dateColumnConverter = new FLDateColumnConverter();
        columnType_columnConverter_map.put(Date.class.getName(), dateColumnConverter);

        FLDoubleColumnConverter doubleColumnConverter = new FLDoubleColumnConverter();
        columnType_columnConverter_map.put(double.class.getName(), doubleColumnConverter);
        columnType_columnConverter_map.put(Double.class.getName(), doubleColumnConverter);

        FLFloatColumnConverter floatColumnConverter = new FLFloatColumnConverter();
        columnType_columnConverter_map.put(float.class.getName(), floatColumnConverter);
        columnType_columnConverter_map.put(Float.class.getName(), floatColumnConverter);

        FLIntegerColumnConverter integerColumnConverter = new FLIntegerColumnConverter();
        columnType_columnConverter_map.put(int.class.getName(), integerColumnConverter);
        columnType_columnConverter_map.put(Integer.class.getName(), integerColumnConverter);

        FLLongColumnConverter longColumnConverter = new FLLongColumnConverter();
        columnType_columnConverter_map.put(long.class.getName(), longColumnConverter);
        columnType_columnConverter_map.put(Long.class.getName(), longColumnConverter);

        FLShortColumnConverter shortColumnConverter = new FLShortColumnConverter();
        columnType_columnConverter_map.put(short.class.getName(), shortColumnConverter);
        columnType_columnConverter_map.put(Short.class.getName(), shortColumnConverter);

        FLSqlDateColumnConverter sqlDateColumnConverter = new FLSqlDateColumnConverter();
        columnType_columnConverter_map.put(java.sql.Date.class.getName(), sqlDateColumnConverter);

        FLStringColumnConverter stringColumnConverter = new FLStringColumnConverter();
        columnType_columnConverter_map.put(String.class.getName(), stringColumnConverter);
    }
}
