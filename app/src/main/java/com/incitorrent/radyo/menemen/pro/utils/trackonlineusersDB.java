package com.incitorrent.radyo.menemen.pro.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Radyo Menemen Pro Created by lutfi on 30.08.2016.
 */
public class trackonlineusersDB extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "onlineusers.db";
    public static final String TABLE_NAME = "onlinelist";
    public static final String _NICK = "nick";
    public static final String _TIME = "time";

    public trackonlineusersDB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE "+ TABLE_NAME + "("+
                _NICK + " TEXT PRIMARY KEY, " +
                _TIME + " INTEGER " +
                ");";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addToHistory(String nick, long time){
        ContentValues values = new ContentValues();
        values.put(_NICK, nick);
        values.put(_TIME, time);
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }catch (Exception e){
            e.printStackTrace();
            Log.v(DATABASE_NAME,e.toString());
        }
        db.close();
    }

    public int getOnlineUserCount(){
        int count = 0;
        long period = System.currentTimeMillis() - (1000*60*3);
        try {
            SQLiteDatabase db = getReadableDatabase();
            Cursor c = db.rawQuery("SELECT count(*) from "+ TABLE_NAME +" WHERE " + _TIME + " >'" + period + "' ", null);
            c.moveToFirst();
            count = c.getInt(0);
            c.close();
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

}
