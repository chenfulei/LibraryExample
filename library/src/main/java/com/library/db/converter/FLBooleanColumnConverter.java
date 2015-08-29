package com.library.db.converter;

import android.database.Cursor;
import android.text.TextUtils;

import com.library.db.sqlite.FLColumnDbType;

/**
 * Created by chen_fulei on 2015/8/28.
 */
public class FLBooleanColumnConverter implements FLColumnConverter<Boolean> {

    @Override
    public Boolean getFieldValue(final Cursor cursor, int index) {
        return cursor.isNull(index) ? null : cursor.getInt(index) == 1;
    }

    @Override
    public Boolean getFieldValue(String fieldStringValue) {
        if (TextUtils.isEmpty(fieldStringValue)) return null;
        return fieldStringValue.length() == 1 ? "1".equals(fieldStringValue) : Boolean.valueOf(fieldStringValue);
    }

    @Override
    public Object fieldValue2ColumnValue(Boolean fieldValue) {
        if (fieldValue == null) return null;
        return fieldValue ? 1 : 0;
    }

    @Override
    public FLColumnDbType getColumnDbType() {
        return FLColumnDbType.INTEGER;
    }

}
