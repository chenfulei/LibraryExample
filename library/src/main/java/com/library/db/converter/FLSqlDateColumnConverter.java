package com.library.db.converter;

import android.database.Cursor;
import android.text.TextUtils;

import com.library.db.sqlite.FLColumnDbType;

import java.sql.Date;

/**
 *Created by chen_fulei on 2015/8/29.
 */
public class FLSqlDateColumnConverter implements FLColumnConverter<Date> {
    @Override
    public java.sql.Date getFieldValue(final Cursor cursor, int index) {
        return cursor.isNull(index) ? null : new java.sql.Date(cursor.getLong(index));
    }

    @Override
    public java.sql.Date getFieldValue(String fieldStringValue) {
        if (TextUtils.isEmpty(fieldStringValue)) return null;
        return new java.sql.Date(Long.valueOf(fieldStringValue));
    }

    @Override
    public Object fieldValue2ColumnValue(java.sql.Date fieldValue) {
        if (fieldValue == null) return null;
        return fieldValue.getTime();
    }

    @Override
    public FLColumnDbType getColumnDbType() {
        return FLColumnDbType.INTEGER;
    }
}
