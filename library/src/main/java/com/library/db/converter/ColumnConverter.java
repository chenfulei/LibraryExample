package com.library.db.converter;

import android.database.Cursor;

import com.library.db.sqlite.ColumnDbType;

/**
 * Created by chen_fulei on 2015/8/28.
 */
public interface ColumnConverter<T> {
    T getFieldValue(final Cursor cursor, int index);

    T getFieldValue(String fieldStringValue);

    Object fieldValue2ColumnValue(T fieldValue);

    ColumnDbType getColumnDbType();
}
