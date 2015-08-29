package com.library.db.sqlite;

/**
 * Created by chen_fulei on 2015/8/28.
 */
public enum FLColumnDbType {

    INTEGER("INTEGER"), REAL("REAL"), TEXT("TEXT"), BLOB("BLOB");

    private String value;

    FLColumnDbType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
