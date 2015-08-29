package com.library.db.table;

import com.library.db.annotation.FLNoAutoIncrement;
import com.library.utils.Debug;

import java.lang.reflect.Field;
import java.util.HashSet;

/**
 * Created by chen_fulei on 2015/8/29.
 */
public class Id extends Column{

    private String columnFieldClassName;
    private boolean isAutoIncrementChecked = false;
    private boolean isAutoIncrement = false;

    /* package */ Id(Class<?> entityType, Field field) {
        super(entityType, field);
        columnFieldClassName = columnField.getType().getName();
    }

    public boolean isAutoIncrement() {
        if (!isAutoIncrementChecked) {
            isAutoIncrementChecked = true;
            isAutoIncrement = columnField.getAnnotation(FLNoAutoIncrement.class) == null
                    && AUTO_INCREMENT_TYPES.contains(columnFieldClassName);
        }
        return isAutoIncrement;
    }

    public void setAutoIncrementId(Object entity, long value) {
        Object idValue = value;
        if (INTEGER_TYPES.contains(columnFieldClassName)) {
            idValue = (int) value;
        }

        if (setMethod != null) {
            try {
                setMethod.invoke(entity, idValue);
            } catch (Throwable e) {
                Debug.Log(e);
            }
        } else {
            try {
                this.columnField.setAccessible(true);
                this.columnField.set(entity, idValue);
            } catch (Throwable e) {
                Debug.Log(e);
            }
        }
    }

    @Override
    public Object getColumnValue(Object entity) {
        Object idValue = super.getColumnValue(entity);
        if (idValue != null) {
            if (this.isAutoIncrement() && (idValue.equals(0) || idValue.equals(0L))) {
                return null;
            } else {
                return idValue;
            }
        }
        return null;
    }

    private static final HashSet<String> INTEGER_TYPES = new HashSet<String>(2);
    private static final HashSet<String> AUTO_INCREMENT_TYPES = new HashSet<String>(4);

    static {
        INTEGER_TYPES.add(int.class.getName());
        INTEGER_TYPES.add(Integer.class.getName());

        AUTO_INCREMENT_TYPES.addAll(INTEGER_TYPES);
        AUTO_INCREMENT_TYPES.add(long.class.getName());
        AUTO_INCREMENT_TYPES.add(Long.class.getName());
    }

}
