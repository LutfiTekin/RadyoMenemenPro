package com.incitorrent.radyo.menemen.pro.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Radyo Menemen Pro Created by lutfi on 3.08.2016.
 */
public class topicDB extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 7;
    public static final String DATABASE_NAME = "radyomenemenproTopic.db";
    public static final String TOPICS_TABLE = "topics";
    public static final String _TOPICID = "tid";
    public static final String _TOPICSTR = "topicstr";
    public static final String _CREATOR = "creator";
    public static final String _JOINED = "joined";
    public static final String _TITLE = "title";
    public static final String _DESCR = "description";
    public static final String _IMAGEURL = "image";

    //Messages table
    public static final String MESSAGES_TABLE = "topics_msg";
    public static final String _TOPIC_MSG_ID = "topics_msg_id";
    public static final String _NICK = "nick";
    public static final String _POST = "post";
    public static final String _TIME = "time";

    public topicDB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE "+ TOPICS_TABLE + "("+
                _TOPICID + " INTEGER PRIMARY KEY," +
                _TOPICSTR + " TEXT, " +
                _CREATOR + " TEXT, " +
                _JOINED + " INTEGER, " +
                _TITLE + " TEXT, " +
                _DESCR + " TEXT, " +
                _IMAGEURL + " TEXT " +
                ");";
        db.execSQL(query);
        query = "CREATE TABLE " + MESSAGES_TABLE + "("+
                _TOPIC_MSG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                _TOPICID + " TEXT, " +
                _NICK + " TEXT, " +
                _POST + " TEXT, " +
                _TIME + " TEXT " +
                ");";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TOPICS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + MESSAGES_TABLE);
        onCreate(db);
    }

    public void addtoTopicHistory(TOPIC t){
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
            db.insertWithOnConflict(TOPICS_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            db.close();
        }
    }

    /**
     * Add message to MESSAGES_TABLE
     * @param tm
     */
    public long addTopicMsg(TOPIC_MSGS tm){
        ContentValues values = new ContentValues();
        values.put(_TOPICID, tm.get_TOPIC_ID());
        values.put(_NICK, tm.get_NICK());
        values.put(_POST, tm.get_POST());
        values.put(_TIME, tm.get_TIME());
        SQLiteDatabase db = getWritableDatabase();

        try {
           long msgid = db.insertWithOnConflict(MESSAGES_TABLE,null,values,SQLiteDatabase.CONFLICT_REPLACE);
            db.close();
            return msgid;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            db.close();
        }
        return -1;
    }

    /**
     * List all the Topics stored on the phone
     * @return
     */
    public Cursor listTopıcs(){
        return getReadableDatabase().query(TOPICS_TABLE,null,null,null,null,null, _TOPICID +" DESC", null);
    }

    /**
     * List topic messages by given limit ordering desc
     * @param limit
     * @return
     */
    public Cursor getHistory(int limit, String topicid){
        Log.d("TOPIC","requested info from " + topicid);
        SQLiteDatabase db = getReadableDatabase();
        if(limit < 20) limit = 20;
        return db.query(MESSAGES_TABLE,null,_TOPICID + " ='" + topicid + "'",null,null,null,_TOPIC_MSG_ID + " DESC", String.valueOf(limit));
    }

    public boolean isHistoryExist(String capsurl,String nick){
        SQLiteDatabase db = getReadableDatabase();
        long rownum = DatabaseUtils.queryNumEntries(db, TOPICS_TABLE, _TOPICSTR + "='"+ capsurl + "' AND " + _CREATOR + "='" + nick + "'" );
        return rownum > 0;
    }

    /**
     * Get topic messages ordered by given reference message id
     * @param msgid
     * @return
     */
    public Cursor getTopicMessagesById(@Nullable String msgid, String topicid){
        if(msgid == null) msgid = "0";
        Log.d(TOPICS_TABLE,"MSGID " + msgid);
        SQLiteDatabase db = getReadableDatabase();
        return db.query(MESSAGES_TABLE,null,_TOPICID + "='" + topicid + "' AND " + _TOPIC_MSG_ID + ">='" + msgid + "'",null,null,null,_TOPIC_MSG_ID +" DESC");
    }

    /**
     * Get topic messages on recyclerviews on scroll event
     * @param msgid
     * @return
     */
    public Cursor getTopicMessagesOnScroll(String msgid,String topicid){
        SQLiteDatabase db = getReadableDatabase();
        return db.query(MESSAGES_TABLE,null,_TOPIC_MSG_ID  + "<'" + msgid + "' AND " + _TOPICID + " ='"+topicid+"'",null,null,null,_TOPIC_MSG_ID +" DESC","40");
    }

    /**
     * Delete a message by given message id
     * @param msgid
     */
    public void deleteMSG(String msgid){
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.delete(MESSAGES_TABLE, _TOPIC_MSG_ID + "=\'"+ msgid + "\'",null);
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get Last message id from messages table
     * @return
     */
    public String lastMsgId(){
        String lastid = "0";
        SQLiteDatabase db = getReadableDatabase();
        try {
            Cursor c = db.query(MESSAGES_TABLE, new String[]{_TOPIC_MSG_ID},null,null,null,null,_TOPIC_MSG_ID + " DESC","1");
            c.moveToFirst();
            lastid = c.getString(c.getColumnIndex(_TOPIC_MSG_ID));
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.close();
        return lastid;
    }

    /**
     * Get TopicStr from given topic id
     * @param topicid
     * @return
     * @throws NullPointerException
     */
    public String getTopicSTR(String topicid) throws NullPointerException{
        String topicstr = null;
        SQLiteDatabase db = getReadableDatabase();
        try {
            Cursor c = db.query(TOPICS_TABLE, new String[]{_TOPICSTR},_TOPICID + " ='"+topicid + "'",null,null,null,null,"1");
            c.moveToFirst();
            topicstr = c.getString(c.getColumnIndex(_TOPICSTR));
            c.close();
            db.close();
        } catch (Exception e) {
            db.close();
            e.printStackTrace();
        }
        return topicstr;
    }

    /**
     * Join the topic with the given id
     * @param topicid
     */
    public void join(String topicid){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(_JOINED,"1");
        int rows = db.update(TOPICS_TABLE,contentValues,_TOPICID + "='" + topicid + "' ",null);
        Log.d(TOPICS_TABLE, rows + " affected");
        db.close();
    }
    /**
     * Leave the topic with the given id
     * @param topicid
     */
    public void leave(String topicid){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(_JOINED,"0");
        int rows = db.update(TOPICS_TABLE,contentValues,_TOPICID + "='" + topicid + "' ", null);
        Log.d(TOPICS_TABLE, rows + " affected");
        db.delete(MESSAGES_TABLE,_TOPICID + "='" + topicid + "' ",null);
        db.close();
    }

    /**
     * Close the topic with given id and delete everything inside
     * @param topicid
     */
    public void closeTopic(String topicid){
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TOPICS_TABLE,_TOPICID + "='" + topicid + "'", null);
        db.delete(MESSAGES_TABLE,_TOPICID + "='" + topicid + "'",null);
        db.close();
    }

    /**
     * Delete topics to keep sycnronized with server
     */
    public void refreshTopics(){
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TOPICS_TABLE,"1",null);
        db.close();
    }

    /**
     * Check if current user is
     * joined to topic with the given id
     * @param topicid
     * @return
     */
    public boolean isJoined(String topicid){
        SQLiteDatabase db = getReadableDatabase();
        long rownum = DatabaseUtils.queryNumEntries(db, TOPICS_TABLE, _TOPICID + "='"+ topicid + "' AND " + _JOINED + "='1'" );
        db.close();
        return rownum > 0;
    }

    /**
     * Get info from topic with given topic id and column
     * @param topicid
     * @param info
     * @return
     */
    public String getTopicInfo(String topicid,String info){
        SQLiteDatabase db = getReadableDatabase();
        try {
            Cursor c = db.query(TOPICS_TABLE,new String[]{info}, _TOPICID + "='" + topicid + "'",null,null,null,null,"1");
            c.moveToFirst();
            String s = c.getString(c.getColumnIndex(info));
            c.close();
            db.close();
            return s;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get Topic Object containin all the info with the
     * given topic id
     * @param topicid
     * @return
     */
    public TOPIC getTopic(String topicid){
        SQLiteDatabase db = getReadableDatabase();

        try {
            Cursor c = db.query(TOPICS_TABLE,null, _TOPICID + "='" + topicid + "'",null,null,null,null,"1");
            c.moveToFirst();
            TOPIC topıc = new TOPIC(
                    c.getString(c.getColumnIndex(_TOPICID)),
                    c.getString(c.getColumnIndex(_TOPICSTR)),
                    c.getString(c.getColumnIndex(_CREATOR)),
                    c.getString(c.getColumnIndex(_JOINED)),
                    c.getString(c.getColumnIndex(_TITLE)),
                    c.getString(c.getColumnIndex(_DESCR)),
                    c.getString(c.getColumnIndex(_IMAGEURL))
                    );
            c.close();
            db.close();
            return topıc;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void edittopic(ContentValues contentValues){
        SQLiteDatabase db = getWritableDatabase();
        try {
            int rows = db.update(TOPICS_TABLE,contentValues,_TOPICID + " ='" + contentValues.get(topicDB._TOPICID) + "'",null);
            Log.d(TOPICS_TABLE, rows + " affected");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    /**
     * Check if topic exist with given id
     * @param topicid
     * @return
     */
    public boolean isTopicExists(String topicid){
        SQLiteDatabase db = getReadableDatabase();
        long rownum = DatabaseUtils.queryNumEntries(db, TOPICS_TABLE, _TOPICID + "='"+ topicid + "'" );
        db.close();
        return rownum > 0;
    }

    public static class TOPIC_MSGS{
        public String _TOPIC_MSG_ID,_TOPIC_ID,_NICK,_POST,_TIME;

        public TOPIC_MSGS(String _TOPIC_MSG_ID, String _TOPIC_ID, String _NICK, String _POST, String _TIME) {
            this._TOPIC_MSG_ID = _TOPIC_MSG_ID;
            this._TOPIC_ID = _TOPIC_ID;
            this._NICK = _NICK;
            this._POST = _POST;
            this._TIME = _TIME;
        }

        public String get_TOPIC_MSG_ID() {
            return _TOPIC_MSG_ID;
        }

        public void set_TOPIC_MSG_ID(String _TOPIC_MSG_ID) {
            this._TOPIC_MSG_ID = _TOPIC_MSG_ID;
        }

        public String get_TOPIC_ID() {
            return _TOPIC_ID;
        }

        public void set_TOPIC_ID(String _TOPIC_ID) {
            this._TOPIC_ID = _TOPIC_ID;
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
