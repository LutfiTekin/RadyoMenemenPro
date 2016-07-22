package com.incitorrent.radyo.menemen.pro.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Radyo Menemen Pro Created by lutfi on 21.07.2016.
 */
public class chatDB extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "radyomenemenproChat.db";
    public static final String TABLE_NAME = "sohbet";
    public static final String _MSGID = "msgid";
    public static final String _NICK = "nick";
    public static final String _POST = "post";
    public static final String _TIME = "time";

    public chatDB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE "+ TABLE_NAME + "("+
                _MSGID + " INTEGER PRIMARY KEY," +
                _NICK + " TEXT, " +
                _POST + " TEXT, " +
                _TIME + " TEXT " +
                ");";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addtoHistory(CHAT chat){
        Log.v(DATABASE_NAME,chat.get_NICK() + " " + chat._POST);
        ContentValues values = new ContentValues();
        values.put(_MSGID, chat.get_MSGID());
        values.put(_NICK, chat.get_NICK());
        values.put(_POST, chat.get_POST());
        values.put(_TIME, chat.get_TIME());
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }catch (Exception e){
            e.printStackTrace();
            Log.v(DATABASE_NAME,e.toString());
        }
        db.close();
    }
    public Cursor getHistory(int limit){
        SQLiteDatabase db = getReadableDatabase();
        if(limit < 20) limit = 20;
        Cursor c = db.query(TABLE_NAME,null,null,null,null,null,_MSGID+" DESC", String.valueOf(limit));
        db.close();
        return c;
    }

    public Cursor getHistoryById(String msgid){
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_NAME,null,_MSGID + ">=\'" + "\'",null,null,null,_MSGID+" DESC");
        db.close();
        return c;
    }

    public void deleteMSG(String msgid){
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.delete(TABLE_NAME,_MSGID + "=\'"+ msgid + "\'",null);
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class CHAT {
        public String _MSGID, _NICK, _POST, _TIME;

        public CHAT(String _MSGID, String _NICK, String _POST, String _TIME) {
            this._MSGID = _MSGID;
            this._NICK = _NICK;
            this._POST = _POST;
            this._TIME = _TIME;
        }

        public String get_MSGID() {
            return _MSGID;
        }

        public void set_MSGID(String _MSGID) {
            this._MSGID = _MSGID;
        }

        public String get_NICK() {
            return _NICK;
        }

        public void set_NICK(String _NICK) {
            this._NICK = _NICK;
        }

        public String get_POST() {
            return _POST;
        }

        public void set_POST(String _POST) {
            this._POST = _POST;
        }

        public String get_TIME() {
            return _TIME;
        }

        public void set_TIME(String _TIME) {
            this._TIME = _TIME;
        }
    }


}
