package com.library.db.sqlite;

import com.library.db.FLDbException;
import com.library.db.FLDbUtils;
import com.library.db.table.Column;
import com.library.db.table.ColumnUtils;
import com.library.db.table.Finder;
import com.library.db.table.Id;
import com.library.db.table.KeyValue;
import com.library.db.table.Table;
import com.library.db.table.TableUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Build "insert", "replace",，"update", "delete" and "create" sql.
 *
 * Created by chen_fulei on 2015/8/29.
 */
public class FLSqlInfoBuilder {

    private FLSqlInfoBuilder() {
    }

    //*********************************************** insert sql ***********************************************

    public static FLSqlInfo buildInsertSqlInfo(FLDbUtils db, Object entity) throws FLDbException {

        List<KeyValue> keyValueList = entity2KeyValueList(db, entity);
        if (keyValueList.size() == 0) return null;

        FLSqlInfo result = new FLSqlInfo();
        StringBuffer sqlBuffer = new StringBuffer();

        sqlBuffer.append("INSERT INTO ");
        sqlBuffer.append(TableUtils.getTableName(entity.getClass()));
        sqlBuffer.append(" (");
        for (KeyValue kv : keyValueList) {
            sqlBuffer.append(kv.key).append(",");
            result.addBindArgWithoutConverter(kv.value);
        }
        sqlBuffer.deleteCharAt(sqlBuffer.length() - 1);
        sqlBuffer.append(") VALUES (");

        int length = keyValueList.size();
        for (int i = 0; i < length; i++) {
            sqlBuffer.append("?,");
        }
        sqlBuffer.deleteCharAt(sqlBuffer.length() - 1);
        sqlBuffer.append(")");

        result.setSql(sqlBuffer.toString());

        return result;
    }

    //*********************************************** replace sql ***********************************************

    public static FLSqlInfo buildReplaceSqlInfo(FLDbUtils db, Object entity) throws FLDbException {

        List<KeyValue> keyValueList = entity2KeyValueList(db, entity);
        if (keyValueList.size() == 0) return null;

        FLSqlInfo result = new FLSqlInfo();
        StringBuffer sqlBuffer = new StringBuffer();

        sqlBuffer.append("REPLACE INTO ");
        sqlBuffer.append(TableUtils.getTableName(entity.getClass()));
        sqlBuffer.append(" (");
        for (KeyValue kv : keyValueList) {
            sqlBuffer.append(kv.key).append(",");
            result.addBindArgWithoutConverter(kv.value);
        }
        sqlBuffer.deleteCharAt(sqlBuffer.length() - 1);
        sqlBuffer.append(") VALUES (");

        int length = keyValueList.size();
        for (int i = 0; i < length; i++) {
            sqlBuffer.append("?,");
        }
        sqlBuffer.deleteCharAt(sqlBuffer.length() - 1);
        sqlBuffer.append(")");

        result.setSql(sqlBuffer.toString());

        return result;
    }

    //*********************************************** delete sql ***********************************************

    private static String buildDeleteSqlByTableName(String tableName) {
        return "DELETE FROM " + tableName;
    }

    public static FLSqlInfo buildDeleteSqlInfo(FLDbUtils db, Object entity) throws FLDbException {
        FLSqlInfo result = new FLSqlInfo();

        Class<?> entityType = entity.getClass();
        Table table = Table.get(db, entityType);
        Id id = table.id;
        Object idValue = id.getColumnValue(entity);

        if (idValue == null) {
            throw new FLDbException("this entity[" + entity.getClass() + "]'s id value is null");
        }
        StringBuilder sb = new StringBuilder(buildDeleteSqlByTableName(table.tableName));
        sb.append(" WHERE ").append(FLWhereBuilder.b(id.getColumnName(), "=", idValue));

        result.setSql(sb.toString());

        return result;
    }

    public static FLSqlInfo buildDeleteSqlInfo(FLDbUtils db, Class<?> entityType, Object idValue) throws FLDbException {
        FLSqlInfo result = new FLSqlInfo();

        Table table = Table.get(db, entityType);
        Id id = table.id;

        if (null == idValue) {
            throw new FLDbException("this entity[" + entityType + "]'s id value is null");
        }
        StringBuilder sb = new StringBuilder(buildDeleteSqlByTableName(table.tableName));
        sb.append(" WHERE ").append(FLWhereBuilder.b(id.getColumnName(), "=", idValue));

        result.setSql(sb.toString());

        return result;
    }

    public static FLSqlInfo buildDeleteSqlInfo(FLDbUtils db, Class<?> entityType, FLWhereBuilder whereBuilder) throws FLDbException {
        Table table = Table.get(db, entityType);
        StringBuilder sb = new StringBuilder(buildDeleteSqlByTableName(table.tableName));

        if (whereBuilder != null && whereBuilder.getWhereItemSize() > 0) {
            sb.append(" WHERE ").append(whereBuilder.toString());
        }

        return new FLSqlInfo(sb.toString());
    }

    //*********************************************** update sql ***********************************************

