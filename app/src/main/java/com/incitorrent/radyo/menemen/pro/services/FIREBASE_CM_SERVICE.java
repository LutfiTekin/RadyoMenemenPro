package com.incitorrent.radyo.menemen.pro.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.incitorrent.radyo.menemen.pro.MainActivity;
import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RMPRO;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;
import com.incitorrent.radyo.menemen.pro.show_image_comments;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;
import com.incitorrent.radyo.menemen.pro.utils.capsDB;
import com.incitorrent.radyo.menemen.pro.utils.chatDB;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;
import java.util.concurrent.ExecutionException;

import static com.incitorrent.radyo.menemen.pro.RadyoMenemenPro.broadcastinfo.DJ;

/**
 * Radyo Menemen Pro Created by lutfi on 16.07.2016.
 */
public class FIREBASE_CM_SERVICE extends FirebaseMessagingService{
    private static final String TAG = "FCM_SERVICE";
    public static final String CATEGORY_CAPS = "caps";
    public static final String CATEGORY_HAYKIR = "haykir";
    public static final String ADD = "add";
    public static final String DELETE = "delete";
    final Context context = RMPRO.getContext();
    private NotificationCompat.Builder SUM_Notification;
    private NotificationManager notificationManager;
    private NotificationManagerCompat notificationManagerCompat;
    private NotificationCompat.InboxStyle inbox;
    private final static String GROUP_KEY_CHAT = "group_key_chat";
    public final static int GROUP_CHAT_NOTIFICATION =  111;
    Menemen m = new Menemen(context);
    final Boolean notify = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifications", true);
    final Boolean notify_new_post = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifications_chat", true);
    final Boolean notify_new_comment_caps = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifications_comment_caps", true);
    final Boolean music_only = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("music_only",false);
    final Boolean is_chat_foreground = m.bool_oku(RadyoMenemenPro.IS_CHAT_FOREGROUND);
    final Boolean notify_when_on_air = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifications_on_air", true);
    final Boolean notify_new_podcast = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifications_podcast", true);
    final Boolean vibrate = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifications_on_air_vibrate", true);
    final Boolean logged = m.oku("logged").equals("evet");
    final Boolean mutechatnotification = m.isNotificationMuted();
    public static final  String CHAT_BROADCAST_FILTER = "com.incitorrent.radyo.menemen.CHATUPDATE"; //CHAT Güncelle
    public static final  String CAPS_BROADCAST_FILTER = "com.incitorrent.radyo.menemen.CAPSUPDATE"; //CAPS Güncelle
    LocalBroadcastManager broadcasterForChat = LocalBroadcastManager.getInstance(context);
    Intent  notification_intent = new Intent(context, MainActivity.class);
    chatDB sql;
    capsDB sql_caps;
    @SuppressLint("ServiceCast")
    @Override
    public void onCreate() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManagerCompat = NotificationManagerCompat.from(context);
        inbox = new NotificationCompat.InboxStyle();
        inbox.setSummaryText(getString(R.string.notification_new_messages_text));
        inbox.setBigContentTitle(getString(R.string.notification_new_messages_text));
        SUM_Notification = new NotificationCompat.Builder(context);
        sql = new chatDB(context,null,null,1);
        sql_caps = new capsDB(context,null,null,1);
//        Log.v(TAG,"onCreate" + " token " + FirebaseInstanceId.getInstance().getToken());
        super.onCreate();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
//        Log.v("onMessageR", remoteMessage.getFrom());
        notification_intent.setAction("radyo.menemen.chat");
        //Broadcast ekle sohbet fragmenti güncelle
        String topic = remoteMessage.getFrom();
        if(topic.equals(RadyoMenemenPro.FCMTopics.GENERAL)){
            generalChat(remoteMessage);
        }else if(topic.equals(RadyoMenemenPro.FCMTopics.NEWS)){
            //OLAN BITEN
           if(notify) updateNews(remoteMessage);
        }else if(topic.equals(RadyoMenemenPro.FCMTopics.ONAIR)){
            //Onair bildirimi
          if(notify && notify_when_on_air) onAir(remoteMessage);
        }else if(topic.equals(RadyoMenemenPro.FCMTopics.PODCAST)){
          if(notify && notify_new_podcast)  notify_new_podcast(remoteMessage);
        }else{
            //Topic yok
            String category = remoteMessage.getData().get("cat");
            if(category == null) return;
            switch (category) {
                case CATEGORY_CAPS:
                    //RECEIVE CAPS COMMENTS
                    String action = getDATA(remoteMessage, "action");
                    if (action == null) break;
                    if (action.equals(ADD))
                        addCapsComments(remoteMessage);
                    break;
                case CATEGORY_HAYKIR:
                    haykirbildirim(remoteMessage);
                    break;
            }

        }
        super.onMessageReceived(remoteMessage);
    }

    private void haykirbildirim(RemoteMessage remoteMessage) {
        String mesaj = getDATA(remoteMessage, "response");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.default_image);
        builder.setAutoCancel(true);
        builder.setContentTitle(getString(R.string.dj_response)).setContentText(m.getSpannedTextWithSmileys(mesaj));
        if(!mutechatnotification) {
            if (PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null) != null)
                builder.setSound(Uri.parse(PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null)));
            if (vibrate)
                builder.setVibrate(new long[]{1500, 500, 500});
        }
        builder.setContentIntent(PendingIntent.getActivity(context, new Random().nextInt(200), notification_intent, PendingIntent.FLAG_UPDATE_CURRENT));
        builder.setGroup(GROUP_KEY_CHAT);
        builder.setAutoCancel(true);
        Notification notification = builder.build();
        notificationManager.notify(new Random().nextInt(100), notification);
    }

    private void generalChat(RemoteMessage remoteMessage) {
        Intent chat = new Intent(CHAT_BROADCAST_FILTER);
        String action = getDATA(remoteMessage, "action");
        String msgid = getDATA(remoteMessage,"msgid");
        if(action.equals(DELETE)){
            sql.deleteMSG(msgid);
            chat.putExtra("action",DELETE);
            chat.putExtra("msgid",msgid);
            broadcasterForChat.sendBroadcast(chat);
            return;
        }
        //CHAT mesajı geldi
        //notify sohbet fragment
        String nick = getDATA(remoteMessage,"nick");
        String msg = getDATA(remoteMessage,"msg");
        String time = getDATA(remoteMessage, "time");
        chat.putExtra("nick",nick);
        chat.putExtra("msg",msg);
        chat.putExtra("msgid",msgid);
        chat.putExtra("time",time);
        chat.putExtra("action",ADD);
        broadcasterForChat.sendBroadcast(chat);
        //add to db
        sql.addtoHistory(new chatDB.CHAT(msgid,nick,msg,time));
        Log.v(TAG, "message received"+ nick + " " + msg + " " + msgid + " " + time);
        if (!notify || !notify_new_post || is_chat_foreground || music_only || !logged) return; //Create notification condition
        buildNotification(nick,msg);
    }

    private void addCapsComments(RemoteMessage remoteMessage) {
        //Get data
        String msgid = getDATA(remoteMessage, "msgid");
        String nick = getDATA(remoteMessage, "nick");
        String comment = getDATA(remoteMessage, "comment");
        String time = getDATA(remoteMessage, "time");
        String caps_url = getDATA(remoteMessage, "caps_url");
        String caps_id = getDATA(remoteMessage, "caps_id");
        //Create ıntent
        Intent caps_comment = new Intent(CAPS_BROADCAST_FILTER);
        caps_comment.putExtra("msgid",msgid)
                .putExtra("nick", nick)
                .putExtra("comment", comment)
                .putExtra("time", time)
                .putExtra("caps_url", caps_url)
                .putExtra("action", ADD);
        broadcasterForChat.sendBroadcast(caps_comment);
        //Add to sql
        sql_caps.addtoHistory(new capsDB.CAPS(msgid, caps_url, nick, comment, time));
        //Build notification
        notification_intent = new Intent(context, show_image_comments.class);
        notification_intent.setAction("radyo.menemen.caps")
                .putExtra("url",caps_url);
        Boolean is_caps_foreground = m.bool_oku(RadyoMenemenPro.IS_CHAT_FOREGROUND + caps_url);
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
        notification_intent.setAction("radyo.menemen.podcast");
        notification.setContentIntent(PendingIntent.getActivity(context, new Random().nextInt(200), notification_intent, PendingIntent.FLAG_CANCEL_CURRENT));
        if(PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null) != null)  notification.setSound(Uri.parse(PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null)));
        if (vibrate)
            notification.setVibrate(new long[]{500, 1000, 500});
        notification.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        notification.setAutoCancel(true);
        notificationManager.notify(RadyoMenemenPro.PODCAST_NOTIFICATION, notification.build());
        Log.v(TAG, " Notification built");
    }

    private void updateNews(RemoteMessage remoteMessage) {
        //Son olan biteni al
        String lastob = null;
        try {
            lastob = new JSONObject(Menemen.getMenemenData(RadyoMenemenPro.OLAN_BITEN)).getJSONArray("olan_biten").getJSONArray(0).getJSONObject(0).getString("time");
            m.kaydet(RadyoMenemenPro.LASTOB,lastob);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
            if (PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null) != null)
                builder.setSound(Uri.parse(PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null)));
            if (vibrate)
                builder.setVibrate(new long[]{500, 1000, 500, 1000});
        notification_intent.setAction("radyo.menemen.news");
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
        //Main activity yi aç
        notification.setContentIntent(PendingIntent.getActivity(context, new Random().nextInt(200), notification_intent, PendingIntent.FLAG_UPDATE_CURRENT));
        if(PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null) != null)  notification.setSound(Uri.parse(PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null)));
        if (vibrate)
            notification.setVibrate(new long[]{500, 500, 500});
        notification.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        notification.setAutoCancel(true);
        notificationManager.notify(RadyoMenemenPro.ON_AIR_NOTIFICATION, notification.build());
        Log.v(TAG, " Notification built");
    }

    private void buildNotification(String nick, String mesaj) {
        Boolean isUser = nick.equals(m.oku("username")); //Mesaj gönderen kişi kullancının kendisi mi? (PCDEN GÖNDERME DURUMUNDA OLABİLİR)
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.mipmap.ic_chat);
        builder.setAutoCancel(true);
           builder.setContentTitle(nick).setContentText(m.getSpannedTextWithSmileys(mesaj));
        if(!mutechatnotification) {
            if (PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null) != null)
                builder.setSound(Uri.parse(PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null)));
            if (vibrate)
                builder.setVibrate(new long[]{500, 500, 500});
        }
        builder.setContentIntent(PendingIntent.getActivity(context, new Random().nextInt(200), notification_intent, PendingIntent.FLAG_UPDATE_CURRENT));
        builder.setGroup(GROUP_KEY_CHAT);
        builder.setAutoCancel(true);
        Notification notification = builder.build();
       notificationManager.notify(new Random().nextInt(200), notification);
        //add lines to inbox
        try {
            Cursor cursor = sql.getHistoryById(m.oku(RadyoMenemenPro.LAST_ID_SEEN_ON_CHAT));
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                String user,post;
                user = cursor.getString(cursor.getColumnIndex(chatDB._NICK));
                post = cursor.getString(cursor.getColumnIndex(chatDB._POST));
                inbox.addLine(String.format("%s: %s", user, m.getSpannedTextWithSmileys(post)));
                cursor.moveToNext();
            }
            cursor.close();
            sql.close();
        } catch (Exception e) {
            inbox.addLine(String.format("%s: %s", nick, Menemen.fromHtmlCompat(mesaj)));
            e.printStackTrace();
        }
        Bitmap largeicon = null;
        try {
            largeicon = Glide.with(context).load(R.mipmap.ic_launcher).asBitmap().into(100,100).get();
        } catch (Exception e){e.printStackTrace();}
        SUM_Notification
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(context, new Random().nextInt(200), notification_intent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setContentTitle(getString(R.string.notification_new_msg))
                .setContentText(String.format("%s: %s", nick, m.getSpannedTextWithSmileys(mesaj)))
                .setSmallIcon(R.mipmap.ic_chat)
                .setStyle(inbox)
                .setGroup(GROUP_KEY_CHAT)
                .setGroupSummary(true)
                .setOnlyAlertOnce(true);
        if(largeicon != null) SUM_Notification.setLargeIcon(largeicon);
        if(!mutechatnotification) {
            if (PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null) != null && !isUser)
                SUM_Notification.setSound(Uri.parse(PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null)));
            if (vibrate && !isUser)
                SUM_Notification.setVibrate(new long[]{500, 500, 500});
        }
        Notification summary = SUM_Notification.build();
        notificationManagerCompat.notify(GROUP_CHAT_NOTIFICATION,summary);
    }



    private void buildNotificationforCaps(String nick, String mesaj, String caps_id) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.default_image);
        builder.setAutoCancel(true);
        builder.setContentTitle(nick).setContentText(m.getSpannedTextWithSmileys(mesaj));
        builder.setSubText(getString(R.string.notification_caps_comment_sub_text));
        if(!mutechatnotification) {
            if (PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null) != null)
                builder.setSound(Uri.parse(PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null)));
            if (vibrate)
                builder.setVibrate(new long[]{500, 500, 500});
        }
        builder.setContentIntent(PendingIntent.getActivity(context, new Random().nextInt(200), notification_intent, PendingIntent.FLAG_UPDATE_CURRENT));
        builder.setGroup(GROUP_KEY_CHAT);
        builder.setAutoCancel(true);
        Notification notification = builder.build();
        int notification_id = RadyoMenemenPro.ON_AIR_NOTIFICATION + 1;
        try {
            notification_id = Integer.parseInt(caps_id) + RadyoMenemenPro.ON_AIR_NOTIFICATION;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        notificationManager.notify(notification_id, notification);
    }
      private String getDATA(RemoteMessage rm, String data) {
        return rm.getData().get(data);
    }

}
