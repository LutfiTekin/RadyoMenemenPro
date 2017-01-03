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
import android.support.annotation.Nullable;
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
import com.incitorrent.radyo.menemen.pro.show_image_comments;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class DirectReplyReceiver extends BroadcastReceiver {
    Menemen m;
    RequestQueue queue;
    public DirectReplyReceiver() {
    }

    @Override
    public void onReceive(Context context, final Intent intent) {
        queue = Volley.newRequestQueue(context.getApplicationContext());
        m = new Menemen(context);
       if(intent.getAction()!=null && getMessage(intent) != null){
           switch (intent.getAction()){
               case RadyoMenemenPro.Action.CHAT:
                   postToMenemen(String.valueOf(getMessage(intent)),context,null);
                   break;
               case RadyoMenemenPro.Action.TOPIC_MESSAGES:
                   postToMenemen(String.valueOf(getMessage(intent)),context,intent.getExtras().getString(topicDB._TOPICID,"failed"));
                   break;
               case RadyoMenemenPro.Action.CAPS:
                   postCapsComment(intent, context);
                   break;
           }
       }


    }

    void postCapsComment(final Intent intent, final Context context) {
        final String url = intent.getExtras().getString("url");
        final int caps_id = Integer.parseInt(intent.getExtras().getString("id"));
        StringRequest postRequest = new StringRequest(Request.Method.POST, RadyoMenemenPro.POST_COMMENT_CAPS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        updateCapsNotification(context, url, caps_id);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                updateCapsNotification(context, url, caps_id);
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> dataToSend = new HashMap<>();
                dataToSend.put("nick", m.getUsername());
                dataToSend.put("capsurl", url);
                dataToSend.put("comment", String.valueOf(getMessage(intent)));
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
        queue.add(postRequest);
    }

    void updateCapsNotification(Context context, String url, int caps_id) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        Intent notification_intent = new Intent(context, show_image_comments.class);
        notification_intent.putExtra("url",url);
        builder
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(context, new Random().nextInt(200), notification_intent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setContentTitle(context.getString(R.string.notification_reply_sent))
                .setContentText(context.getString(R.string.notification_see_all_comments))
                .setSmallIcon(R.drawable.default_image)
                .setOnlyAlertOnce(true);
        Notification notification = builder.build();
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(RadyoMenemenPro.CAPS_NOTIFICATION + caps_id,notification);
    }

    @Nullable
    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    private CharSequence getMessage(Intent ıntent){
        Bundle remoteInput = RemoteInput.getResultsFromIntent(ıntent);
        if(remoteInput != null){
            return remoteInput.getCharSequence(FIREBASE_CM_SERVICE.DIRECT_REPLY_KEY);
        }
        return null;
    }


    private void postToMenemen(final String msg,final Context context, @Nullable final String topicid) {
        boolean isTopic = false;
        if(topicid != null){
            if(topicid.equals("failed")) return;
            isTopic = true;
        }
        if(!m.isInternetAvailable()) {
            Toast.makeText(context.getApplicationContext(), R.string.toast_internet_warn, Toast.LENGTH_SHORT).show();
            return;
        }
        StringRequest post = new StringRequest(Request.Method.POST,
                (isTopic) ? RadyoMenemenPro.MENEMEN_TOPICS_POST : RadyoMenemenPro.MESAJ_GONDER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        updateChatNotification(context, msg, topicid);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                updateChatNotification(context, msg, topicid);
              error.printStackTrace();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> dataToSend = m.getAuthMap();
                dataToSend.put("mesaj", msg);
                if(topicid != null)
                    dataToSend.put(topicDB._TOPICID,topicid);
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

    void updateChatNotification(Context context, String msg, @Nullable String topicid) {
        boolean isTopic = topicid != null;
        NotificationCompat.MessagingStyle inbox = new NotificationCompat.MessagingStyle(context.getString(R.string.me));
        try {
            chatDB sql = m.getChatDB();
            topicDB Tsql = m.getTopicDB();
            DateFormat df = new SimpleDateFormat(RadyoMenemenPro.CHAT_DATE_FORMAT, Locale.US);
            Cursor cursor =
                    (isTopic) ?
                            Tsql.getTopicMessagesById(m.oku(RadyoMenemenPro.LAST_ID_SEEN_ON_TOPIC + topicid),topicid)
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
            if(isTopic)
                Tsql.close();
            else
                sql.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        inbox.addMessage(msg,System.currentTimeMillis(),null);

        NotificationCompat.Builder SUM_Notification = new NotificationCompat.Builder(context);
        Intent notification_intent = new Intent(context, MainActivity.class);
        notification_intent.setAction(RadyoMenemenPro.Action.CHAT);
        if(isTopic) {
            notification_intent.setAction(RadyoMenemenPro.Action.TOPIC_MESSAGES);
            notification_intent.putExtra(topicDB._TOPICID,topicid);
        }
        inbox.setConversationTitle((isTopic) ? m.getTopicDB().getTopicInfo(topicid,topicDB._TITLE) : context.getString(R.string.notification_new_msg));
        SUM_Notification
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(context, new Random().nextInt(200), notification_intent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setContentTitle((isTopic) ? m.getTopicDB().getTopicInfo(topicid,topicDB._TITLE) : context.getString(R.string.notification_new_msg))
                .setSmallIcon((isTopic) ? R.drawable.ic_topic_discussion : R.mipmap.ic_chat)
                .setStyle(inbox)
                .setOnlyAlertOnce(true);
        Notification summary = SUM_Notification.build();
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        try {
            notificationManagerCompat.notify((isTopic) ? FIREBASE_CM_SERVICE.CHAT_NOTIFICATION + Integer.parseInt(topicid) : FIREBASE_CM_SERVICE.CHAT_NOTIFICATION,summary);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }
}
