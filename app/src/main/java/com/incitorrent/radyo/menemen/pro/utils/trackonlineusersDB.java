package com.incitorrent.radyo.menemen.pro.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Radyo Menemen Pro Created by lutfi on 30.08.2016.
 */
public class trackonlineusersDB extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "onlineusers.db";
    public static final String TABLE_NAME = "onlinelist";
    public static final String _NICK = "nick";
    public static final String _TOPIC = "topic";
    public static final String _TIME = "time";

    public trackonlineusersDB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE "+ TABLE_NAME + "("+
                _NICK + " TEXT PRIMARY KEY, " +
                _TOPIC + " INTEGER, " +
                _TIME + " INTEGER " +
                ");";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addToHistory(String nick, @Nullable String topic, long time){
        if(topic == null) topic = "0";
        ContentValues values = new ContentValues();
        values.put(_NICK, nick);
        values.put(_TOPIC, topic);
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

    /**
     * Get online users' count
     * @param topicid
     * @return
     */
    public int getOnlineUserCount(@Nullable String topicid){
        int count = 0;
        long period = System.currentTimeMillis() - (1000*60*3);
        try {
            SQLiteDatabase db = getReadableDatabase();
            String TOPIC = (topicid == null) ? "" : "AND " + _TOPIC + "='" + topicid + "'";
            Cursor c = db.rawQuery("SELECT count(*) from "+ TABLE_NAME +" WHERE " + _TIME + " >'" + period + "' " + TOPIC, null);
            c.moveToFirst();
            count = c.getInt(0);
            c.close();
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }
    public Cursor getOnlineUserList(String cur_user){
        SQLiteDatabase db = getReadableDatabase();
        long period = System.currentTimeMillis() - (1000*60*3);
        return db.query(TABLE_NAME,null,_TIME + " >'" + period + "' AND " + _NICK +" != '" + cur_user + "'",null,null,null,_TIME + " DESC");
    }
}
