package com.library.db.table;

import android.database.Cursor;

import com.library.db.FLDbException;
import com.library.db.converter.FLColumnConverter;
import com.library.db.converter.FLColumnConverterFactory;
import com.library.db.sqlite.FLColumnDbType;
import com.library.db.sqlite.FLForeignLazyLoader;
import com.library.utils.Debug;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by chen_fulei on 2015/8/29.
 */
public class Foreign extends Column{

    private final String foreignColumnName;
    private final FLColumnConverter foreignColumnConverter;

    /* package */ Foreign(Class<?> entityType, Field field) {
        super(entityType, field);

        foreignColumnName = ColumnUtils.getForeignColumnNameByField(field);
        Class<?> foreignColumnType =
                TableUtils.getColumnOrId(getForeignEntityType(), foreignColumnName).columnField.getType();
        foreignColumnConverter = FLColumnConverterFactory.getColumnConverter(foreignColumnType);
    }

    public String getForeignColumnName() {
        return foreignColumnName;
    }

    public Class<?> getForeignEntityType() {
        return ColumnUtils.getForeignEntityType(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setValue2Entity(Object entity, Cursor cursor, int index) {
        Object fieldValue = foreignColumnConverter.getFieldValue(cursor, index);
        if (fieldValue == null) return;

        Object value = null;
        Class<?> columnType = columnField.getType();
        if (columnType.equals(FLForeignLazyLoader.class)) {
            value = new FLForeignLazyLoader<>(this, fieldValue);
        } else if (columnType.equals(List.class)) {
            try {
                value = new FLForeignLazyLoader<>(this, fieldValue).getAllFromDb();
            } catch (FLDbException e) {
                Debug.Log(e);
            }
        } else {
            try {
                value = new FLForeignLazyLoader<>(this, fieldValue).getFirstFromDb();
            } catch (FLDbException e) {
                Debug.Log(e);
            }

        }

        if (setMethod != null) {
            try {
                setMethod.invoke(entity, value);
            } catch (Throwable e) {
                Debug.Log(e);
            }
        } else {
            try {
                this.columnField.setAccessible(true);
                this.columnField.set(entity, value);
            } catch (Throwable e) {
                Debug.Log(e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getColumnValue(Object entity) {
        Object fieldValue = getFieldValue(entity);
        Object columnValue = null;

        if (fieldValue != null) {
            Class<?> columnType = columnField.getType();
            if (columnType.equals(FLForeignLazyLoader.class)) {
                columnValue = ((FLForeignLazyLoader) fieldValue).getColumnValue();
            } else if (columnType.equals(List.class)) {
                try {
                    List<?> foreignEntities = (List<?>) fieldValue;
                    if (foreignEntities.size() > 0) {

                        Class<?> foreignEntityType = ColumnUtils.getForeignEntityType(this);
                        Column column = TableUtils.getColumnOrId(foreignEntityType, foreignColumnName);
                        columnValue = column.getColumnValue(foreignEntities.get(0));

                        // 仅自动关联外键
                        Table table = this.getTable();
                        if (table != null && column instanceof Id) {
                            for (Object foreignObj : foreignEntities) {
                                Object idValue = column.getColumnValue(foreignObj);
                                if (idValue == null) {
                                    table.db.saveOrUpdate(foreignObj);
                                }
                            }
                        }

                        columnValue = column.getColumnValue(foreignEntities.get(0));
                    }
                } catch (Throwable e) {
                    Debug.Log(e);
                }
            } else {
                try {
                    Column column = TableUtils.getColumnOrId(columnType, foreignColumnName);
                    columnValue = column.getColumnValue(fieldValue);

                    Table table = this.getTable();
                    if (table != null && columnValue == null && column instanceof Id) {
                        table.db.saveOrUpdate(fieldValue);
                    }

                    columnValue = column.getColumnValue(fieldValue);
                } catch (Throwable e) {
                    Debug.Log(e);
                }
            }
        }

        return columnValue;
    }

    @Override
    public FLColumnDbType getColumnDbType() {
        return foreignColumnConverter.getColumnDbType();
    }

    /**
     * It always return null.
     *
     * @return null
     */
    @Override
    public Object getDefaultValue() {
        return null;
    }

}
