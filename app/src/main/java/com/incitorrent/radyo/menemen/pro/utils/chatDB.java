package com.incitorrent.radyo.menemen.pro.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
        ContentValues values = new ContentValues();
        values.put(_MSGID, chat.get_MSGID());
        values.put(_NICK, chat.get_NICK());
        values.put(_POST, chat.get_POST());
        values.put(_TIME, chat.get_TIME());
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        }catch (Exception e){
            e.printStackTrace();
        }
        db.close();
    }
    public Cursor getHistory(int limit){
        SQLiteDatabase db = getReadableDatabase();
        if(limit < 20) limit = 20;
        return db.query(TABLE_NAME,null,null,null,null,null,_MSGID+" DESC", String.valueOf(limit));
    }

    public Cursor getHistoryById(String msgid){
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_NAME,null,_MSGID + ">=\'" + msgid + "\'",null,null,null,_MSGID+" DESC");
    }

    public Cursor getHistoryOnScroll(String msgid){
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_NAME,null,_MSGID + "<\'" + msgid + "\'",null,null,null,_MSGID+" DESC","40");
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

    public Cursor getCapsGallery(int limit){
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_NAME,null,_POST + " LIKE '%caps.radyomenemen.com/images%'",null,null,null,_MSGID + " DESC",String.valueOf(limit));
    }

    public String getNextCaps(String capsurl, Boolean next){
        SQLiteDatabase db = getReadableDatabase();
        String caps = null;
        String condition = (next) ? " > " : " < ";
        String order = (next) ? " ASC" : " DESC";
        try {
            Cursor c = db.query(TABLE_NAME,new String[]{_MSGID},_POST + " LIKE '%"+ capsurl +"%'",null,null,null,_MSGID + " DESC","1");
            c.moveToFirst();
            String id = c.getString(c.getColumnIndex(_MSGID));
            c = db.query(TABLE_NAME,new String[]{_POST},_MSGID + condition + id + " AND " + _POST + " LIKE '%caps.radyomenemen.com/images%'", null,null,null,_MSGID + order, "1");
            c.moveToFirst();
            caps = c.getString(c.getColumnIndex(_POST));
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.close();
        return (caps != null) ? Menemen.getCapsUrl(Menemen.fromHtmlCompat(caps)) : capsurl;
    }

    public String getCapsUploader(String capsurl){
        SQLiteDatabase db = getReadableDatabase();
        String uploader = null;
        try {
            Cursor c = db.query(TABLE_NAME,null,_POST + " LIKE '%"+ capsurl +"%'",null,null,null,_MSGID + " DESC","1");
            c.moveToFirst();
            uploader = c.getString(c.getColumnIndex(_NICK)).toUpperCase();
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
            db.close();
            return null;
        }
        db.close();
        return uploader;
    }

    public Cursor loadCapsGalleryOnScroll(String msgid){
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_NAME,null,_POST + " LIKE '%caps.radyomenemen.com/images%' AND " + _MSGID + " < '" + msgid + "'",null,null,null,_MSGID + " DESC","10");
    }

    public String lastMsgId(){
        String lastid = "0";
        SQLiteDatabase db = getReadableDatabase();
        try {
            Cursor c = db.query(TABLE_NAME, new String[]{_MSGID},null,null,null,null,_MSGID + " DESC","1");
            c.moveToFirst();
            lastid = c.getString(c.getColumnIndex(_MSGID));
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.close();
        return lastid;
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
