package com.library.db.sqlite;

import com.library.db.FLDbException;
import com.library.db.table.ColumnUtils;
import com.library.db.table.Foreign;
import com.library.db.table.Table;

import java.util.List;

/**
 * Created by chen_fulei on 2015/8/29.
 */
public class FLForeignLazyLoader<T> {

    private final Foreign foreignColumn;
    private Object columnValue;

    public FLForeignLazyLoader(Foreign foreignColumn, Object value) {
        this.foreignColumn = foreignColumn;
        this.columnValue = ColumnUtils.convert2DbColumnValueIfNeeded(value);
    }

    public List<T> getAllFromDb() throws FLDbException {
        List<T> entities = null;
        Table table = foreignColumn.getTable();
        if (table != null) {
            entities = table.db.findAll(
                    FLSelector.from(foreignColumn.getForeignEntityType()).
                            where(foreignColumn.getForeignColumnName(), "=", columnValue)
            );
        }
        return entities;
    }

    public T getFirstFromDb() throws FLDbException {
        T entity = null;
        Table table = foreignColumn.getTable();
        if (table != null) {
            entity = table.db.findFirst(
                    FLSelector.from(foreignColumn.getForeignEntityType()).
                            where(foreignColumn.getForeignColumnName(), "=", columnValue)
            );
        }
        return entity;
    }

    public void setColumnValue(Object value) {
        this.columnValue = ColumnUtils.convert2DbColumnValueIfNeeded(value);
    }

    public Object getColumnValue() {
        return columnValue;
    }
}
