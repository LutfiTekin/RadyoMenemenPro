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
import com.incitorrent.radyo.menemen.pro.utils.Menemen;
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
    final Boolean music_only = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("music_only",false);
    final Boolean is_chat_foreground = m.bool_oku(RadyoMenemenPro.IS_CHAT_FOREGROUND);
    final Boolean notify_when_on_air = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifications_on_air", true);
    final Boolean notify_new_podcast = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifications_podcast", true);
    final Boolean vibrate = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifications_on_air_vibrate", true);
    final Boolean logged = m.oku("logged").equals("evet");
    final Boolean mutechatnotification = m.isNotificationMuted();
    public static final  String CHAT_BROADCAST_FILTER = "com.incitorrent.radyo.menemen.CHATUPDATE"; //CHAT Güncelle
    LocalBroadcastManager broadcasterForChat = LocalBroadcastManager.getInstance(context);
    Intent  notification_intent = new Intent(context, MainActivity.class);
    chatDB sql;
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
        Log.v(TAG,"onCreate");
        super.onCreate();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        notification_intent.setAction("radyo.menemen.chat");
//Broadcast ekle sohbet fragmenti güncelle
        String topic = remoteMessage.getFrom();
        Intent chat = new Intent(CHAT_BROADCAST_FILTER);
        String msgid = getDATA(remoteMessage,"msgid");
        if(topic.equals(RadyoMenemenPro.FCMTopics.GENERAL)){
            String action = getDATA(remoteMessage, "action");
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
            if (!notify || !notify_new_post || is_chat_foreground || music_only || !logged || mutechatnotification) return; //Create notification condition
            buildNotification(nick,msg);
        }else if(topic.equals(RadyoMenemenPro.FCMTopics.NEWS)){
            //OLAN BITEN
            //TODO bildirim oluştur
            updateNews();
        }else if(topic.equals(RadyoMenemenPro.FCMTopics.ONAIR)){
            //Onair bildirimi
          if(notify && notify_when_on_air){
              onAir(remoteMessage);
          }
        }else if(topic.equals(RadyoMenemenPro.FCMTopics.PODCAST)){
          if(notify && notify_new_podcast)  notify_new_podcast(remoteMessage);
        }
        super.onMessageReceived(remoteMessage);
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
        //Main activity yi aç
        notification.setContentIntent(PendingIntent.getActivity(context, new Random().nextInt(200), notification_intent, PendingIntent.FLAG_CANCEL_CURRENT));
        if(PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null) != null)  notification.setSound(Uri.parse(PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null)));
        if (vibrate)
            notification.setVibrate(new long[]{500, 1000, 500});
        notification.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        notification.setAutoCancel(true);
        notificationManager.notify(RadyoMenemenPro.PODCAST_NOTIFICATION, notification.build());
        Log.v(TAG, " Notification built");
    }

    private void updateNews() {
        //Son olan biteni al
        Log.v("updateNews","updated");
        String lastob = null;
        try {
            lastob = new JSONObject(Menemen.getMenemenData(RadyoMenemenPro.OLAN_BITEN)).getJSONArray("olan_biten").getJSONArray(0).getJSONObject(0).getString("time");
            m.kaydet(RadyoMenemenPro.LASTOB,lastob);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
//        inbox.addLine(String.format("%s: %s", nick, mesaj));
           builder.setContentTitle(nick).setContentText(Menemen.fromHtmlCompat(mesaj));
        if(PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null) != null)
            builder.setSound(Uri.parse(PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null)));
        if (vibrate)
            builder.setVibrate(new long[]{500, 500, 500});
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
                inbox.addLine(String.format("%s: %s", user, post));
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
                .setSmallIcon(R.mipmap.ic_chat)
                .setStyle(inbox)
                .setGroup(GROUP_KEY_CHAT)
                .setGroupSummary(true);
        if(largeicon != null) SUM_Notification.setLargeIcon(largeicon);
        if(PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null) != null && !isUser)
            SUM_Notification.setSound(Uri.parse(PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null)));
        if (vibrate && !isUser)
            SUM_Notification.setVibrate(new long[]{500, 500, 500});
        Notification summary = SUM_Notification.build();
        notificationManagerCompat.notify(GROUP_CHAT_NOTIFICATION,summary);
//        notificationManager.notify(GROUP_CHAT_NOTIFICATION,summary);
    }

    private String getDATA(RemoteMessage rm,String data) {
        return rm.getData().get(data);
    }

}
