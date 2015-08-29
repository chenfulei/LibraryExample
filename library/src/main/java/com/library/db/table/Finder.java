package com.library.db.table;

import android.database.Cursor;

import com.library.db.FLDbException;
import com.library.db.annotation.FLFinder;
import com.library.db.sqlite.FLColumnDbType;
import com.library.db.sqlite.FLFinderLazyLoader;
import com.library.utils.Debug;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by chen_fulei on 2015/8/29.
 */
public class Finder extends Column{

    private final String valueColumnName;
    private final String targetColumnName;

    /* package */ Finder(Class<?> entityType, Field field) {
        super(entityType, field);

        FLFinder finder =field.getAnnotation(FLFinder.class);
        this.valueColumnName = finder.valueColumn();
        this.targetColumnName = finder.targetColumn();
    }

    public Class<?> getTargetEntityType() {
        return ColumnUtils.getFinderTargetEntityType(this);
    }

    public String getTargetColumnName() {
        return targetColumnName;
    }

    @Override
    public void setValue2Entity(Object entity, Cursor cursor, int index) {
        Object value = null;
        Class<?> columnType = columnField.getType();
        Object finderValue = TableUtils.getColumnOrId(entity.getClass(), this.valueColumnName).getColumnValue(entity);
        if (columnType.equals(FLFinderLazyLoader.class)) {
            value = new FLFinderLazyLoader<>(this, finderValue);
        } else if (columnType.equals(List.class)) {
            try {
                value = new FLFinderLazyLoader<>(this, finderValue).getAllFromDb();
            } catch (FLDbException e) {
                Debug.Log(e);
            }
        } else {
            try {
                value = new FLFinderLazyLoader<>(this, finderValue).getFirstFromDb();
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

    @Override
    public Object getColumnValue(Object entity) {
        return null;
    }

    @Override
    public Object getDefaultValue() {
        return null;
    }

    @Override
    public FLColumnDbType getColumnDbType() {
        return FLColumnDbType.TEXT;
    }

}
