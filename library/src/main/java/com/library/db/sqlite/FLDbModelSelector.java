package com.library.db.sqlite;

import android.text.TextUtils;

/**
 * Created by chen_fulei on 2015/8/29.
 */
public class FLDbModelSelector {

    private String[] columnExpressions;
    private String groupByColumnName;
    private FLWhereBuilder having;

    private FLSelector selector;

    private FLDbModelSelector(Class<?> entityType) {
        selector = FLSelector.from(entityType);
    }

    protected FLDbModelSelector(FLSelector selector, String groupByColumnName) {
        this.selector = selector;
        this.groupByColumnName = groupByColumnName;
    }

    protected FLDbModelSelector(FLSelector selector, String[] columnExpressions) {
        this.selector = selector;
        this.columnExpressions = columnExpressions;
    }

    public static FLDbModelSelector from(Class<?> entityType) {
        return new FLDbModelSelector(entityType);
    }

    public FLDbModelSelector where(FLWhereBuilder whereBuilder) {
        selector.where(whereBuilder);
        return this;
    }

    public FLDbModelSelector where(String columnName, String op, Object value) {
        selector.where(columnName, op, value);
        return this;
    }

    public FLDbModelSelector and(String columnName, String op, Object value) {
        selector.and(columnName, op, value);
        return this;
    }

    public FLDbModelSelector and(FLWhereBuilder where) {
        selector.and(where);
        return this;
    }

    public FLDbModelSelector or(String columnName, String op, Object value) {
        selector.or(columnName, op, value);
        return this;
    }

    public FLDbModelSelector or(FLWhereBuilder where) {
        selector.or(where);
        return this;
    }

    public FLDbModelSelector expr(String expr) {
        selector.expr(expr);
        return this;
    }

    public FLDbModelSelector expr(String columnName, String op, Object value) {
        selector.expr(columnName, op, value);
        return this;
    }

    public FLDbModelSelector groupBy(String columnName) {
        this.groupByColumnName = columnName;
        return this;
    }

    public FLDbModelSelector having(FLWhereBuilder whereBuilder) {
        this.having = whereBuilder;
        return this;
    }

    public FLDbModelSelector select(String... columnExpressions) {
        this.columnExpressions = columnExpressions;
        return this;
    }

    public FLDbModelSelector orderBy(String columnName) {
        selector.orderBy(columnName);
        return this;
    }

    public FLDbModelSelector orderBy(String columnName, boolean desc) {
        selector.orderBy(columnName, desc);
        return this;
    }

    public FLDbModelSelector limit(int limit) {
        selector.limit(limit);
        return this;
    }

    public FLDbModelSelector offset(int offset) {
        selector.offset(offset);
        return this;
    }

    public Class<?> getEntityType() {
        return selector.getEntityType();
    }

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("SELECT ");
        if (columnExpressions != null && columnExpressions.length > 0) {
            for (int i = 0; i < columnExpressions.length; i++) {
                result.append(columnExpressions[i]);
                result.append(",");
            }
            result.deleteCharAt(result.length() - 1);
        } else {
            if (!TextUtils.isEmpty(groupByColumnName)) {
                result.append(groupByColumnName);
            } else {
                result.append("*");
            }
        }
        result.append(" FROM ").append(selector.tableName);
        if (selector.whereBuilder != null && selector.whereBuilder.getWhereItemSize() > 0) {
            result.append(" WHERE ").append(selector.whereBuilder.toString());
        }
        if (!TextUtils.isEmpty(groupByColumnName)) {
            result.append(" GROUP BY ").append(groupByColumnName);
            if (having != null && having.getWhereItemSize() > 0) {
                result.append(" HAVING ").append(having.toString());
            }
        }
        if (selector.orderByList != null) {
            for (int i = 0; i < selector.orderByList.size(); i++) {
                result.append(" ORDER BY ").append(selector.orderByList.get(i).toString());
            }
        }
        if (selector.limit > 0) {
            result.append(" LIMIT ").append(selector.limit);
            result.append(" OFFSET ").append(selector.offset);
        }
        return result.toString();
    }

}
