package com.library.db.sqlite;

import com.library.db.FLDbException;
import com.library.db.table.ColumnUtils;
import com.library.db.table.Finder;
import com.library.db.table.Table;

import java.util.List;

/**
 * Created by chen_fulei on 2015/8/29.
 */
public class FLFinderLazyLoader<T> {

    private final Finder finderColumn;
    private final Object finderValue;

    public FLFinderLazyLoader(Finder finderColumn, Object value) {
        this.finderColumn = finderColumn;
        this.finderValue = ColumnUtils.convert2DbColumnValueIfNeeded(value);
    }

    public List<T> getAllFromDb() throws FLDbException {
        List<T> entities = null;
        Table table = finderColumn.getTable();
        if (table != null) {
            entities = table.db.findAll(
                    FLSelector.from(finderColumn.getTargetEntityType()).
                            where(finderColumn.getTargetColumnName(), "=", finderValue)
            );
        }
        return entities;
    }

    public T getFirstFromDb() throws FLDbException {
        T entity = null;
        Table table = finderColumn.getTable();
        if (table != null) {
            entity = table.db.findFirst(
                    FLSelector.from(finderColumn.getTargetEntityType()).
                            where(finderColumn.getTargetColumnName(), "=", finderValue)
            );
        }
        return entity;
    }

}
