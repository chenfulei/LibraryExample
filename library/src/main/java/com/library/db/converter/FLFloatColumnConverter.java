package com.library.db.converter;

import android.database.Cursor;
import android.text.TextUtils;

import com.library.db.sqlite.FLColumnDbType;

/**
 *
 *Created by chen_fulei on 2015/8/29.
 */
public class FLFloatColumnConverter implements FLColumnConverter<Float> {
    @Override
    public Float getFieldValue(final Cursor cursor, int index) {
        return cursor.isNull(index) ? null : cursor.getFloat(index);
    }

    @Override
    public Float getFieldValue(String fieldStringValue) {
        if (TextUtils.isEmpty(fieldStringValue)) return null;
        return Float.valueOf(fieldStringValue);
    }

    @Override
    public Object fieldValue2ColumnValue(Float fieldValue) {
        return fieldValue;
    }

    @Override
    public FLColumnDbType getColumnDbType() {
        return FLColumnDbType.REAL;
    }
}
