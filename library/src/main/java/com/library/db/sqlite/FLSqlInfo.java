package com.library.db.sqlite;

import com.library.db.table.ColumnUtils;

import java.util.LinkedList;

/**
 * Created by chen_fulei on 2015/8/29.
 */
public class FLSqlInfo {

    private String sql;
    private LinkedList<Object> bindArgs;

    public FLSqlInfo() {
    }

    public FLSqlInfo(String sql) {
        this.sql = sql;
    }

    public FLSqlInfo(String sql, Object... bindArgs) {
        this.sql = sql;
        addBindArgs(bindArgs);
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public LinkedList<Object> getBindArgs() {
        return bindArgs;
    }

    public Object[] getBindArgsAsArray() {
        if (bindArgs != null) {
            return bindArgs.toArray();
        }
        return null;
    }

    public String[] getBindArgsAsStrArray() {
        if (bindArgs != null) {
            String[] strings = new String[bindArgs.size()];
            for (int i = 0; i < bindArgs.size(); i++) {
                Object value = bindArgs.get(i);
                strings[i] = value == null ? null : value.toString();
            }
            return strings;
        }
        return null;
    }

    public void addBindArg(Object arg) {
        if (bindArgs == null) {
            bindArgs = new LinkedList<Object>();
        }

        bindArgs.add(ColumnUtils.convert2DbColumnValueIfNeeded(arg));
    }

    /* package */ void addBindArgWithoutConverter(Object arg) {
        if (bindArgs == null) {
            bindArgs = new LinkedList<Object>();
        }

        bindArgs.add(arg);
    }

    public void addBindArgs(Object... bindArgs) {
        if (bindArgs != null) {
            for (Object arg : bindArgs) {
                addBindArg(arg);
            }
        }
    }

}
