package com.shunix.encryptor.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shunix
 * @since 2015/10/26
 */
public class DatabaseManager implements DatabaseInterface {

    private SQLiteOpenHelper mHelper;
    private SQLiteDatabase mWritableDatabase;
    private SQLiteDatabase mReadableDatabase;
    private static final String TAG = DatabaseManager.class.getName();

    public DatabaseManager(Context context) {
        mHelper = new SQLiteOpenHelperImpl(context, DB_NAME, null, DB_VERSION);
        mWritableDatabase = mHelper.getWritableDatabase();
        mReadableDatabase = mHelper.getReadableDatabase();
    }

    public void close() {
        if (mWritableDatabase != null && mWritableDatabase.isOpen()) {
            mWritableDatabase.close();
            mWritableDatabase = null;
        }
        if (mReadableDatabase != null && mReadableDatabase.isOpen()) {
            mReadableDatabase.close();
            mReadableDatabase = null;
        }
        if (mHelper != null) {
            mHelper.close();
        }
    }

    public boolean insertPassword(String name, String password, long timestamp) {
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(password) || timestamp == 0) {
            return false;
        }
        long id = -1;
        try {
            if (mWritableDatabase == null) {
                mWritableDatabase = mHelper.getWritableDatabase();
            }
            ContentValues cv = new ContentValues();
            cv.put(NAME_COL, name);
            cv.put(PWD_COL, password);
            cv.put(TS_COL, timestamp);
            if (findPwdRow(name) == -1) {
                // 新插入
                id = mWritableDatabase.insert(PWD_TABLE_NAME, null, cv);
            } else {
                // 已存在，更新
                id = mWritableDatabase.replace(PWD_TABLE_NAME, null, cv);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return id != -1;
    }

    public String queryPassword(String name) {
        if (TextUtils.isEmpty(name)) {
            return null;
        }
        if (mReadableDatabase == null) {
            mReadableDatabase = mHelper.getReadableDatabase();
        }
        try {
            // NAME_COL是UNIQUE的
            Cursor cursor = mReadableDatabase.query(PWD_TABLE_NAME, null, NAME_COL + "=?", new String[]{name}, null, null, null);
            cursor.moveToNext();
            if (cursor.getColumnIndex(PRIMARY_KEY) >= 0) {
                return cursor.getString(cursor.getColumnIndex(PWD_COL));
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

    public List<PasswordEntity> getAllPasswords() {
        List<PasswordEntity> entities = new ArrayList<PasswordEntity>();
        if (mReadableDatabase == null) {
            mReadableDatabase = mHelper.getReadableDatabase();
        }
        try {
            Cursor cursor = mReadableDatabase.query(PWD_TABLE_NAME, null, null, null, null, null, null);
            while(cursor.moveToNext()) {
                PasswordEntity entity = new PasswordEntity();
                entity.name = cursor.getString(cursor.getColumnIndex(NAME_COL));
                entity.password = cursor.getString(cursor.getColumnIndex(PWD_COL));
                entity.timestamp = cursor.getLong(cursor.getColumnIndex(TS_COL));
                entities.add(entity);
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return entities;
    }

    public boolean deletePassword(String name) {
        if (TextUtils.isEmpty(name)) {
            return false;
        }
        if (mWritableDatabase == null) {
            mWritableDatabase = mHelper.getWritableDatabase();
        }
        try {
            return mWritableDatabase.delete(PWD_TABLE_NAME, NAME_COL + "=?", new String[]{name}) != 0;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return false;
    }

    private long findPwdRow(String name) {
        long id = -1;
        if (mReadableDatabase == null) {
            mReadableDatabase = mHelper.getReadableDatabase();
        }
        try {
            // NAME_COL是UNIQUE的
            Cursor cursor = mReadableDatabase.query(PWD_TABLE_NAME, new String[] {PRIMARY_KEY}, NAME_COL + "=?", new String[]{name}, null, null, null);
            cursor.moveToNext();
            if (cursor.getColumnIndex(PRIMARY_KEY) >= 0) {
                id = cursor.getLong(cursor.getColumnIndex(PRIMARY_KEY));
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return id;
    }

    class SQLiteOpenHelperImpl extends SQLiteOpenHelper {

        public SQLiteOpenHelperImpl(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            initDatabase(sqLiteDatabase);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            // 第一个版本，无升级逻辑
        }

        protected void initDatabase(SQLiteDatabase db) {
            StringBuffer sb = new StringBuffer("CREATE TABLE IF NOT EXISTS ");
            sb.append(PWD_TABLE_NAME)
                    .append("(")
                    .append(PRIMARY_KEY)
                    .append(" INTEGER PRIMARY KEY AUTOINCREMENT")
                    .append(",")
                    .append(NAME_COL)
                    .append(" TEXT UNIQUE")
                    .append(",")
                    .append(PWD_COL)
                    .append(" TEXT")
                    .append(",")
                    .append(TS_COL)
                    .append(" INTEGER")
                    .append(")");
            try {
                db.execSQL(sb.toString());
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    public static class PasswordEntity {
        public String name;
        public String password;
        public long timestamp;
    }
}
