package com.library.db.converter;

import android.database.Cursor;

import com.library.db.sqlite.ColumnDbType;

/**
 * Created by chen_fulei on 2015/8/28.
 */
public class ByteArrayColumnConverter implements ColumnConverter<byte[]>{

    @Override
    public byte[] getFieldValue(final Cursor cursor, int index) {
        return cursor.isNull(index) ? null : cursor.getBlob(index);
    }

    @Override
    public byte[] getFieldValue(String fieldStringValue) {
        return null;
    }

    @Override
    public Object fieldValue2ColumnValue(byte[] fieldValue) {
        return fieldValue;
    }

    @Override
    public ColumnDbType getColumnDbType() {
        return ColumnDbType.BLOB;
    }

}
