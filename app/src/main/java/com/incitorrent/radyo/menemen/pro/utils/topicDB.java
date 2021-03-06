package com.incitorrent.radyo.menemen.pro.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;
import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;

/**
 * Radyo Menemen Pro Created by lutfi on 3.08.2016.
 */
public class topicDB extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 8;
    public static final String DATABASE_NAME = "radyomenemenproTopic.db";
    public static final String TOPICS_TABLE = "topics";
    public static final String _TOPICID = "tid";
    public static final String _TOPICSTR = "topicstr";
    public static final String _CREATOR = "creator";
    public static final String _JOINED = "joined";
    public static final String _TYPE = "type";
    public static final String _TITLE = "title";
    public static final String _DESCR = "description";
    public static final String _IMAGEURL = "image";

    //Messages table
    public static final String MESSAGES_TABLE = "topics_msg";
    public static final String _TOPIC_MSG_ID = "topics_msg_id";
    public static final String _NICK = "nick";
    public static final String _POST = "post";
    public static final String _TIME = "time";

    //Relevant constants
    public static final String JOINED = "1";
    public static final String PUBLIC_TOPIC = JOINED;
    public static final String PRIVATE_TOPIC = "2";

    private Context context;
    public topicDB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
        this.context = context;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE "+ TOPICS_TABLE + "("+
                _TOPICID + " INTEGER PRIMARY KEY," +
                _TOPICSTR + " TEXT, " +
                _CREATOR + " TEXT, " +
                _JOINED + " INTEGER, " +
                _TYPE + " INTEGER, " +
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
        values.put(_TYPE, t.get_TYPE());
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
     * @return all topics except current user is creator
     * or topic type is private
     */
    public Cursor listTopıcs(){
        Menemen m = new Menemen(context);
        String curuser = m.getUsername();
        return getReadableDatabase().rawQuery("SELECT " + TOPICS_TABLE + ".* FROM " +TOPICS_TABLE +
                " INNER JOIN " + MESSAGES_TABLE + " ON " + MESSAGES_TABLE + "." + _TOPICID + "=" + TOPICS_TABLE + "." + _TOPICID +
                " WHERE " + _CREATOR + "!= ? AND (" + _JOINED + " ='1' OR " + _TYPE + " ='1')" +
                "GROUP BY " + TOPICS_TABLE  + "." + _TOPICID + " HAVING MAX(" + MESSAGES_TABLE +"."+ _TOPIC_MSG_ID + ") " +
                " ORDER BY " + MESSAGES_TABLE + "." + _TOPIC_MSG_ID + " DESC" ,new String[]{curuser});
    }
    /**
     * List all the Topics stored on the phone
     * @return all new topics that user not joined
     */
    public Cursor listNewTopics(){
        return getReadableDatabase().rawQuery("SELECT * FROM " +TOPICS_TABLE +
                " WHERE " + _TYPE + "='1' AND " + _JOINED + "!='1'" +
                " ORDER BY " + _TOPICID + " DESC",null);
    }

    /**
     * List all the Topics stored on the phone
     * @return all topics created by current user
     */
    public Cursor liswOwnTopics(){
        Menemen m = new Menemen(context);
        String curuser = m.getUsername();
        return getReadableDatabase().rawQuery("SELECT * FROM " + TOPICS_TABLE +
                " WHERE " + _CREATOR + "= ? " +
                " ORDER BY " + _TOPICID + " DESC",new String[]{curuser});
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
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(_JOINED,"1");
            int rows = db.update(TOPICS_TABLE,contentValues,_TOPICID + "='" + topicid + "' ",null);
            Log.d(TOPICS_TABLE, rows + " affected");
            String topicstr = getTopicSTR(topicid);
            if(topicstr!=null)
                FirebaseMessaging.getInstance().subscribeToTopic(topicstr);
            TOPIC_MSGS tm = new TOPIC_MSGS(null,topicid,context.getString(R.string.app_name),context.getString(R.string.topics_curuser_joined),Menemen.getFormattedDate(System.currentTimeMillis(), RadyoMenemenPro.CHAT_DATE_FORMAT));
            addTopicMsg(tm);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            db.close();
        }
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
        db.delete(TOPICS_TABLE, _TYPE + "='1'",null);
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

    public String getTopicId(TOPIC topıc){
        SQLiteDatabase db = getReadableDatabase();
        try {
            Cursor c;
            String topicstr = topıc.get_TOPICSTR();
            String title = topıc.get_TITLE();
            if(topicstr!=null)
                c = db.query(TOPICS_TABLE,new String[]{_TOPICID}, _TOPICSTR+ "='" + topicstr + "'",null,null,null,null,"1");
            else if(title != null)
                c = db.query(TOPICS_TABLE,new String[]{_TOPICID}, _TITLE+ "='" + title + "'",null,null,null,null,"1");
            else return null;
            c.moveToFirst();
            String s = c.getString(c.getColumnIndex(_TOPICID));
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
                    c.getString(c.getColumnIndex(_IMAGEURL)),
                    c.getString(c.getColumnIndex(_TYPE))
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
    /**
     * Check if topic exist with given title
     * @param title
     * @return
     */
    public boolean isTopicExists(String title,boolean searchbytitle){
        SQLiteDatabase db = getReadableDatabase();
        long rownum = DatabaseUtils.queryNumEntries(db, TOPICS_TABLE, _TITLE + "='"+ title + "'" );
        db.close();
        return rownum > 0;
    }

    /**
     * Check if any pm topics exist between user1 and user2
     * @param user1
     * @param user2
     * @return
     */
    public String getPmTopicIdIfExist(String user1, String user2){
        if (isTopicExists(RadyoMenemenPro.PM + user1 + "+" + user2, true)) {
            return getTopicId(new TOPIC(RadyoMenemenPro.PM + user1 + "+" + user2,null));
        }
        if (isTopicExists(RadyoMenemenPro.PM + user2 + "+" + user1, true)) {
            return getTopicId(new TOPIC(RadyoMenemenPro.PM + user2 + "+" + user1,null));
        }
        return null;
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
        public String _TOPICID,_TOPICSTR,_CREATOR,_JOINED,_TITLE,_DESCR,_IMAGEURL, _TYPE;

        public TOPIC(String _TOPICID, String _TOPICSTR, String _CREATOR, String _JOINED, String _TITLE, String _DESCR, String _IMAGEURL, String _TYPE) {
            this._TOPICID = _TOPICID;
            this._TOPICSTR = _TOPICSTR;
            this._CREATOR = _CREATOR;
            this._JOINED = _JOINED;
            this._TITLE = _TITLE;
            this._DESCR = _DESCR;
            this._IMAGEURL = _IMAGEURL;
            this._TYPE = _TYPE;
        }

        public TOPIC (@Nullable String _TITLE,@Nullable String _TOPICSTR){
            this._TOPICSTR = _TOPICSTR;
            this._TITLE = _TITLE;
        }

        public String get_TOPICID() {
            return _TOPICID;
        }

        public String get_TOPICSTR() {
            return _TOPICSTR;
        }

        public String get_CREATOR() {
            return _CREATOR;
        }

        public String get_JOINED() {
            return _JOINED;
        }

        public String get_TITLE() {
            return _TITLE;
        }

        public String get_DESCR() {
            return _DESCR;
        }

        public String get_IMAGEURL() {
            return _IMAGEURL;
        }

        public String get_TYPE() {
            return _TYPE;
        }
    }


}
