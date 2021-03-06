package com.incitorrent.radyo.menemen.pro.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.incitorrent.radyo.menemen.pro.MainActivity;
import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RMPRO;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;
import com.incitorrent.radyo.menemen.pro.show_image_comments;
import com.incitorrent.radyo.menemen.pro.utils.DirectReplyReceiver;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;
import com.incitorrent.radyo.menemen.pro.utils.NotificationControls;
import com.incitorrent.radyo.menemen.pro.utils.capsDB;
import com.incitorrent.radyo.menemen.pro.utils.chatDB;
import com.incitorrent.radyo.menemen.pro.utils.topicDB;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import static com.incitorrent.radyo.menemen.pro.RadyoMenemenPro.broadcastinfo.DJ;

/**
 * Radyo Menemen Pro Created by lutfi on 16.07.2016.
 */
public class FIREBASE_CM_SERVICE extends FirebaseMessagingService{
    private static final String TAG = "FCM_SERVICE";
    public static final String CATEGORY_CAPS = "caps";
    public static final String CATEGORY_CHAT = "generalchat";
    public static final String CATEGORY_ONLINE = "online";
    public static final String CATEGORY_TOPICS = "cat_topics";
    public static final String ADD = "add";
    public static final String EDIT = "edit";
    public static final String DELETE = "delete";
    public static final String JOIN = "join";
    public static final String LEAVE = "leave";
    public static final String CLOSE = "close";
    public static final String ADD_USER = "adduser";
    final Context context = RMPRO.getContext();
    private NotificationCompat.Builder SUM_Notification;
    private NotificationManager notificationManager;
    private NotificationManagerCompat notificationManagerCompat;
    private NotificationCompat.MessagingStyle inbox;
    private final static String GROUP_KEY_CHAT = "group_key_chat";
    public final static int CHAT_NOTIFICATION =  111;
    public static final String DIRECT_REPLY_KEY  = "d_r";
    Menemen m = new Menemen(context);
    final boolean notify = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifications", true);
    final boolean notify_new_post = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifications_chat", true);
    final boolean notify_new_comment_caps = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifications_comment_caps", true);
    final boolean music_only = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("music_only",false);
    final boolean is_chat_foreground = m.bool_oku(RadyoMenemenPro.IS_CHAT_FOREGROUND);
    final boolean notify_when_on_air = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifications_on_air", true);
    final boolean notify_new_podcast = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifications_podcast", true);
    final boolean vibrate = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifications_on_air_vibrate", true);
    final boolean logged = m.isLoggedIn();
    Boolean mutechatnotification = m.getSavedTime(RadyoMenemenPro.MUTE_NOTIFICATION) > System.currentTimeMillis();
    public static final  String CHAT_BROADCAST_FILTER = "com.incitorrent.radyo.menemen.CHATUPDATE"; //CHAT Güncelle
    public static final  String CAPS_BROADCAST_FILTER = "com.incitorrent.radyo.menemen.CAPSUPDATE"; //CAPS Güncelle
    LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
    Intent  notification_intent = new Intent(context, MainActivity.class);
    chatDB sql;
    capsDB sql_caps;
    @SuppressLint("ServiceCast")
    @Override
    public void onCreate() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManagerCompat = NotificationManagerCompat.from(context);
        inbox = new NotificationCompat.MessagingStyle(getString(R.string.me));
        SUM_Notification = new NotificationCompat.Builder(context);
        sql = new chatDB(context,null,null,1);
        sql_caps = new capsDB(context,null,null,1);
        super.onCreate();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG,"onMessageReceived");
        notification_intent.setAction(RadyoMenemenPro.Action.CHAT);
        //Broadcast ekle sohbet fragmenti güncelle
        String topic = remoteMessage.getFrom();
        switch (topic) {
            case RadyoMenemenPro.FCMTopics.NEWS:
                //OLAN BITEN
                updateNews(remoteMessage);
                break;
            case RadyoMenemenPro.FCMTopics.ONAIR:
                //Onair bildirimi
                if (notify) onAir(remoteMessage);
                break;
            case RadyoMenemenPro.FCMTopics.PODCAST:
                if (notify && notify_new_podcast) notify_new_podcast(remoteMessage);
                break;
            case RadyoMenemenPro.FCMTopics.SYNC:
                sync();
                break;
            case RadyoMenemenPro.FCMTopics.SONG_CHANGE_EVENT:
                if(!m.isServiceRunning(MUSIC_PLAY_SERVICE.class))
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("songchange");
                if (m.isPlaying() && !m.bool_oku(RadyoMenemenPro.IS_PODCAST)) {
                    startService(new Intent(FIREBASE_CM_SERVICE.this, MUSIC_INFO_SERVICE.class));
                    if(getDATA(remoteMessage, "byuser").equals("1"))
                        notifySongChangedByListener();
                    else {
                        sendMenemenPointRequest();
                        notificationManager.cancel(RadyoMenemenPro.SONG_CHANGED_BY_USER_NOTIFICATION);
                    }
                }
                if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean("show_listeners",false) ){
                    m.kaydet(RadyoMenemenPro.LISTENERS_COUNT, getDATA(remoteMessage,"listeners"));
                }
                break;
            case RadyoMenemenPro.FCMTopics.NEW_PUBLIC_TOPIC:
                addNewTopic(remoteMessage);
                break;
            default:
                //Tokens or user-generated topics
                String category = remoteMessage.getData().get("cat");
                String action = getDATA(remoteMessage, "action");
                if (category == null) return;
                switch (category) {
                    case CATEGORY_CAPS:
                        //RECEIVE CAPS COMMENTS
                        if (action == null) break;
                        if (action.equals(ADD))
                            addCapsComments(remoteMessage);
                        break;
                    case CATEGORY_CHAT:
                        generalChat(remoteMessage);
                        break;
                    case CATEGORY_TOPICS:
                        if(action==null) break;
                        if(action.equals(JOIN))
                            userjoinedtotopic(getDATA(remoteMessage,"user"),getDATA(remoteMessage,topicDB._TOPICID));
                        else if(action.equals(LEAVE))
                            userlefttopic(getDATA(remoteMessage,"user"),getDATA(remoteMessage,topicDB._TOPICID));
                        else if(action.equals(ADD)) {
                            topicmsg(remoteMessage);
                        }
                        else if(action.equals(CLOSE))
                            closetopic(getDATA(remoteMessage,topicDB._TOPICID));
                        else if(action.equals(EDIT))
                            edittopic(remoteMessage);
                        else if(action.equals(ADD_USER))
                            if(getDATA(remoteMessage,"user").equals(m.getUsername()))
                                jointopic(getDATA(remoteMessage,topicDB._TOPICID),getDATA(remoteMessage, topicDB._CREATOR));
                        break;
                }

                break;
        }
        super.onMessageReceived(remoteMessage);
    }

    private void jointopic(final String topicid, final String creator) {
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest join = new StringRequest(Request.Method.POST, RadyoMenemenPro.MENEMEN_TOPICS_JOIN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(!m.getTopicDB().isTopicExists(topicid)) return;
                        if(response.equals("2")) {
                            m.getTopicDB().join(topicid);
                            //Notify user
                            if(!m.getTopicDB().getTopicInfo(topicid,topicDB._DESCR).equals(RadyoMenemenPro.PM)) {
                                try {
                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                                    builder.setSmallIcon(R.drawable.ic_topic_discussion);
                                    builder.setAutoCancel(true);
                                    builder.setContentTitle(m.getTopicDB().getTopicInfo(topicid, topicDB._TITLE))
                                            .setContentText(String.format(getString(R.string.topics_creator_added_you), creator));
                                    if (notify && PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null) != null)
                                        builder.setSound(Uri.parse(PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null)));
                                    if (vibrate)
                                        builder.setVibrate(new long[]{500, 500});
                                    notification_intent.setAction(RadyoMenemenPro.Action.TOPIC_MESSAGES);
                                    notification_intent.putExtra(topicDB._TOPICID, topicid);
                                    builder.setContentIntent(PendingIntent.getActivity(context, new Random().nextInt(200), notification_intent, PendingIntent.FLAG_UPDATE_CURRENT));
                                    builder.setAutoCancel(true);
                                    Notification notification = builder.build();
                                    int notification_id = CHAT_NOTIFICATION + new Random().nextInt(200);
                                    notificationManager.notify(notification_id, notification);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                    }
                },null){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> dataToSend = m.getAuthMap();
                dataToSend.put(topicDB._TOPICID,topicid);
                return dataToSend;
            }

            @Override
            public Priority getPriority() {
                return Priority.IMMEDIATE;
            }

            @Override
            public RetryPolicy getRetryPolicy() {
                return new RetryPolicy() {
                    @Override
                    public int getCurrentTimeout() {
                        return 5000;
                    }

                    @Override
                    public int getCurrentRetryCount() {
                        return 5;
                    }

                    @Override
                    public void retry(VolleyError error) throws VolleyError {

                    }
                };
            }
        };
        queue.add(join);
    }

    private void edittopic(RemoteMessage remoteMessage) {
        try {
        String topicid = getDATA(remoteMessage,topicDB._TOPICID);
        ContentValues contentValues = new ContentValues();
        contentValues.put(topicDB._TOPICID,topicid);
        contentValues.put(topicDB._TITLE,getDATA(remoteMessage,topicDB._TITLE));
        contentValues.put(topicDB._DESCR,getDATA(remoteMessage,"descr"));
        contentValues.put(topicDB._IMAGEURL,getDATA(remoteMessage,topicDB._IMAGEURL));
        m.getTopicDB().edittopic(contentValues);
        String message = String.format(getString(R.string.topic_edited_by_owner), m.getTopicDB().getTopicInfo(topicid, topicDB._CREATOR), getDATA(remoteMessage, "oldtitle"));
        if(m.getTopicDB().getTopicInfo(topicid,topicDB._CREATOR).equals(m.getUsername()))
            message = getString(R.string.topic_edit_success);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setSmallIcon(R.drawable.ic_topic_discussion);
            builder.setAutoCancel(true);
            builder.setContentTitle(getDATA(remoteMessage, "oldtitle"))
                    .setContentText(message);
            if (notify && PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null) != null)
                builder.setSound(Uri.parse(PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null)));
            if (vibrate)
                builder.setVibrate(new long[]{500,500});
            notification_intent.setAction(RadyoMenemenPro.Action.TOPIC_MESSAGES);
            notification_intent.putExtra(topicDB._TOPICID,topicid);
            builder.setContentIntent(PendingIntent.getActivity(context, new Random().nextInt(200), notification_intent, PendingIntent.FLAG_UPDATE_CURRENT));
            builder.setAutoCancel(true);
            Notification notification = builder.build();
            int notification_id = CHAT_NOTIFICATION + new Random().nextInt(200);
            notificationManager.notify(notification_id, notification);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void closetopic(String topicid) {
        try {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setSmallIcon(R.drawable.ic_topic_discussion);
            builder.setAutoCancel(true);
            builder.setContentTitle(m.getTopicDB().getTopicInfo(topicid,topicDB._TITLE))
                    .setContentText(getString(R.string.topic_closed_by_creator));
            if (notify && PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null) != null)
                builder.setSound(Uri.parse(PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null)));
            if (vibrate)
                builder.setVibrate(new long[]{500,500});
            notification_intent.setAction(RadyoMenemenPro.Action.TOPICS);
            builder.setContentIntent(PendingIntent.getActivity(context, new Random().nextInt(200), notification_intent, PendingIntent.FLAG_UPDATE_CURRENT));
            builder.setAutoCancel(true);
            Notification notification = builder.build();
            int notification_id = CHAT_NOTIFICATION + Integer.parseInt(topicid);
            notificationManager.notify(notification_id, notification);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } finally {
            m.getTopicDB().closeTopic(topicid);
        }

    }

    private void topicmsg(RemoteMessage remoteMessage) {
        Intent topic = new Intent(CHAT_BROADCAST_FILTER);
        String action = getDATA(remoteMessage, "action");
        String topicid = getDATA(remoteMessage,"tid");
        String msgid = getDATA(remoteMessage, "msgid");
        //CHAT mesajı geldi
        //notify sohbet fragment
        String nick = getDATA(remoteMessage,"nick");
        String msg = getDATA(remoteMessage,"msg");
        String time = getDATA(remoteMessage, "time");
        //add to db get auto incremented id
        msgid = String.valueOf(m.getTopicDB().addTopicMsg(new topicDB.TOPIC_MSGS(null,topicid,nick,msg,time)));
        final boolean is_topic_foreground = m.bool_oku(RadyoMenemenPro.IS_CHAT_FOREGROUND + "tid" + topicid);
        if(is_topic_foreground) {
            //Update ui only if chat is foreground
            topic.putExtra(topicDB._TOPICID,topicid);
            topic.putExtra("nick", nick);
            topic.putExtra("msg", msg);
            topic.putExtra("msgid", msgid);
            topic.putExtra("time", time);
            topic.putExtra("action", ADD);
            broadcastManager.sendBroadcast(topic);
        }


        final boolean isUser = nick.equals(m.getUsername());
        //Notification creation condition
        if (!notify || !notify_new_post || is_topic_foreground || music_only || !logged || isUser) return;
        buildChatNotification(nick,Menemen.fromHtmlCompat(msg),topicid);

    }

    private void userlefttopic(String user, String topicid) {
        if(user.equals(m.getUsername())) return;
        try {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setSmallIcon(R.drawable.ic_topic_discussion);
            builder.setAutoCancel(true);
            builder.setContentTitle(m.getTopicDB().getTopicInfo(topicid,topicDB._TITLE))
                    .setContentText(m.getSpannedTextWithSmileys(String.format(getString(R.string.topic_user_left), user)));
            if (notify && PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null) != null)
                builder.setSound(Uri.parse(PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null)));
            if (vibrate)
                builder.setVibrate(new long[]{500,500});
            notification_intent.setAction(RadyoMenemenPro.Action.TOPIC_MESSAGES);
            notification_intent.putExtra(topicDB._TOPICID,topicid);
            builder.setContentIntent(PendingIntent.getActivity(context, new Random().nextInt(200), notification_intent, PendingIntent.FLAG_UPDATE_CURRENT));
            builder.setAutoCancel(true);
            Notification notification = builder.build();
            int notification_id = CHAT_NOTIFICATION + Integer.parseInt(topicid);
            notificationManager.notify(notification_id, notification);
            long msgid = m.getTopicDB().addTopicMsg(new topicDB.TOPIC_MSGS(null,topicid,getString(R.string.app_name),String.format(getString(R.string.topic_user_left), user),Menemen.getFormattedDate(System.currentTimeMillis(), RadyoMenemenPro.CHAT_DATE_FORMAT)));
            if(m.bool_oku(RadyoMenemenPro.IS_CHAT_FOREGROUND + "tid" + topicid)) {
                Intent topic = new Intent(CHAT_BROADCAST_FILTER);
                //Update ui only if chat is foreground
                topic.putExtra(topicDB._TOPICID,topicid);
                topic.putExtra("nick", getString(R.string.app_name));
                topic.putExtra("msg", String.format(getString(R.string.topic_user_left), user));
                topic.putExtra("msgid", String.valueOf(msgid));
                topic.putExtra("time", Menemen.getFormattedDate(System.currentTimeMillis(), RadyoMenemenPro.CHAT_DATE_FORMAT));
                topic.putExtra("action", ADD);
                broadcastManager.sendBroadcast(topic);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void userjoinedtotopic(String user, String topicid) {
        if(user.equals(m.getUsername())) return;
        try {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setSmallIcon(R.drawable.ic_topic_discussion);
            builder.setAutoCancel(true);
            builder.setContentTitle(m.getTopicDB().getTopicInfo(topicid,topicDB._TITLE))
                    .setContentText(m.getSpannedTextWithSmileys(String.format(getString(R.string.topic_user_joined), user)));
            if (notify && PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null) != null)
                builder.setSound(Uri.parse(PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null)));
            if (vibrate)
                builder.setVibrate(new long[]{500,500});
            notification_intent.setAction(RadyoMenemenPro.Action.TOPIC_MESSAGES);
            notification_intent.putExtra(topicDB._TOPICID,topicid);
            builder.setContentIntent(PendingIntent.getActivity(context, new Random().nextInt(200), notification_intent, PendingIntent.FLAG_UPDATE_CURRENT));
            builder.setAutoCancel(true);
            Notification notification = builder.build();
            int notification_id = CHAT_NOTIFICATION + Integer.parseInt(topicid);
            notificationManager.notify(notification_id, notification);
            long msgid = m.getTopicDB().addTopicMsg(new topicDB.TOPIC_MSGS(null,topicid,getString(R.string.app_name),String.format(getString(R.string.topic_user_joined), user),Menemen.getFormattedDate(System.currentTimeMillis(), RadyoMenemenPro.CHAT_DATE_FORMAT)));
            if(m.bool_oku(RadyoMenemenPro.IS_CHAT_FOREGROUND+"tid"+topicid)) {
                Intent topic = new Intent(CHAT_BROADCAST_FILTER);
                //Update ui only if chat is foreground
                topic.putExtra(topicDB._TOPICID,topicid);
                topic.putExtra("nick", getString(R.string.app_name));
                topic.putExtra("msg", String.format(getString(R.string.topic_user_joined), user));
                topic.putExtra("msgid", String.valueOf(msgid));
                topic.putExtra("time", Menemen.getFormattedDate(System.currentTimeMillis(), RadyoMenemenPro.CHAT_DATE_FORMAT));
                topic.putExtra("action", ADD);
                broadcastManager.sendBroadcast(topic);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addNewTopic(RemoteMessage rm) {
        m.getTopicDB().addtoTopicHistory(new topicDB.TOPIC(
                getDATA(rm,"id"),
                getDATA(rm,"tpc"),
                getDATA(rm,"creator"),
                (getDATA(rm,"creator").equals(m.getUsername())) ? "1" : "0",
                getDATA(rm,"title"),
                getDATA(rm,"descr"),
                getDATA(rm,"image"),
                getDATA(rm,"type")
                ));
    }

    private void notifySongChangedByListener() {
        NotificationCompat.Builder notification;
        notification = new NotificationCompat.Builder(context);
        notification.setContentTitle(getString(R.string.track_changed))
                .setContentText(getString(R.string.track_changed_summary))
                .setSmallIcon(R.drawable.ic_library_music_black_24dp);
        //Main activity yi aç
        notification_intent.setAction(RadyoMenemenPro.Action.RADIO);
        notification.setContentIntent(PendingIntent.getActivity(context, new Random().nextInt(), notification_intent, PendingIntent.FLAG_UPDATE_CURRENT));
        notification.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        notification.setPriority(Notification.PRIORITY_LOW);
        notification.setAutoCancel(true);
        notificationManager.notify(RadyoMenemenPro.SONG_CHANGED_BY_USER_NOTIFICATION, notification.build());
    }

    private void sendMenemenPointRequest() {
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest postRequest = new StringRequest(Request.Method.POST, RadyoMenemenPro.MP_ADD,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    }
                },
                null
        ) {
            @Override
            protected Map<String, String> getParams(){
                try {
                    return m.getAuthMap();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
        queue.add(postRequest);
    }



    //TODO use remote message parameter
    private void sync() {
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, RadyoMenemenPro.SYNCCHANNEL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject J = new JSONObject(response);
                            JSONArray JARR = J.getJSONArray("info");
                            JSONObject Jo = JARR.getJSONObject(0);
                            m.kaydet(RadyoMenemenPro.LOW_CHANNEL,Jo.getString(RadyoMenemenPro.LOW_CHANNEL));
                            m.kaydet(RadyoMenemenPro.MID_CHANNEL,Jo.getString(RadyoMenemenPro.MID_CHANNEL));
                            m.kaydet(RadyoMenemenPro.HIGH_CHANNEL,Jo.getString(RadyoMenemenPro.HIGH_CHANNEL));
                            m.kaydet(RadyoMenemenPro.CAPS_API_KEY,Jo.getString("capsapikey"));
                            if(m.isLoggedIn() && m.isFirstTime("tokenset")) m.setToken();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },null);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(9000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
    }


    private void generalChat(RemoteMessage remoteMessage) {
        Intent chat = new Intent(CHAT_BROADCAST_FILTER);
        String action = getDATA(remoteMessage, "action");
        String msgid = getDATA(remoteMessage,"msgid");
        if(action.equals(DELETE)){
            sql.deleteMSG(msgid);
            if(is_chat_foreground) {
                chat.putExtra("action", DELETE);
                chat.putExtra("msgid", msgid);
                broadcastManager.sendBroadcast(chat);
            }else
            notificationManagerCompat.cancel(FIREBASE_CM_SERVICE.CHAT_NOTIFICATION);
            return;
        }
        //CHAT mesajı geldi
        //notify sohbet fragment
        String nick = getDATA(remoteMessage,"nick");
        String msg = getDATA(remoteMessage,"msg");
        String time = getDATA(remoteMessage, "time");
        if(is_chat_foreground) {
            //Update ui only if chat is foreground
            chat.putExtra("nick", nick);
            chat.putExtra("msg", msg);
            chat.putExtra("msgid", msgid);
            chat.putExtra("time", time);
            chat.putExtra("action", ADD);
            broadcastManager.sendBroadcast(chat);
        }
        //add to db
        sql.addtoHistory(new chatDB.CHAT(msgid,nick,msg,time));

        final Boolean isUser = nick.equals(m.getUsername());
        //Notification creation condition
        if (!notify || !notify_new_post || is_chat_foreground || music_only || !logged || isUser) return;
        buildChatNotification(nick,Menemen.fromHtmlCompat(msg),null);
    }

    private void addCapsComments(RemoteMessage remoteMessage) {
        //Get data
        String msgid = getDATA(remoteMessage, "msgid");
        String nick = getDATA(remoteMessage, "nick");
        String comment = getDATA(remoteMessage, "comment");
        String time = getDATA(remoteMessage, "time");
        String caps_url = getDATA(remoteMessage, "caps_url");
        String caps_id = getDATA(remoteMessage, "caps_id");
        //Check if caps comments screen is in foreground
        Boolean is_caps_foreground = m.bool_oku(RadyoMenemenPro.IS_CHAT_FOREGROUND + caps_url);
        //Create ıntent
        if (is_caps_foreground) {
            //Only broadcast if caps is in foreground
            Intent caps_comment = new Intent(CAPS_BROADCAST_FILTER);
            caps_comment.putExtra("msgid",msgid)
                    .putExtra("nick", nick)
                    .putExtra("comment", comment)
                    .putExtra("time", time)
                    .putExtra("caps_url", caps_url)
                    .putExtra("action", ADD);
            broadcastManager.sendBroadcast(caps_comment);
        }
        //Add to sql
        sql_caps.addtoHistory(new capsDB.CAPS(msgid, caps_url, nick, comment, time));
        //Build notification
        notification_intent = new Intent(context, show_image_comments.class);
        notification_intent.setAction(RadyoMenemenPro.Action.CAPS)
                .putExtra("url",caps_url);
        if(!nick.equals(m.getUsername()) && notify_new_comment_caps && !is_caps_foreground)
            buildNotificationforCaps(nick,comment,caps_id);
    }

    private void notify_new_podcast(RemoteMessage rm) {
        final String podcast_message = getDATA(rm,"podcast_msg");
        NotificationCompat.Builder notification;
        notification = new NotificationCompat.Builder(context);
        notification.setContentTitle(getString(R.string.notification_title_new_podcast))
                .setContentText(podcast_message)
                .setSmallIcon(R.drawable.ic_on_air);
        try {
            notification.setLargeIcon(Glide.with(context).load(R.mipmap.ic_launcher).asBitmap().into(100,100).get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        //podcast fragmenti aç
        notification_intent.setAction(RadyoMenemenPro.Action.PODCAST);
        notification.setContentIntent(PendingIntent.getActivity(context, new Random().nextInt(200), notification_intent, PendingIntent.FLAG_CANCEL_CURRENT));
        if(PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null) != null)  notification.setSound(Uri.parse(PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null)));
        if (vibrate)
            notification.setVibrate(new long[]{500, 1000, 500});
        notification.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        notification.setAutoCancel(true);
        notificationManager.notify(RadyoMenemenPro.PODCAST_NOTIFICATION, notification.build());
    }

    private void updateNews(RemoteMessage remoteMessage) {
        //Son olan biteni al
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest request = new StringRequest(Request.Method.GET, RadyoMenemenPro.OLAN_BITEN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            String lastob = new JSONObject(response).getJSONArray("olan_biten").getJSONArray(0).getJSONObject(0).getString("time");
                            m.kaydet(RadyoMenemenPro.LASTOB,lastob);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },null){
            @Override
            public Priority getPriority() {
                return Priority.LOW;
            }
        };
        queue.add(request);
        String title,content;
        title = getDATA(remoteMessage, "title");
        content = getDATA(remoteMessage, "content");
        //Bildirim oluştur
        newsNotification(title, content);
    }

    private void newsNotification(String title, String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.announce);
        builder.setAutoCancel(true);
        builder.setContentTitle(title)
                .setContentText(m.getSpannedTextWithSmileys(content))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(m.getSpannedTextWithSmileys(content)))
                .setSubText(getString(R.string.news));
            if (notify && PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null) != null)
                builder.setSound(Uri.parse(PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null)));
            if (vibrate)
                builder.setVibrate(new long[]{500, 1000, 500, 1000});
        notification_intent.setAction(RadyoMenemenPro.Action.OLAN_BITEN);
        builder.setContentIntent(PendingIntent.getActivity(context, new Random().nextInt(200), notification_intent, PendingIntent.FLAG_UPDATE_CURRENT));
        builder.setAutoCancel(true);
        Notification notification = builder.build();
        int notification_id = RadyoMenemenPro.ON_AIR_NOTIFICATION + 2;
        notificationManager.notify(notification_id, notification);
    }

    private void onAir(RemoteMessage rm) {
        String dj = getDATA(rm,DJ);
        NotificationCompat.Builder notification;
        notification = new NotificationCompat.Builder(context);
        notification.setContentTitle(getString(R.string.notification_onair_title))
                .setContentText(dj + getString(R.string.notification_onair_content))
                .setSmallIcon(R.drawable.ic_on_air);
        try {
            notification.setLargeIcon(Glide.with(context).load(R.mipmap.ic_launcher).asBitmap().into(100,100).get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        //Play radio action
        if(!m.isPlaying()) {
            Intent playpause = new Intent(this, NotificationControls.class);
            playpause.putExtra(RadyoMenemenPro.DATA_SOURCE,m.getRadioDataSource());
            PendingIntent ppIntent = PendingIntent.getBroadcast(this, new Random().nextInt(102), playpause, PendingIntent.FLAG_CANCEL_CURRENT);
            notification.addAction(R.drawable.ic_play_arrow_black_24dp,getString(R.string.media_play_now),ppIntent);
        }
        //Main activity yi aç
        notification_intent.setAction(RadyoMenemenPro.Action.RADIO);
        int notificationid = (m.isPlaying()) ? RadyoMenemenPro.ON_AIR_NOTIFICATION : RadyoMenemenPro.NOW_PLAYING_NOTIFICATION;
        notification.setContentIntent(PendingIntent.getActivity(context, new Random().nextInt(), notification_intent, PendingIntent.FLAG_UPDATE_CURRENT));
        if (notify_when_on_air) {
            if(PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null) != null)
                notification.setSound(Uri.parse(PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null)));
            if (vibrate)
                notification.setVibrate(new long[]{500, 500, 500});
        }
        notification.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        notification.setAutoCancel(true);
        notificationManager.notify(notificationid, notification.build());
    }

    private void buildChatNotification(String nick, String mesaj, @Nullable String topicid) {
        boolean isTopic = topicid != null;
        boolean ispm = false;
        String pm_user = null;
        if(isTopic) {
            ispm = m.getTopicDB().getTopicInfo(topicid,topicDB._DESCR).equals(RadyoMenemenPro.PM);
            notification_intent.setAction(ispm ? RadyoMenemenPro.Action.PRIVATE_MESSAGE : RadyoMenemenPro.Action.TOPIC_MESSAGES);
            if(ispm){
                //Extract username from pm topic title
                pm_user = m.getTopicDB().getTopicInfo(topicid, topicDB._TITLE)
                        .substring(2)
                        .replaceAll(m.getUsername(), "")
                        .replaceAll("\\+", "");
                notification_intent.putExtra(RadyoMenemenPro.NICK,pm_user);
            }else notification_intent.putExtra(topicDB._TOPICID,topicid);
        }
        boolean isUser = nick.equals(m.getUsername()); //Mesaj gönderen kişi kullancının kendisi mi? (PCDEN GÖNDERME DURUMUNDA OLABİLİR)
        if(isUser)
            nick = getString(R.string.me);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon((isTopic) ? R.drawable.ic_topic_discussion : R.mipmap.ic_chat);
        if(ispm) builder.setSmallIcon(R.drawable.ic_pm);
        builder.setOnlyAlertOnce(true);
        builder.setAutoCancel(true);
          if(isTopic) {
              //Check if pm topic
              if(ispm){
                  if(pm_user!=null)
                      builder.setContentTitle(String.format(getString(R.string.topics_pm_with_user), pm_user));
              }else
                  builder.setContentTitle(m.getTopicDB().getTopicInfo(topicid, topicDB._TITLE)).setContentText(nick + ": " + m.getSpannedTextWithSmileys(mesaj));
          }
        else
              builder.setContentTitle(nick).setContentText(m.getSpannedTextWithSmileys(mesaj));
        if(!mutechatnotification && !m.isPlaying()) {
            if (PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null) != null)
                builder.setSound(Uri.parse(PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null)));
            if (vibrate)
                builder.setVibrate(new long[]{500, 500, 500});
        }
        builder.setContentIntent(PendingIntent.getActivity(context, new Random().nextInt(200), notification_intent, PendingIntent.FLAG_UPDATE_CURRENT));
        builder.setLights(Color.BLUE, 1000, 500);
        builder.setAutoCancel(true);
        Notification notification = builder.build();
        //add lines to inbox
        try {
            DateFormat df = new SimpleDateFormat(RadyoMenemenPro.CHAT_DATE_FORMAT, Locale.US);
            Cursor cursor =
                    (isTopic) ?
                            m.getTopicDB().getTopicMessagesById(m.oku(RadyoMenemenPro.LAST_ID_SEEN_ON_TOPIC + topicid),topicid)
                            : sql.getHistoryById(m.oku(RadyoMenemenPro.LAST_ID_SEEN_ON_CHAT));
            cursor.moveToLast();
            while(!cursor.isBeforeFirst()){
                String user,post,time;
                user = cursor.getString(cursor.getColumnIndex(chatDB._NICK));
                if(user.equals(m.getUsername()))
                    user = null;
                post = Menemen.fromHtmlCompat(cursor.getString(cursor.getColumnIndex(chatDB._POST)));
                time = cursor.getString(cursor.getColumnIndex(chatDB._TIME));
                inbox.addMessage(post, df.parse(time).getTime(),user);
                cursor.moveToPrevious();
            }
            cursor.close();
            sql.close();
        } catch (Exception e) {
            inbox.addMessage(Menemen.fromHtmlCompat(mesaj),System.currentTimeMillis(),nick);
            e.printStackTrace();
        }
        if(inbox.getMessages().size()<2)
            notificationManager.notify(CHAT_NOTIFICATION, notification);
        if (inbox.getMessages().size()>1) {
            Bitmap largeicon = null;
            try {
                if(isTopic && !m.getTopicDB().getTopicInfo(topicid,topicDB._IMAGEURL).equals("default"))
                    largeicon = Glide.with(context)
                            .load(RadyoMenemenPro.CAPS_IMAGES_PATH + m.getTopicDB().getTopicInfo(topicid,topicDB._IMAGEURL))
                            .asBitmap()
                            .placeholder(R.drawable.ic_topic_discussion)
                            .error(R.mipmap.ic_launcher).into(100,100).get();
                else
                    largeicon = Glide.with(context).load(R.mipmap.ic_launcher).asBitmap().into(100,100).get();
            } catch (Exception e){e.printStackTrace();}
            //DIRECT REPLY
            if(ispm){
                inbox.setConversationTitle(String.format(getString(R.string.topics_pm_with_user), pm_user));
            }
            else inbox.setConversationTitle((isTopic) ? m.getTopicDB().getTopicInfo(topicid,topicDB._TITLE) : getString(R.string.notification_new_messages_text));

            SUM_Notification
                    .setAutoCancel(true)
                    .setContentIntent(PendingIntent.getActivity(context, new Random().nextInt(200), notification_intent, PendingIntent.FLAG_UPDATE_CURRENT))
                    .setContentTitle((isTopic) ? m.getTopicDB().getTopicInfo(topicid,topicDB._TITLE) : getString(R.string.notification_new_msg))
                    .setContentText(String.format("%s: %s", nick, m.getSpannedTextWithSmileys(mesaj)))
                    .setSmallIcon((isTopic) ? R.drawable.ic_topic_discussion : R.mipmap.ic_chat)
                    .setStyle(inbox)
                    .setOnlyAlertOnce(true);
            if(ispm)
                SUM_Notification.setContentTitle(String.format(getString(R.string.topics_pm_with_user), pm_user));
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                if(!SUM_Notification.mActions.isEmpty())
                    SUM_Notification.mActions.clear();
                Intent direct_reply_intent = new Intent(context, DirectReplyReceiver.class);
                direct_reply_intent.setAction((isTopic) ? RadyoMenemenPro.Action.TOPIC_MESSAGES : RadyoMenemenPro.Action.CHAT);
                if(isTopic) direct_reply_intent.putExtra(topicDB._TOPICID,topicid);
                if(!ispm)SUM_Notification.addAction(m.getDirectReplyAction(direct_reply_intent));
            }
            if(largeicon != null) SUM_Notification.setLargeIcon(largeicon);
            if(!(m.getSavedTime(RadyoMenemenPro.MUTE_NOTIFICATION) > System.currentTimeMillis()) && !isUser) {
                if (!m.isPlaying() && PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null) != null)
                    SUM_Notification.setSound(Uri.parse(PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null)));
                if (vibrate)
                    SUM_Notification.setVibrate(new long[]{500, 500, 500});
            }
            if(!mutechatnotification)
                m.saveTime(RadyoMenemenPro.MUTE_NOTIFICATION,(1000*5)); //Sonraki 5 saniyeyi sustur
            Notification summary = SUM_Notification.build();
            try {
                notificationManagerCompat.notify((isTopic) ? CHAT_NOTIFICATION + Integer.parseInt(topicid) : CHAT_NOTIFICATION,summary);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }



    private void buildNotificationforCaps(String nick, String mesaj, String caps_id) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.default_image);
        builder.setAutoCancel(true);
        builder.setContentTitle(nick).setContentText(m.getSpannedTextWithSmileys(mesaj));
        builder.setSubText(getString(R.string.notification_caps_comment_sub_text));
        if(!mutechatnotification && m.isPlaying()) {
            if (PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null) != null)
                builder.setSound(Uri.parse(PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null)));
            if (vibrate)
                builder.setVibrate(new long[]{500, 500, 500});
        }
        builder.setContentIntent(PendingIntent.getActivity(context, new Random().nextInt(200), notification_intent, PendingIntent.FLAG_UPDATE_CURRENT));
        builder.setGroup(GROUP_KEY_CHAT);
        builder.setAutoCancel(true);
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            Intent direct_reply_intent = new Intent(context, DirectReplyReceiver.class);
            direct_reply_intent.setAction(notification_intent.getAction());
            direct_reply_intent.putExtra("url",notification_intent.getExtras().getString("url"));
            direct_reply_intent.putExtra("id",caps_id);
            builder.addAction(m.getDirectReplyAction(direct_reply_intent));
        }
        Notification notification = builder.build();
        int notification_id = RadyoMenemenPro.CAPS_NOTIFICATION;
        try {
            notification_id = Integer.parseInt(caps_id) + RadyoMenemenPro.CAPS_NOTIFICATION;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        notificationManager.notify(notification_id, notification);
    }
      private String getDATA(RemoteMessage rm, String data) {
        return rm.getData().get(data);
    }

}
