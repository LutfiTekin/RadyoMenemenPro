//package com.incitorrent.radyo.menemen.pro.utils;
//
//import android.content.Context;
//import android.database.sqlite.SQLiteDatabase;
//import android.database.sqlite.SQLiteOpenHelper;
//
///**
// * Created by lutfi on 21.05.2016.
// */
//public class sohbetDB extends SQLiteOpenHelper {
//    public static final int DATABASE_VERSION = 1;
//    public static final String DATABASE_NAME = "radyoemenemenproSohbet.db";
//
//    public static final String TABLE_NAME = "sohbet";
//    public static final String _MSGID = "msgid";
//    public static final String _NICK = "nick";
//    public static final String _POST = "post";
//    public static final String _TIME = "time";
//
//    public sohbetDB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
//        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
//    }
//
//    @Override
//    public void onCreate(SQLiteDatabase db) {
//    db.execSQL("CREATE TABLE" + TABLE_NAME + "("+
//        _MSGID + " INTEGER PRIMARY KEY,"+
//            _NICK + " TEXT"+
//            _POST + " " +
//
//
//
//    );
//    }
//
//    @Override
//    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//
//    }
//}
