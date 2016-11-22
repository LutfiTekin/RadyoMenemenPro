package com.incitorrent.radyo.menemen.pro.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Radyo Menemen Pro Created by lutfi on 3.08.2016.
 */
public class topicDB extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "radyomenemenproTopic.db";
    public static final String TABLE_NAME = "topics";
    public static final String _TOPICID = "tid";
    public static final String _TOPICSTR = "topicstr";
    public static final String _CREATOR = "creator";
    public static final String _JOINED = "joined";
    public static final String _TITLE = "title";
    public static final String _DESCR = "description";
    public static final String _IMAGEURL = "image";

    public topicDB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE "+ TABLE_NAME + "("+
                _TOPICID + " INTEGER PRIMARY KEY," +
                _TOPICSTR + " TEXT, " +
                _CREATOR + " TEXT, " +
                _JOINED + " INTEGER, " +
                _TITLE + " TEXT, " +
                _DESCR + " TEXT, " +
                _IMAGEURL + " TEXT " +
                ");";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addtoHistory(TOPIC t){
        ContentValues values = new ContentValues();
        values.put(_TOPICID, t.get_TOPICID());
        values.put(_TOPICSTR, t.get_TOPICSTR());
        values.put(_CREATOR, t.get_CREATOR());
        values.put(_JOINED, t.get_JOINED());
        values.put(_TITLE, t.get_TITLE());
        values.put(_DESCR, t.get_DESCR());
        values.put(_IMAGEURL, t.get_IMAGEURL());
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }catch (Exception e){
            db.close();
            e.printStackTrace();
            return;
        }
        db.close();
    }
    public Cursor getHistory(){
        return getReadableDatabase().query(TABLE_NAME,null,null,null,null,null, _TOPICID +" DESC", null);
    }


    public boolean isHistoryExist(String capsurl,String nick){
        SQLiteDatabase db = getReadableDatabase();
        long rownum = DatabaseUtils.queryNumEntries(db,TABLE_NAME, _TOPICSTR + "='"+ capsurl + "' AND " + _CREATOR + "='" + nick + "'" );
        return rownum > 0;
    }



    public void deleteMSG(String msgid){
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.delete(TABLE_NAME, _TOPICID + "=\'"+ msgid + "\'",null);
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static class TOPIC {
        public String _TOPICID,_TOPICSTR,_CREATOR,_JOINED,_TITLE,_DESCR,_IMAGEURL;

        public TOPIC(String _TOPICID, String _TOPICSTR, String _CREATOR, String _JOINED, String _TITLE, String _DESCR, String _IMAGEURL) {
            this._TOPICID = _TOPICID;
            this._TOPICSTR = _TOPICSTR;
            this._CREATOR = _CREATOR;
            this._JOINED = _JOINED;
            this._TITLE = _TITLE;
            this._DESCR = _DESCR;
            this._IMAGEURL = _IMAGEURL;
        }

        public String get_TOPICID() {
            return _TOPICID;
        }

        public void set_TOPICID(String _TOPICID) {
            this._TOPICID = _TOPICID;
        }

        public String get_TOPICSTR() {
            return _TOPICSTR;
        }

        public void set_TOPICSTR(String _TOPICSTR) {
            this._TOPICSTR = _TOPICSTR;
        }

        public String get_CREATOR() {
            return _CREATOR;
        }

        public void set_CREATOR(String _CREATOR) {
            this._CREATOR = _CREATOR;
        }

        public String get_JOINED() {
            return _JOINED;
        }

        public void set_JOINED(String _JOINED) {
            this._JOINED = _JOINED;
        }

        public String get_TITLE() {
            return _TITLE;
        }

        public void set_TITLE(String _TITLE) {
            this._TITLE = _TITLE;
        }

        public String get_DESCR() {
            return _DESCR;
        }

        public void set_DESCR(String _DESCR) {
            this._DESCR = _DESCR;
        }

        public String get_IMAGEURL() {
            return _IMAGEURL;
        }

        public void set_IMAGEURL(String _IMAGEURL) {
            this._IMAGEURL = _IMAGEURL;
        }
    }


}