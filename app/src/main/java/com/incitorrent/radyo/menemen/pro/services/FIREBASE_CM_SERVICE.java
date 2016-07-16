package com.incitorrent.radyo.menemen.pro.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.incitorrent.radyo.menemen.pro.MainActivity;
import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Random;

/**
 * Radyo Menemen Pro Created by lutfi on 16.07.2016.
 */
public class FIREBASE_CM_SERVICE extends FirebaseMessagingService{
final Context context = FIREBASE_CM_SERVICE.this;
    private NotificationManager notificationManager;
    private final static String GROUP_KEY_CHAT = "group_key_chat";
    private final static int CHAT_NOTIFICATION =  111;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
//Broadcast ekle sohbet fragmenti güncelle
        String topic = remoteMessage.getFrom();
        if(topic.equals(RadyoMenemenPro.FCMTopics.GENERAL)){
            //CHAT mesajı geldi
            String nick = getDATA(remoteMessage,"nick");
            String msg = getDATA(remoteMessage,"msg");
            Log.v("onMessageReceived", "message received"+ nick + " " + msg);
            buildNotification(nick,msg);
            //TODO notify sohbet fragment
        }else if(topic.equals(RadyoMenemenPro.FCMTopics.NEWS)){
            //OLAN BITEN
        }
        super.onMessageReceived(remoteMessage);
    }

    private void buildNotification(String nick, String mesaj) {
 


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.mipmap.ic_chat);
        builder.setAutoCancel(true);


            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
                mesaj = Html.fromHtml(mesaj, Html.FROM_HTML_MODE_LEGACY).toString();
            else mesaj = Html.fromHtml(mesaj).toString();

           builder.setContentTitle(nick).setContentText(mesaj);

        if(PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null) != null)
            builder.setSound(Uri.parse(PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null)));
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifications_on_air_vibrate", true))
            builder.setVibrate(new long[]{500, 500, 500});

        Intent  notification_intent = new Intent(context, MainActivity.class);
        notification_intent.setAction("radyo.menemen.chat");
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
