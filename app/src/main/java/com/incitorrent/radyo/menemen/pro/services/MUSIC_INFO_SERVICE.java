package com.incitorrent.radyo.menemen.pro.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.incitorrent.radyo.menemen.pro.MainActivity;
import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;
import com.incitorrent.radyo.menemen.pro.utils.radioDB;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.incitorrent.radyo.menemen.pro.RadyoMenemenPro.broadcastinfo.*;

/**
 * Created by lutfi on 22.05.2016.
 */
public class MUSIC_INFO_SERVICE extends Service {

    public static final String TAG = "MUSIC_INFO_SERVICE";
    public static final  String NP_FILTER = "com.incitorrent.radyo.menemen.NPUPDATE"; //NowPlaying - şimdi çalıyor kutusunu güncelle
    public static final String LAST_ARTWORK_URL = "lastartwork";
    public static final String LAST_MSG = "&print_last_msg";
    public static final String LAST_USER = "&print_last_user";
    public static final int CHAT_NOTIFICATON = 8888;
    final Context context = this;
    Menemen inf;
    radioDB sql;
    NotificationCompat.Builder notification;
    Intent notififcation_intent;
    NotificationManager nm;
    LocalBroadcastManager broadcasterForUi;
    public MUSIC_INFO_SERVICE() {
    }

