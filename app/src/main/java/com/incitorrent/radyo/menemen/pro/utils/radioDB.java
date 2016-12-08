package com.incitorrent.radyo.menemen.pro.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by lutfi on 21.05.2016.
 */
public class radioDB extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "radyomenemenproCalan.db";
    public static final String TABLE_NAME = "radyo";
    public static final String _SONGID = "songid";
    public static final String _SONGNAME = "song";
    public static final String _SONGHASH = "hash";
    public static final String _URL = "url";
    public static final String _ARTURL = "arturl";

    public radioDB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE "+ TABLE_NAME + "("+
                _SONGID + " INTEGER PRIMARY KEY," +
                _SONGNAME + " TEXT, " +
                _URL + " TEXT, " +
                _ARTURL + " TEXT, " +
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
        values.put(_ARTURL, s.get_ARTURL());
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }catch (Exception e){
            e.printStackTrace();
            Log.d("DBLog",e.toString());
        }
        db.close();
    }
    public Cursor getHistory(int limit,@Nullable String searchquery){
        SQLiteDatabase db = getReadableDatabase();
        searchquery = (searchquery != null) ? _SONGNAME + " LIKE '%" + searchquery + "%'" : null;
        return db.query((searchquery!=null),TABLE_NAME,null,searchquery,null,null,null,_SONGID + " DESC", (limit == 0) ? null : String.valueOf(limit));
    }

    public void deleteTrack(String songhash){
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME,_SONGHASH + "=\'"+ songhash + "\'",null);
        db.close();
    }

    public static class Songs{
        public String _SONGID,_SONGNAME,_URL,_SONGHASH,_ARTURL;

        public Songs(String _SONGHASH, String _SONGID, String _SONGNAME, String _URL, String _ARTURL) {
            this._SONGID = _SONGID;
            this._SONGNAME = _SONGNAME;
            this._URL = _URL;
            this._SONGHASH = _SONGHASH;
            this._ARTURL = _ARTURL;
        }

        public String get_SONGID() {
            return _SONGID;
        }

        public String get_SONGNAME() {
            return _SONGNAME;
        }

        public String get_URL() {
            return _URL;
        }

        public String get_SONGHASH() {
            return _SONGHASH;
        }

        public String get_ARTURL() {
            return _ARTURL;
        }

        public void set_SONGID(String _SONGID) {
            this._SONGID = _SONGID;
        }

        public void set_SONGNAME(String _SONGNAME) {
            this._SONGNAME = _SONGNAME;
        }

        public void set_URL(String _URL) {
            this._URL = _URL;
        }

        public void set_SONGHASH(String _SONGHASH) {
            this._SONGHASH = _SONGHASH;
        }

        public void set_ARTURL(String _ARTURL) {
            this._ARTURL = _ARTURL;
        }
    }
}
