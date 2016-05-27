package com.incitorrent.radyo.menemen.pro.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by lutfi on 21.05.2016.
 */
public class radioDB extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "radyomenemenproCalan.db";

    public static final String TABLE_NAME = "radyo";
    public static final String _SONGID = "songid";
    public static final String _SONGNAME = "song";
    public static final String _SONGHASH = "hash";
    public static final String _URL = "url";

    public radioDB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE "+ TABLE_NAME + "("+
                _SONGID + " INTEGER PRIMARY KEY," +
                _SONGNAME + " TEXT, " +
                _URL + " TEXT, " +
                _SONGHASH + " TEXT " +
                ");";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
    public void addtoHistory(Songs s){
        Log.v("ADD TO HISTORY",s.get_SONGNAME());
        ContentValues values = new ContentValues();
        values.put(_SONGID, s.get_SONGID());
        values.put(_SONGNAME, s.get_SONGNAME());
        values.put(_URL, s.get_URL());
        values.put(_SONGHASH, s.get_SONGHASH());
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }catch (Exception e){
            e.printStackTrace();
            Log.v("DBLog",e.toString());
        }
        db.close();
    }
    public Cursor getHistory(int limit){
        SQLiteDatabase db = getReadableDatabase(); //TODO test
        if(limit < 8) limit = 8;
        return db.query(TABLE_NAME,null,null,null,null,null,_SONGID+" DESC", String.valueOf(limit));
    }


    public static class Songs{
        public String _SONGID;
        public String _SONGNAME;
        public String _URL;
        public String _SONGHASH;

    public Songs( String _SONGHASH, String _SONGID, String _SONGNAME, String _URL) {


        this._SONGHASH = _SONGHASH;
        this._SONGID = _SONGID;
        this._SONGNAME = _SONGNAME;
        this._URL = _URL;
    }


    public String get_URL() {
        return _URL;
    }

    public void set_URL(String _URL) {
        this._URL = _URL;
    }

    public String get_SONGHASH() {
        return _SONGHASH;
    }

    public void set_SONGHASH(String _SONGHASH) {
        this._SONGHASH = _SONGHASH;
    }

    public String get_SONGID() {
        return _SONGID;
    }

    public void set_SONGID(String _SONGID) {
        this._SONGID = _SONGID;
    }

    public String get_SONGNAME() {
        return _SONGNAME;
    }

    public void set_SONGNAME(String _SONGNAME) {
        this._SONGNAME = _SONGNAME;
    }
}
}
