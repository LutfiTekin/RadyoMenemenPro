package com.incitorrent.radyo.menemen.pro.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.incitorrent.radyo.menemen.pro.MainActivity;
import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RMPRO;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;

import org.json.JSONArray;
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
    final Context context = RMPRO.getContext();
    private NotificationManager notificationManager;
    private final static String GROUP_KEY_CHAT = "group_key_chat";
    private final static int CHAT_NOTIFICATION =  111;
    Menemen m = new Menemen(context);
    final Boolean notify = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifications", true);
    final Boolean notify_new_post = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifications_chat", false);
    final Boolean music_only = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("music_only",false);
    final Boolean is_chat_foreground = m.bool_oku(RadyoMenemenPro.IS_CHAT_FOREGROUND);
    final Boolean notify_when_on_air = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifications_on_air", true);
    public static final  String CHAT_BROADCAST_FILTER = "com.incitorrent.radyo.menemen.CHATUPDATE"; //CHAT Güncelle
    LocalBroadcastManager broadcasterForChat = LocalBroadcastManager.getInstance(context);
    Intent  notification_intent = new Intent(context, MainActivity.class);

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notification_intent.setAction("radyo.menemen.chat");
//Broadcast ekle sohbet fragmenti güncelle
        String topic = remoteMessage.getFrom();
        if(topic.equals(RadyoMenemenPro.FCMTopics.GENERAL)){
            //CHAT mesajı geldi
            //notify sohbet fragment
            Intent chat = new Intent(CHAT_BROADCAST_FILTER);
            String nick = getDATA(remoteMessage,"nick");
            String msg = getDATA(remoteMessage,"msg");
            String msgid = getDATA(remoteMessage,"msgid");
            chat.putExtra("nick",nick);
            chat.putExtra("msg",msg);
            chat.putExtra("msgid",msgid);
            broadcasterForChat.sendBroadcast(chat);
            Log.v("onMessageReceived", "message received"+ nick + " " + msg + " " + msgid);
            if (!notify || !notify_new_post || is_chat_foreground || music_only) return; //Create notification condition
            buildNotification(nick,msg);

        }else if(topic.equals(RadyoMenemenPro.FCMTopics.NEWS)){
            //OLAN BITEN
            //Son olan biteni al
            //TODO bildirim oluştur
            String lastob = null;
            try {
                lastob = new JSONObject(Menemen.getMenemenData(RadyoMenemenPro.OLAN_BITEN)).getJSONArray("olan_biten").getJSONArray(0).getJSONObject(0).getString("time");
                m.kaydet(RadyoMenemenPro.LASTOB,lastob);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else if(topic.equals(RadyoMenemenPro.FCMTopics.ONAIR)){
            //Onair bildirimi
          if(notify && notify_when_on_air){
              onAir(remoteMessage);
          }
        }
        super.onMessageReceived(remoteMessage);
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
        notification.setContentIntent(PendingIntent.getActivity(context, new Random().nextInt(200), notification_intent, PendingIntent.FLAG_CANCEL_CURRENT));
        if(PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null) != null)  notification.setSound(Uri.parse(PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null)));
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifications_on_air_vibrate", true))
            notification.setVibrate(new long[]{500, 500, 500});
        notification.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        notification.setAutoCancel(true);
        notificationManager.notify(RadyoMenemenPro.ON_AIR_NOTIFICATION, notification.build());
        Log.v(TAG, " Notification built");
    }

    private void buildNotification(String nick, String mesaj) {



        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.mipmap.ic_chat);
        builder.setAutoCancel(true);
           builder.setContentTitle(nick).setContentText(mesaj);

        if(PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null) != null)
            builder.setSound(Uri.parse(PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null)));
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifications_on_air_vibrate", true))
            builder.setVibrate(new long[]{500, 500, 500});
        builder.setContentIntent(PendingIntent.getActivity(context, new Random().nextInt(200), notification_intent, PendingIntent.FLAG_CANCEL_CURRENT));
        builder.setGroup(GROUP_KEY_CHAT);

        builder.setAutoCancel(true);
        Notification notification = builder.build();
       notificationManager.notify(new Random().nextInt(200), notification);
        Notification summary = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(context, new Random().nextInt(200), notification_intent, PendingIntent.FLAG_CANCEL_CURRENT))
                .setContentTitle(getString(R.string.notification_new_msg))
                .setContentText(getString(R.string.notification_new_messages_text))
                .setSmallIcon(R.mipmap.ic_chat)
                .setGroup(GROUP_KEY_CHAT)
                .setGroupSummary(true)
                .build();
        notificationManager.notify(CHAT_NOTIFICATION,summary);
    }

    private String getDATA(RemoteMessage rm,String data) {
        return rm.getData().get(data);
    }

}
