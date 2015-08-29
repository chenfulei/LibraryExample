package com.library.db.converter;

import android.database.Cursor;
import android.text.TextUtils;

import com.library.db.sqlite.FLColumnDbType;

/**
 * Created by chen_fulei on 2015/8/29.
 */
public class FLShortColumnConverter implements FLColumnConverter<Short> {
    @Override
    public Short getFieldValue(final Cursor cursor, int index) {
        return cursor.isNull(index) ? null : cursor.getShort(index);
    }

    @Override
    public Short getFieldValue(String fieldStringValue) {
        if (TextUtils.isEmpty(fieldStringValue)) return null;
        return Short.valueOf(fieldStringValue);
    }

    @Override
    public Object fieldValue2ColumnValue(Short fieldValue) {
        return fieldValue;
    }

    @Override
    public FLColumnDbType getColumnDbType() {
        return FLColumnDbType.INTEGER;
    }
}
