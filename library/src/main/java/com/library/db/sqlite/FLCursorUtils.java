package com.library.db.sqlite;

import android.database.Cursor;

import com.library.db.FLDbUtils;
import com.library.db.table.Column;
import com.library.db.table.DbModel;
import com.library.db.table.Finder;
import com.library.db.table.Id;
import com.library.db.table.Table;
import com.library.utils.Debug;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chen_fulei on 2015/8/29.
 */
public class FLCursorUtils {
    public static <T> T getEntity(final FLDbUtils db, final Cursor cursor, Class<T> entityType, long findCacheSequence) {
        if (db == null || cursor == null) return null;

        EntityTempCache.setSeq(findCacheSequence);
        try {
            Table table = Table.get(db, entityType);
            Id id = table.id;
            String idColumnName = id.getColumnName();
            int idIndex = id.getIndex();
            if (idIndex < 0) {
                idIndex = cursor.getColumnIndex(idColumnName);
            }
            Object idValue = id.getColumnConverter().getFieldValue(cursor, idIndex);
            T entity = EntityTempCache.get(entityType, idValue);
            if (entity == null) {
                entity = entityType.newInstance();
                id.setValue2Entity(entity, cursor, idIndex);
                EntityTempCache.put(entityType, idValue, entity);
            } else {
                return entity;
            }
            int columnCount = cursor.getColumnCount();
            for (int i = 0; i < columnCount; i++) {
                String columnName = cursor.getColumnName(i);
                Column column = table.columnMap.get(columnName);
                if (column != null) {
                    column.setValue2Entity(entity, cursor, i);
                }
            }

            // init finder
            for (Finder finder : table.finderMap.values()) {
                finder.setValue2Entity(entity, null, 0);
            }
            return entity;
        } catch (Throwable e) {
            Debug.Log(e);
        }

        return null;
    }

    public static DbModel getDbModel(final Cursor cursor) {
        DbModel result = null;
        if (cursor != null) {
            result = new DbModel();
            int columnCount = cursor.getColumnCount();
            for (int i = 0; i < columnCount; i++) {
                result.add(cursor.getColumnName(i), cursor.getString(i));
            }
        }
        return result;
    }

    public static class FindCacheSequence {
        private FindCacheSequence() {
        }

        private static long seq = 0;
        private static final String FOREIGN_LAZY_LOADER_CLASS_NAME = FLForeignLazyLoader.class.getName();
        private static final String FINDER_LAZY_LOADER_CLASS_NAME = FLFinderLazyLoader.class.getName();

        public static long getSeq() {
            String findMethodCaller = Thread.currentThread().getStackTrace()[4].getClassName();
            if (!findMethodCaller.equals(FOREIGN_LAZY_LOADER_CLASS_NAME) && !findMethodCaller.equals(FINDER_LAZY_LOADER_CLASS_NAME)) {
                ++seq;
            }
            return seq;
        }
    }

    private static class EntityTempCache {
        private EntityTempCache() {
        }

        private static final ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<String, Object>();

        private static long seq = 0;

        public static <T> void put(Class<T> entityType, Object idValue, Object entity) {
            cache.put(entityType.getName() + "#" + idValue, entity);
        }

        @SuppressWarnings("unchecked")
        public static <T> T get(Class<T> entityType, Object idValue) {
            return (T) cache.get(entityType.getName() + "#" + idValue);
        }

        public static void setSeq(long seq) {
            if (EntityTempCache.seq != seq) {
                cache.clear();
                EntityTempCache.seq = seq;
            }
        }
    }
}