    @Override
    public void onDestroy() {
        Log.v(TAG,"Destroy called");
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notififcation_intent = new Intent(context, MainActivity.class);
        notififcation_intent.setAction("radyo.menemen.chat");
        broadcasterForUi = LocalBroadcastManager.getInstance(this);
        inf = new Menemen(context);
        sql = new radioDB(context,null,null,1);
        final ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
        exec.scheduleAtFixedRate(new Runnable() {
            public void run() {
                Log.v(TAG,"Update");
                    new UpdateOnBackground().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }, 0, RadyoMenemenPro.MUSIC_SERVICE_INFO_INTERVAL, TimeUnit.SECONDS); // execute every ** seconds
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        Log.v(TAG, "ID: " +startId);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    public class UpdateOnBackground extends AsyncTask<String,String,String>{
        @Override
        protected String doInBackground(String... params) {
            Log.v(TAG, "UpdateOnBackground");
            if (!inf.isInternetAvailable()) return null;
            BufferedReader reader = null;
            final Boolean notify = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifications", true);
            final Boolean notify_when_onair = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifications_on_air", true);
            final Boolean isPlaying = inf.oku("caliyor").equals("evet");

            //yayın bildirimi ayar kapalı olduğunda şarkı kontrolünü radyo çaldığı sırada yap
            Boolean shouldcheck = isPlaying || (notify_when_onair && notify);
            Log.v(TAG, " Should check " + shouldcheck + " isPlaying " + isPlaying + " notifyonair " + notify_when_onair + " notify " + notify );
            try {
                if (shouldcheck) {
                    //Şarkı bilgisi kontrolü

                        String line = Menemen.getMenemenData(RadyoMenemenPro.BROADCASTINFO);
                        Log.v(TAG, line);
                        JSONObject c = new JSONObject(line).getJSONArray("info").getJSONObject(0);
                        String calan = c.getString(CALAN);
                        inf.kaydet(CALAN, Menemen.radiodecodefix(calan));
                        inf.kaydet(DJ, c.getString(DJ));
                        String songid = c.getString("songid");
                        String download = "no url";//artık indirme yok
                        String artwork = c.getString(ARTWORK);
                        inf.kaydet(LAST_ARTWORK_URL, artwork);
                        if (isPlaying && !inf.oku(RadyoMenemenPro.SAVED_MUSIC_INFO).equals(calan)) {
                            sql.addtoHistory(new radioDB.Songs(songid, null, calan, download,artwork)); // Şarkıyı kaydet
                            inf.kaydet(RadyoMenemenPro.SAVED_MUSIC_INFO, calan);
                            notifyNP();
                        }

                }

                //Son olan biteni al

                String lastob = new JSONObject(Menemen.getMenemenData(RadyoMenemenPro.OLAN_BITEN)).getJSONArray("olan_biten").getJSONArray(0).getJSONObject(0).getString("time");
                inf.kaydet(RadyoMenemenPro.LASTOB,lastob);
                Log.v(TAG,"LASTOB" + lastob);

                //Son olan biteni al END
                //TODO DJ cevabını kontrol et
//            if (!inf.oku("logged").equals("yok")) {
//                Map<String, String> dataToSend = new HashMap<>();
//                dataToSend.put("nick", inf.oku("username"));
//                dataToSend.put("mkey", inf.oku("mkey"));
//                String encodedStr = Menemen.getEncodedData(dataToSend);
//                try {
//                    HttpURLConnection connection = (HttpURLConnection) new URL(RadyoMenemenPro.DJRESPONSE).openConnection();
//                    connection.setRequestMethod("POST");
//
//                    connection.setDoOutput(true);
//                    OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
//                    writer.write(encodedStr);
//                    writer.flush();
//                    StringBuilder sb = new StringBuilder();
//                    reader = new BufferedReader(new InputStreamReader(
//                            connection.getInputStream(), "iso-8859-9"), 8);
//                    String line;
//                    while ((line = reader.readLine()) != null) {
//                        sb.append(line + "\n");
//                    }
//                    line = sb.toString();
//                    JSONObject j = new JSONObject(line).getJSONArray("djresp").getJSONObject(0);

//                    Log.v(TAG, "DJRESP \n" + j.getString("id")+ "\n" + j.getString("cevap"));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
                //yayın başlayınca bildirim at
                //Bildirim ayarı açık mı? , Yayında Oto Dj mi var ?, Radyo zaten çalmıyor mu?
                Boolean isOnair = notify_when_onair && !inf.oku("dj").equals(RadyoMenemenPro.OTO_DJ) && !inf.oku("dj").equals("yok") && !inf.oku(RadyoMenemenPro.SAVED_DJ).equals(inf.oku("dj")) && !isPlaying;
                if (isOnair) {
                    notification = new NotificationCompat.Builder(context);

                        notification.setContentTitle(getString(R.string.notification_onair_title))
                                .setContentText(inf.oku(DJ) + getString(R.string.notification_onair_content))
                                .setSmallIcon(R.drawable.ic_on_air)
                                .setLargeIcon(Glide.with(MUSIC_INFO_SERVICE.this).load(R.mipmap.ic_launcher).asBitmap().into(100,100).get());
                        //Main activity yi aç
                        notification.setContentIntent(PendingIntent.getActivity(context, new Random().nextInt(200),notififcation_intent, PendingIntent.FLAG_CANCEL_CURRENT));
                    if(PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null) != null)  notification.setSound(Uri.parse(PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null)));
                    if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifications_on_air_vibrate", true))
                        notification.setVibrate(new long[]{500, 500, 500});
                    notification.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                    notification.setAutoCancel(true);

                    nm.notify(RadyoMenemenPro.ON_AIR_NOTIFICATION, notification.build());
                    inf.kaydet(RadyoMenemenPro.SAVED_DJ, inf.oku("dj")); //önceki djyi kaydet
                    Log.v(TAG, " Notification built");

                }

                //Mesaj bildirimi
                final Boolean notify_new_post = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifications_chat", false);
                final Boolean music_only = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("music_only",false);
                final Boolean is_chat_foreground = inf.bool_oku(RadyoMenemenPro.IS_CHAT_FOREGROUND);
                Log.v(RadyoMenemenPro.IS_CHAT_FOREGROUND,"?? " + is_chat_foreground + " notify " + (notify_new_post && !music_only && notify && !is_chat_foreground));
                if(notify_new_post && !music_only && notify && !is_chat_foreground){
                    //TODO sohbete yeni mesaj gelince bildir
                    final String GROUP_KEY_CHAT = "group_key_chat";

                    //Kendi mesajına bildirim gösterme
                    String local_user = inf.oku("username").trim();
                    String remote_user = Menemen.getMenemenData(RadyoMenemenPro.MESAJLAR + LAST_USER).trim();
                   if(local_user.equals(remote_user))
                        return null;
                    String line;
                    String lastmsg = Menemen.getMenemenData(RadyoMenemenPro.MESAJLAR + LAST_MSG);
                    String saved_lastmsg = inf.oku("last_msg_not");
                    if(lastmsg.equals(saved_lastmsg)) return null;

                    line = Menemen.getMenemenData(RadyoMenemenPro.MESAJLAR + "&sonmsg=" + saved_lastmsg + "&orderasc");
                    inf.kaydet("last_msg_not",lastmsg);
                    Log.v(TAG, "LINE " + line + " lastmsg" + lastmsg + " savedlast " + saved_lastmsg);
                    if(line==null) return null;
                    if(line.equals("yok")) return null;

                        JSONArray arr = new JSONObject(line).getJSONArray("mesajlar");
                        JSONObject c;
                        if(arr.getJSONArray(0).length()<1) return null;
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                        builder.setContentTitle(String.format(getString(R.string.notification_new_message), arr.getJSONArray(0).length()));
                        builder.setSmallIcon(R.mipmap.ic_chat);
                        builder.setAutoCancel(true);
                        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();

                        for(int i = 0;i<arr.getJSONArray(0).length();i++){
                            String nick,mesaj;
                            JSONArray innerJarr = arr.getJSONArray(0);
                            c = innerJarr.getJSONObject(i);
                            nick = c.getString("nick");
                            mesaj = c.getString("post");
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
                                mesaj = Html.fromHtml(mesaj, Html.FROM_HTML_MODE_LEGACY).toString();
                            else mesaj = Html.fromHtml(mesaj).toString();
                            //TODO stack notification
                            style.addLine(String.format("%s: %s", nick, mesaj));

                        }
                        if(PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null) != null)                            builder.setSound(Uri.parse(PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null)));
                        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifications_on_air_vibrate", true))
                            builder.setVibrate(new long[]{500, 500, 500});
                        style.setBigContentTitle(String.format(getString(R.string.notification_new_message), arr.getJSONArray(0).length()));
                        builder.setStyle(style);
                        builder.setContentIntent(PendingIntent.getActivity(context, new Random().nextInt(200), notififcation_intent, PendingIntent.FLAG_CANCEL_CURRENT));
                        builder.setGroup(GROUP_KEY_CHAT);
                        builder.setGroupSummary(true);
                        builder.setAutoCancel(true);
                        Notification summaryNotification = builder.build();

                        nm.notify(CHAT_NOTIFICATON, summaryNotification);

                }
                //Mesaj bildirimi END
            } catch (JSONException | InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }catch (Exception e){
                Log.v(TAG,"ERROR" + e.toString());
            }


            return null;
        }
    }

    public void notifyNP() {
        Intent intent = new Intent(NP_FILTER);
        broadcasterForUi.sendBroadcast(intent);
    }
}
