package com.incitorrent.radyo.menemen.pro.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.incitorrent.radyo.menemen.pro.MainActivity;
import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;
import com.incitorrent.radyo.menemen.pro.utils.radioDB;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by lutfi on 22.05.2016.
 */
public class MUSIC_INFO_SERVICE extends Service {

    final String TAG = "MUSIC_INFO_SERVICE";
    final Context context = this;
    Menemen inf;
    radioDB sql;
    NotificationCompat.Builder notification;
    NotificationManager nm;
    public MUSIC_INFO_SERVICE() {
    }

    @Override
    public void onDestroy() {
        Log.v(TAG,"Destroy called");
        super.onDestroy();
    }

    @Override
    public void onCreate() {
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
        return START_NOT_STICKY;
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
            final Boolean notify_when_onair = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifications_on_air", true);
            final Boolean isPlaying = inf.oku("caliyor").equals("evet");

            //yayın bildirimi ayar kapalı olduğunda şarkı kontrolünü radyo çaldığı sırada yap
            Boolean shouldcheck = isPlaying || notify_when_onair;
            //Log.v(TAG, " Should check " + shouldcheck + " isPlaying " + isPlaying + " notify " + notify_when_onair );
            if (shouldcheck) {
                //Şarkı bilgisi kontrolü
                try {
                    String line = Menemen.getMenemenData(RadyoMenemenPro.BROADCASTINFO);
                    Log.v(TAG, line);
                    JSONObject c = new JSONObject(line).getJSONArray("info").getJSONObject(0);
                    String calan = c.getString("calan");
                    inf.kaydet("calan", Menemen.radiodecodefix(calan));
                    inf.kaydet("dj", c.getString("name"));
                    inf.kaydet("djnotu", c.getString("djnotu"));
                    String songid = c.getString("songid");
                    String download = c.getString("download");
                    inf.kaydet("LASTsongid", songid);
                    if (isPlaying && !inf.oku(RadyoMenemenPro.SAVED_MUSIC_INFO).equals(calan)) {
                        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("download_artwork", true))
                            inf.downloadImageIfNecessary(songid, c.getString("artwork"));
                        sql.addtoHistory(new radioDB.Songs(songid, null, calan, download)); // Şarkıyı kaydet
                        inf.kaydet(RadyoMenemenPro.SAVED_MUSIC_INFO, calan);
                        Log.v(TAG, "Artwork downloaded" + c.getString("artwork"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    Log.wtf("Null", e.toString());
                }
            }

            //DJ cevabını kontrol et
            if (!inf.oku("logged").equals("yok")) {
                Map<String, String> dataToSend = new HashMap<>();
                dataToSend.put("nick", inf.oku("username"));
                dataToSend.put("mkey", inf.oku("mkey"));
                String encodedStr = Menemen.getEncodedData(dataToSend);
                try {
                    HttpURLConnection connection = (HttpURLConnection) new URL(RadyoMenemenPro.DJRESPONSE).openConnection();
                    connection.setRequestMethod("POST");

                    connection.setDoOutput(true);
                    OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                    writer.write(encodedStr);
                    writer.flush();
                    StringBuilder sb = new StringBuilder();
                    reader = new BufferedReader(new InputStreamReader(
                            connection.getInputStream(), "iso-8859-9"), 8);
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    line = sb.toString();
                    JSONObject j = new JSONObject(line).getJSONArray("djresp").getJSONObject(0);

//                    Log.v(TAG, "DJRESP \n" + j.getString("id")+ "\n" + j.getString("cevap"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //yayın başlayınca bildirim at
            //Bildirim ayarı açık mı? , Yayında Oto Dj mi var ?, Radyo zaten çalmıyor mu?
            Boolean isOnair = notify_when_onair && !inf.oku("dj").equals(RadyoMenemenPro.OTO_DJ) && !inf.oku("dj").equals("yok") && !inf.oku(RadyoMenemenPro.SAVED_DJ).equals(inf.oku("dj")) && !isPlaying;
            if (isOnair) {
                notification = new NotificationCompat.Builder(context);
                notification.setContentTitle(getString(R.string.notification_onair_title))
                        .setContentText(inf.oku("dj") + getString(R.string.notification_onair_content))
                        .setSubText(inf.oku("djnotu"))
                        .setSmallIcon(R.drawable.ic_on_air);
                      if(PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null) != null)  notification.setSound(Uri.parse(PreferenceManager.getDefaultSharedPreferences(context).getString("notifications_on_air_ringtone", null)));
                //Main activity yi aç
                notification.setContentIntent(PendingIntent.getActivity(context, new Random().nextInt(200), new Intent(context, MainActivity.class), PendingIntent.FLAG_CANCEL_CURRENT));
                if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifications_on_air_vibrate", true))
                    notification.setVibrate(new long[]{500, 500, 500});
                notification.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                nm.notify(RadyoMenemenPro.ON_AIR_NOTIFICATION, notification.build());
                inf.kaydet(RadyoMenemenPro.SAVED_DJ, inf.oku("dj")); //önceki djyi kaydet
                Log.v(TAG, " Notification built");
            }

            return null;
        }
    }


}
