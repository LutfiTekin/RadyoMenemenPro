package com.incitorrent.radyo.menemen.pro.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Radyo Menemen Pro Created by lutfi on 3.08.2016.
 */
public class capsDB extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "radyomenemenproCaps.db";
    public static final String TABLE_NAME = "capscomment";
    public static final String _MSGID = "msgid";
    public static final String _CAPSURL = "capsurl";
    public static final String _NICK = "nick";
    public static final String _POST = "post";
    public static final String _TIME = "time";

    public capsDB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE "+ TABLE_NAME + "("+
                _MSGID + " INTEGER PRIMARY KEY," +
                _CAPSURL + " TEXT, " +
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

    public void addtoHistory(CAPS caps){
        Log.v(DATABASE_NAME,caps.get_NICK() + " " + caps._POST);
        ContentValues values = new ContentValues();
        values.put(_MSGID, caps.get_MSGID());
        values.put(_CAPSURL, caps.get_CAPSURL());
        values.put(_NICK, caps.get_NICK());
        values.put(_POST, caps.get_POST());
        values.put(_TIME, caps.get_TIME());
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }catch (Exception e){
            e.printStackTrace();
            Log.v(DATABASE_NAME,e.toString());
        }
        db.close();
    }
    public Cursor getHistory(int limit, String capsurl){
        SQLiteDatabase db = getReadableDatabase();
        if(limit < 1) limit = 20;
        return db.query(TABLE_NAME,null,_CAPSURL + "='"+ capsurl + "'",null,null,null,_MSGID+" DESC", String.valueOf(limit));
    }
    public String getFirstComment(String capsurl){
        String firscomment = null;
        SQLiteDatabase db = getReadableDatabase();
        try {
            Cursor c = db.query(TABLE_NAME,null,_CAPSURL + "='"+ capsurl + "'",null,null,null,_MSGID+" ASC", "1");
            c.moveToFirst();
            firscomment = c.getString(c.getColumnIndex(_NICK)).toUpperCase() + ": " + c.getString(c.getColumnIndex(_POST));
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.close();
        return firscomment;
    }

    public Boolean isHistoryExist(String capsurl,String nick){
        SQLiteDatabase db = getReadableDatabase();
        long rownum = DatabaseUtils.queryNumEntries(db,TABLE_NAME, _CAPSURL  + "='"+ capsurl + "' AND " + _NICK + "='" + nick + "'" );
        return rownum > 0;
    }

    //TODO implement onScroll Load More
//    public Cursor getHistoryById(String msgid){
//        SQLiteDatabase db = getReadableDatabase();
//        return db.query(TABLE_NAME,null,_MSGID + ">=\'" + msgid + "\'",null,null,null,_MSGID+" DESC");
//    }
//
//    public Cursor getHistoryOnScroll(String msgid){
//        SQLiteDatabase db = getReadableDatabase();
//        return db.query(TABLE_NAME,null,_MSGID + "<\'" + msgid + "\'",null,null,null,_MSGID+" DESC","20");
//    }

    public void deleteMSG(String msgid){
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.delete(TABLE_NAME,_MSGID + "=\'"+ msgid + "\'",null);
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int commentCount(String capsurl){
        int count = 0;
        try {
            SQLiteDatabase db = getReadableDatabase();
            Cursor c = db.rawQuery("SELECT count(*) from "+ TABLE_NAME +" WHERE " + _CAPSURL + " ='" + capsurl + "' ", null);
            c.moveToFirst();
            count = c.getInt(0);
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    public static class CAPS {
        public String _MSGID, _CAPSURL, _NICK, _POST, _TIME;

        public CAPS(String _MSGID, String _CAPSURL, String _NICK, String _POST, String _TIME) {
            this._MSGID = _MSGID;
            this._CAPSURL = _CAPSURL;
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

        public String get_CAPSURL() {
            return _CAPSURL;
        }

        public void set_CAPSURL(String _CAPSURL) {
            this._CAPSURL = _CAPSURL;
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
