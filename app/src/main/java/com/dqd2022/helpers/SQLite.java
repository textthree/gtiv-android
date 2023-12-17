package com.dqd2022.helpers;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


public class SQLite {
    public SQLiteDatabase getDb() {
        return App.getDb();
    }

    Cursor cursor;
    static SQLite instance;

    public static SQLite getInstance() {
        if (instance == null) instance = new SQLite();
        return instance;
    }

    public SQLite() {
    }

    public SQLite(Cursor cursor) {
        this.cursor = cursor;
    }

    public int getInt(String fieldName) {
        int fieldIndex = cursor.getColumnIndex(fieldName);
        return cursor.getInt(fieldIndex);
    }

    public Long getLong(String fieldName) {
        int fieldIndex = cursor.getColumnIndex(fieldName);
        return cursor.getLong(fieldIndex);
    }

    public String getString(String fieldName) {
        int fieldIndex = cursor.getColumnIndex(fieldName);
        return cursor.getString(fieldIndex);
    }


    // getOne start
    public SQLite getOne(String sql) {
        Cursor cursor = App.getDb().rawQuery(sql, null);
        this.cursor = cursor;
        return this;
    }

    public String getFieldToString(String fieldName) {
        if (cursor.getCount() == 0) return "";
        cursor.moveToNext();
        int fieldIndex = cursor.getColumnIndex(fieldName);
        return cursor.getString(fieldIndex);
    }

    public int getFieldToInt(String fieldName) {
        if (cursor.getCount() == 0) return 0;
        cursor.moveToNext();
        int fieldIndex = cursor.getColumnIndex(fieldName);
        return cursor.getInt(fieldIndex);
    }

    public Long getFieldToLong(String fieldName) {
        if (cursor.getCount() == 0) return 0l;
        cursor.moveToNext();
        int fieldIndex = cursor.getColumnIndex(fieldName);
        return cursor.getLong(fieldIndex);
    }
    // getOne end

    // 静态方法 start
    public static String getStringFromCursor(Cursor cursor, String fieldName) {
        if (cursor == null) return "";
        int fieldIndex = cursor.getColumnIndex(fieldName);
        return cursor.getString(fieldIndex);
    }

    public static int getIntFromCursor(Cursor cursor, String fieldName) {
        int fieldIndex = cursor.getColumnIndex(fieldName);
        return cursor.getInt(fieldIndex);
    }

    public static Long getLongFromCursor(Cursor cursor, String fieldName) {
        int fieldIndex = cursor.getColumnIndex(fieldName);
        return cursor.getLong(fieldIndex);
    }

    // 静态方法 end


}

