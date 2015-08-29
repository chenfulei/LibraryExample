package com.library.db.sqlite;

import android.text.TextUtils;

import com.library.db.converter.FLColumnConverterFactory;
import com.library.db.table.ColumnUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by chen_fulei on 2015/8/29.
 */
public class FLWhereBuilder {
    private final List<String> whereItems;

    private FLWhereBuilder() {
        this.whereItems = new ArrayList<String>();
    }

    /**
     * create new instance
     *
     * @return
     */
    public static FLWhereBuilder b() {
        return new FLWhereBuilder();
    }

    /**
     * create new instance
     *
     * @param columnName
     * @param op         operator: "=","<","LIKE","IN","BETWEEN"...
     * @param value
     * @return
     */
    public static FLWhereBuilder b(String columnName, String op, Object value) {
        FLWhereBuilder result = new FLWhereBuilder();
        result.appendCondition(null, columnName, op, value);
        return result;
    }

    /**
     * add AND condition
     *
     * @param columnName
     * @param op         operator: "=","<","LIKE","IN","BETWEEN"...
     * @param value
     * @return
     */
    public FLWhereBuilder and(String columnName, String op, Object value) {
        appendCondition(whereItems.size() == 0 ? null : "AND", columnName, op, value);
        return this;
    }

    /**
     * add OR condition
     *
     * @param columnName
     * @param op         operator: "=","<","LIKE","IN","BETWEEN"...
     * @param value
     * @return
     */
    public FLWhereBuilder or(String columnName, String op, Object value) {
        appendCondition(whereItems.size() == 0 ? null : "OR", columnName, op, value);
        return this;
    }

    public FLWhereBuilder expr(String expr) {
        whereItems.add(" " + expr);
        return this;
    }

    public FLWhereBuilder expr(String columnName, String op, Object value) {
        appendCondition(null, columnName, op, value);
        return this;
    }

    public int getWhereItemSize() {
        return whereItems.size();
    }

    @Override
    public String toString() {
        if (whereItems.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String item : whereItems) {
            sb.append(item);
        }
        return sb.toString();
    }

    private void appendCondition(String conj, String columnName, String op, Object value) {
        StringBuilder sqlSb = new StringBuilder();

        if (whereItems.size() > 0) {
            sqlSb.append(" ");
        }

        // append conj
        if (!TextUtils.isEmpty(conj)) {
            sqlSb.append(conj + " ");
        }

        // append columnName
        sqlSb.append(columnName);

        // convert op
        if ("!=".equals(op)) {
            op = "<>";
        } else if ("==".equals(op)) {
            op = "=";
        }

        // append op & value
        if (value == null) {
            if ("=".equals(op)) {
                sqlSb.append(" IS NULL");
            } else if ("<>".equals(op)) {
                sqlSb.append(" IS NOT NULL");
            } else {
                sqlSb.append(" " + op + " NULL");
            }
        } else {
            sqlSb.append(" " + op + " ");

            if ("IN".equalsIgnoreCase(op)) {
                Iterable<?> items = null;
                if (value instanceof Iterable) {
                    items = (Iterable<?>) value;
                } else if (value.getClass().isArray()) {
                    ArrayList<Object> arrayList = new ArrayList<Object>();
                    int len = Array.getLength(value);
                    for (int i = 0; i < len; i++) {
                        arrayList.add(Array.get(value, i));
                    }
                    items = arrayList;
                }
                if (items != null) {
                    StringBuffer stringBuffer = new StringBuffer("(");
                    for (Object item : items) {
                        Object itemColValue = ColumnUtils.convert2DbColumnValueIfNeeded(item);
                        if (FLColumnDbType.TEXT.equals(FLColumnConverterFactory.getDbColumnType(itemColValue.getClass()))) {
                            String valueStr = itemColValue.toString();
                            if (valueStr.indexOf('\'') != -1) { // convert single quotations
                                valueStr = valueStr.replace("'", "''");
                            }
                            stringBuffer.append("'" + valueStr + "'");
                        } else {
                            stringBuffer.append(itemColValue);
                        }
                        stringBuffer.append(",");
                    }
                    stringBuffer.deleteCharAt(stringBuffer.length() - 1);
                    stringBuffer.append(")");
                    sqlSb.append(stringBuffer.toString());
                } else {
                    throw new IllegalArgumentException("value must be an Array or an Iterable.");
                }
            } else if ("BETWEEN".equalsIgnoreCase(op)) {
                Iterable<?> items = null;
                if (value instanceof Iterable) {
                    items = (Iterable<?>) value;
                } else if (value.getClass().isArray()) {
                    ArrayList<Object> arrayList = new ArrayList<Object>();
                    int len = Array.getLength(value);
                    for (int i = 0; i < len; i++) {
                        arrayList.add(Array.get(value, i));
                    }
                    items = arrayList;
                }
                if (items != null) {
                    Iterator<?> iterator = items.iterator();
                    if (!iterator.hasNext()) throw new IllegalArgumentException("value must have tow items.");
                    Object start = iterator.next();
                    if (!iterator.hasNext()) throw new IllegalArgumentException("value must have tow items.");
                    Object end = iterator.next();

                    Object startColValue = ColumnUtils.convert2DbColumnValueIfNeeded(start);
                    Object endColValue = ColumnUtils.convert2DbColumnValueIfNeeded(end);

                    if (FLColumnDbType.TEXT.equals(FLColumnConverterFactory.getDbColumnType(startColValue.getClass()))) {
                        String startStr = startColValue.toString();
                        if (startStr.indexOf('\'') != -1) { // convert single quotations
                            startStr = startStr.replace("'", "''");
                        }
                        String endStr = endColValue.toString();
                        if (endStr.indexOf('\'') != -1) { // convert single quotations
                            endStr = endStr.replace("'", "''");
                        }
                        sqlSb.append("'" + startStr + "'");
                        sqlSb.append(" AND ");
                        sqlSb.append("'" + endStr + "'");
                    } else {
                        sqlSb.append(startColValue);
                        sqlSb.append(" AND ");
                        sqlSb.append(endColValue);
                    }
                } else {
                    throw new IllegalArgumentException("value must be an Array or an Iterable.");
                }
            } else {
                value = ColumnUtils.convert2DbColumnValueIfNeeded(value);
                if (FLColumnDbType.TEXT.equals(FLColumnConverterFactory.getDbColumnType(value.getClass()))) {
                    String valueStr = value.toString();
                    if (valueStr.indexOf('\'') != -1) { // convert single quotations
                        valueStr = valueStr.replace("'", "''");
                    }
                    sqlSb.append("'" + valueStr + "'");
                } else {
                    sqlSb.append(value);
                }
            }
        }
        whereItems.add(sqlSb.toString());
    }
}
