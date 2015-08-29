package com.library.db.sqlite;

import com.library.db.table.TableUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chen_fulei on 2015/8/29.
 */
public class FLSelector {
    protected Class<?> entityType;
    protected String tableName;

    protected FLWhereBuilder whereBuilder;
    protected List<OrderBy> orderByList;
    protected int limit = 0;
    protected int offset = 0;

    private FLSelector(Class<?> entityType) {
        this.entityType = entityType;
        this.tableName = TableUtils.getTableName(entityType);
    }

    public static FLSelector from(Class<?> entityType) {
        return new FLSelector(entityType);
    }

    public FLSelector where(FLWhereBuilder whereBuilder) {
        this.whereBuilder = whereBuilder;
        return this;
    }

    public FLSelector where(String columnName, String op, Object value) {
        this.whereBuilder = FLWhereBuilder.b(columnName, op, value);
        return this;
    }

    public FLSelector and(String columnName, String op, Object value) {
        this.whereBuilder.and(columnName, op, value);
        return this;
    }

    public FLSelector and(FLWhereBuilder where) {
        this.whereBuilder.expr("AND (" + where.toString() + ")");
        return this;
    }

    public FLSelector or(String columnName, String op, Object value) {
        this.whereBuilder.or(columnName, op, value);
        return this;
    }

    public FLSelector or(FLWhereBuilder where) {
        this.whereBuilder.expr("OR (" + where.toString() + ")");
        return this;
    }

    public FLSelector expr(String expr) {
        if (this.whereBuilder == null) {
            this.whereBuilder = FLWhereBuilder.b();
        }
        this.whereBuilder.expr(expr);
        return this;
    }

    public FLSelector expr(String columnName, String op, Object value) {
        if (this.whereBuilder == null) {
            this.whereBuilder = FLWhereBuilder.b();
        }
        this.whereBuilder.expr(columnName, op, value);
        return this;
    }

    public FLDbModelSelector groupBy(String columnName) {
        return new FLDbModelSelector(this, columnName);
    }

    public FLDbModelSelector select(String... columnExpressions) {
        return new FLDbModelSelector(this, columnExpressions);
    }

    public FLSelector orderBy(String columnName) {
        if (orderByList == null) {
            orderByList = new ArrayList<>(2);
        }
        orderByList.add(new OrderBy(columnName));
        return this;
    }

    public FLSelector orderBy(String columnName, boolean desc) {
        if (orderByList == null) {
            orderByList = new ArrayList<OrderBy>(2);
        }
        orderByList.add(new OrderBy(columnName, desc));
        return this;
    }

    public FLSelector limit(int limit) {
        this.limit = limit;
        return this;
    }

    public FLSelector offset(int offset) {
        this.offset = offset;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("SELECT ");
        result.append("*");
        result.append(" FROM ").append(tableName);
        if (whereBuilder != null && whereBuilder.getWhereItemSize() > 0) {
            result.append(" WHERE ").append(whereBuilder.toString());
        }
        if (orderByList != null) {
            for (int i = 0; i < orderByList.size(); i++) {
                result.append(" ORDER BY ").append(orderByList.get(i).toString());
            }
        }
        if (limit > 0) {
            result.append(" LIMIT ").append(limit);
            result.append(" OFFSET ").append(offset);
        }
        return result.toString();
    }

    public Class<?> getEntityType() {
        return entityType;
    }

    protected class OrderBy {
        private String columnName;
        private boolean desc;

        public OrderBy(String columnName) {
            this.columnName = columnName;
        }

        public OrderBy(String columnName, boolean desc) {
            this.columnName = columnName;
            this.desc = desc;
        }

        @Override
        public String toString() {
            return columnName + (desc ? " DESC" : " ASC");
        }
    }
}
