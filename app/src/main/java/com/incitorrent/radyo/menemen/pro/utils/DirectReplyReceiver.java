package com.incitorrent.radyo.menemen.pro.utils;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.incitorrent.radyo.menemen.pro.MainActivity;
import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;
import com.incitorrent.radyo.menemen.pro.services.FIREBASE_CM_SERVICE;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class DirectReplyReceiver extends BroadcastReceiver {
    Menemen m;
    public DirectReplyReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        m = new Menemen(context);
       if(intent.getAction()!=null){
           switch (intent.getAction()){
               case RadyoMenemenPro.Action.CHAT:
                   if(getMessage(intent) != null)
                       postToMenemen(String.valueOf(getMessage(intent)),context);
                   break;
           }
       }


    }
    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    private CharSequence getMessage(Intent ıntent){
        Bundle remoteInput = RemoteInput.getResultsFromIntent(ıntent);
        if(remoteInput != null){
            return remoteInput.getCharSequence(FIREBASE_CM_SERVICE.DIRECT_REPLY_KEY);
        }
        return null;
    }


    private void postToMenemen(final String msg,final Context context) {
        if(!m.isInternetAvailable()) {
            Toast.makeText(context.getApplicationContext(), R.string.toast_internet_warn, Toast.LENGTH_SHORT).show();
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(context.getApplicationContext());
        StringRequest post = new StringRequest(Request.Method.POST, RadyoMenemenPro.MESAJ_GONDER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        updateNotification(context, msg);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                updateNotification(context, msg);
              error.printStackTrace();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> dataToSend = m.getAuthMap();
                dataToSend.put("mesaj", msg);
                return dataToSend;
            }

            @Override
            public Priority getPriority() {
                return Priority.IMMEDIATE;
            }

            @Override
            public RetryPolicy getRetryPolicy() {
                return m.menemenRetryPolicy();
            }
        };
        queue.add(post);
    }

    void updateNotification(Context context, String msg) {
        NotificationCompat.MessagingStyle inbox = new NotificationCompat.MessagingStyle(context.getString(R.string.me));
        try {
            chatDB sql = m.getChatDB();
            DateFormat df = new SimpleDateFormat(RadyoMenemenPro.CHAT_DATE_FORMAT, Locale.US);
            Cursor cursor = sql.getHistoryById(m.oku(RadyoMenemenPro.LAST_ID_SEEN_ON_CHAT));
            cursor.moveToLast();
            while(!cursor.isBeforeFirst()){
                String user,post,time;
                user = cursor.getString(cursor.getColumnIndex(chatDB._NICK));
                if(user.equals(m.getUsername()))
                    user = null;
                post = cursor.getString(cursor.getColumnIndex(chatDB._POST));
                time = cursor.getString(cursor.getColumnIndex(chatDB._TIME));
                inbox.addMessage(post, df.parse(time).getTime(),user);
                cursor.moveToPrevious();
            }
            cursor.close();
            sql.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        inbox.addMessage(msg,System.currentTimeMillis(),null);

        NotificationCompat.Builder SUM_Notification = new NotificationCompat.Builder(context);
        Intent notification_intent = new Intent(context, MainActivity.class);
        notification_intent.setAction(RadyoMenemenPro.Action.CHAT);
        inbox.setConversationTitle(context.getString(R.string.notification_new_msg));
        SUM_Notification
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(context, new Random().nextInt(200), notification_intent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setContentTitle(context.getString(R.string.notification_new_msg))
                .setSmallIcon(R.mipmap.ic_chat)
                .setStyle(inbox)
                .setOnlyAlertOnce(true);
        Notification summary = SUM_Notification.build();
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(FIREBASE_CM_SERVICE.CHAT_NOTIFICATION,summary);
    }
}