    public static FLSqlInfo buildUpdateSqlInfo(FLDbUtils db, Object entity, String... updateColumnNames) throws FLDbException {

        List<KeyValue> keyValueList = entity2KeyValueList(db, entity);
        if (keyValueList.size() == 0) return null;

        HashSet<String> updateColumnNameSet = null;
        if (updateColumnNames != null && updateColumnNames.length > 0) {
            updateColumnNameSet = new HashSet<String>(updateColumnNames.length);
            Collections.addAll(updateColumnNameSet, updateColumnNames);
        }

        Class<?> entityType = entity.getClass();
        Table table = Table.get(db, entityType);
        Id id = table.id;
        Object idValue = id.getColumnValue(entity);

        if (null == idValue) {
            throw new FLDbException("this entity[" + entity.getClass() + "]'s id value is null");
        }

        FLSqlInfo result = new FLSqlInfo();
        StringBuffer sqlBuffer = new StringBuffer("UPDATE ");
        sqlBuffer.append(table.tableName);
        sqlBuffer.append(" SET ");
        for (KeyValue kv : keyValueList) {
            if (updateColumnNameSet == null || updateColumnNameSet.contains(kv.key)) {
                sqlBuffer.append(kv.key).append("=?,");
                result.addBindArgWithoutConverter(kv.value);
            }
        }
        sqlBuffer.deleteCharAt(sqlBuffer.length() - 1);
        sqlBuffer.append(" WHERE ").append(FLWhereBuilder.b(id.getColumnName(), "=", idValue));

        result.setSql(sqlBuffer.toString());
        return result;
    }

    public static FLSqlInfo buildUpdateSqlInfo(FLDbUtils db, Object entity, FLWhereBuilder whereBuilder, String... updateColumnNames) throws FLDbException {

        List<KeyValue> keyValueList = entity2KeyValueList(db, entity);
        if (keyValueList.size() == 0) return null;

        HashSet<String> updateColumnNameSet = null;
        if (updateColumnNames != null && updateColumnNames.length > 0) {
            updateColumnNameSet = new HashSet<String>(updateColumnNames.length);
            Collections.addAll(updateColumnNameSet, updateColumnNames);
        }

        Class<?> entityType = entity.getClass();
        String tableName = TableUtils.getTableName(entityType);

        FLSqlInfo result = new FLSqlInfo();
        StringBuffer sqlBuffer = new StringBuffer("UPDATE ");
        sqlBuffer.append(tableName);
        sqlBuffer.append(" SET ");
        for (KeyValue kv : keyValueList) {
            if (updateColumnNameSet == null || updateColumnNameSet.contains(kv.key)) {
                sqlBuffer.append(kv.key).append("=?,");
                result.addBindArgWithoutConverter(kv.value);
            }
        }
        sqlBuffer.deleteCharAt(sqlBuffer.length() - 1);
        if (whereBuilder != null && whereBuilder.getWhereItemSize() > 0) {
            sqlBuffer.append(" WHERE ").append(whereBuilder.toString());
        }

        result.setSql(sqlBuffer.toString());
        return result;
    }

    //*********************************************** others ***********************************************

    public static FLSqlInfo buildCreateTableSqlInfo(FLDbUtils db, Class<?> entityType) throws FLDbException {
        Table table = Table.get(db, entityType);
        Id id = table.id;

        StringBuffer sqlBuffer = new StringBuffer();
        sqlBuffer.append("CREATE TABLE IF NOT EXISTS ");
        sqlBuffer.append(table.tableName);
        sqlBuffer.append(" ( ");

        if (id.isAutoIncrement()) {
            sqlBuffer.append("\"").append(id.getColumnName()).append("\"  ").append("INTEGER PRIMARY KEY AUTOINCREMENT,");
        } else {
            sqlBuffer.append("\"").append(id.getColumnName()).append("\"  ").append(id.getColumnDbType()).append(" PRIMARY KEY,");
        }

        Collection<Column> columns = table.columnMap.values();
        for (Column column : columns) {
            if (column instanceof Finder) {
                continue;
            }
            sqlBuffer.append("\"").append(column.getColumnName()).append("\"  ");
            sqlBuffer.append(column.getColumnDbType());
            if (ColumnUtils.isUnique(column.getColumnField())) {
                sqlBuffer.append(" UNIQUE");
            }
            if (ColumnUtils.isNotNull(column.getColumnField())) {
                sqlBuffer.append(" NOT NULL");
            }
            String check = ColumnUtils.getCheck(column.getColumnField());
            if (check != null) {
                sqlBuffer.append(" CHECK(").append(check).append(")");
            }
            sqlBuffer.append(",");
        }

        sqlBuffer.deleteCharAt(sqlBuffer.length() - 1);
        sqlBuffer.append(" )");
        return new FLSqlInfo(sqlBuffer.toString());
    }

    private static KeyValue column2KeyValue(Object entity, Column column) {
        KeyValue kv = null;
        String key = column.getColumnName();
        if (key != null) {
            Object value = column.getColumnValue(entity);
            value = value == null ? column.getDefaultValue() : value;
            kv = new KeyValue(key, value);
        }
        return kv;
    }

    public static List<KeyValue> entity2KeyValueList(FLDbUtils db, Object entity) {

        List<KeyValue> keyValueList = new ArrayList<>();

        Class<?> entityType = entity.getClass();
        Table table = Table.get(db, entityType);
        Id id = table.id;

        if (!id.isAutoIncrement()) {
            Object idValue = id.getColumnValue(entity);
            KeyValue kv = new KeyValue(id.getColumnName(), idValue);
            keyValueList.add(kv);
        }

        Collection<Column> columns = table.columnMap.values();
        for (Column column : columns) {
            if (column instanceof Finder) {
                continue;
            }
            KeyValue kv = column2KeyValue(entity, column);
            if (kv != null) {
                keyValueList.add(kv);
            }
        }

        return keyValueList;
    }
}
