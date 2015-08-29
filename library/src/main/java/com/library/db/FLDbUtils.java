package com.library.db;

import android.content.Context;
import android.text.TextUtils;

import com.library.db.sqlite.FLSelector;

import java.util.List;

/**
 * Created by chen_fulei on 2015/8/28.
 */
public class FLDbUtils {
    private DaoConfig daoConfig;

    @SuppressWarnings("unchecked")
    public <T> List<T> findAll(FLSelector selector) throws FLDbException {

        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> T findFirst(FLSelector selector) throws FLDbException {


        return null;
    }

    public void saveOrUpdate(Object entity) throws FLDbException {

    }

    public DaoConfig getDaoConfig() {

        return daoConfig;
    }

    public static class DaoConfig {
        private Context context;
        private String dbName = "xUtils.db"; // default db name
        private int dbVersion = 1;
        private DbUpgradeListener dbUpgradeListener;

        private String dbDir;

        public DaoConfig(Context context) {
            this.context = context.getApplicationContext();
        }

        public Context getContext() {
            return context;
        }

        public String getDbName() {
            return dbName;
        }

        public void setDbName(String dbName) {
            if (!TextUtils.isEmpty(dbName)) {
                this.dbName = dbName;
            }
        }

        public int getDbVersion() {
            return dbVersion;
        }

        public void setDbVersion(int dbVersion) {
            this.dbVersion = dbVersion;
        }

        public DbUpgradeListener getDbUpgradeListener() {
            return dbUpgradeListener;
        }

        public void setDbUpgradeListener(DbUpgradeListener dbUpgradeListener) {
            this.dbUpgradeListener = dbUpgradeListener;
        }

        public String getDbDir() {
            return dbDir;
        }

        /**
         * set database dir
         *
         * @param dbDir If dbDir is null or empty, use the app default db dir.
         */
        public void setDbDir(String dbDir) {
            this.dbDir = dbDir;
        }
    }

    public interface DbUpgradeListener {
        public void onUpgrade(FLDbUtils db, int oldVersion, int newVersion);
    }
}
