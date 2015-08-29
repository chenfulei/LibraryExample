package com.library.db.converter;

import android.database.Cursor;

import com.library.db.sqlite.FLColumnDbType;

/**
 * Created by chen_fulei on 2015/8/29.
 */
public class FLStringColumnConverter implements FLColumnConverter<String> {
    @Override
    public String getFieldValue(final Cursor cursor, int index) {
        return cursor.isNull(index) ? null : cursor.getString(index);
    }

    @Override
    public String getFieldValue(String fieldStringValue) {
        return fieldStringValue;
    }

    @Override
    public Object fieldValue2ColumnValue(String fieldValue) {
        return fieldValue;
    }

    @Override
    public FLColumnDbType getColumnDbType() {
        return FLColumnDbType.TEXT;
    }
}
