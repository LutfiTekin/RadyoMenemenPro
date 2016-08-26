package com.incitorrent.radyo.menemen.pro.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.incitorrent.radyo.menemen.pro.MainActivity;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;
import com.incitorrent.radyo.menemen.pro.utils.radioDB;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.incitorrent.radyo.menemen.pro.RadyoMenemenPro.broadcastinfo.ARTWORK;
import static com.incitorrent.radyo.menemen.pro.RadyoMenemenPro.broadcastinfo.CALAN;
import static com.incitorrent.radyo.menemen.pro.RadyoMenemenPro.broadcastinfo.DJ;

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
    Intent notification_intent;
    NotificationManager nm;
    LocalBroadcastManager broadcasterForUi;
    ScheduledThreadPoolExecutor exec;
    public MUSIC_INFO_SERVICE() {
    }

    @Override
    public void onDestroy() {
        exec.shutdown();
        Log.v(TAG,"Destroy called");
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notification_intent = new Intent(context, MainActivity.class);
        notification_intent.setAction("radyo.menemen.chat");
        broadcasterForUi = LocalBroadcastManager.getInstance(this);
        inf = new Menemen(context);
        sql = new radioDB(context,null,null,1);

        exec = new ScheduledThreadPoolExecutor(1);
        exec.scheduleAtFixedRate(new Runnable() {
            public void run() {
                Log.v(TAG,"Update");
                    new UpdateOnBackground().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }, 0, RadyoMenemenPro.MUSIC_INFO_SERVICE_INTERVAL, TimeUnit.SECONDS); // execute every ** seconds
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        Log.v(TAG, "ID: " +startId);
        new UpdateNow().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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


            //yayın bildirimi ayar kapalı olduğunda şarkı kontrolünü radyo çaldığı sırada yap
//            Boolean shouldcheck = isPlaying || (notify_when_onair && notify);
//            Log.v(TAG, " Should check " + shouldcheck + " isPlaying " + isPlaying + " notifyonair " + notify_when_onair + " notify " + notify );
            try {
                if (inf.isPlaying()) {
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
                        if (!inf.oku(RadyoMenemenPro.SAVED_MUSIC_INFO).equals(calan)) {
                            sql.addtoHistory(new radioDB.Songs(songid, null, calan, download,artwork)); // Şarkıyı kaydet
                            inf.kaydet(RadyoMenemenPro.SAVED_MUSIC_INFO, calan);
                            notifyNP();
                        }

                }
            }
//            catch (JSONException | InterruptedException | ExecutionException e) {
//                e.printStackTrace();
//            }
            catch (Exception e){
                Log.v(TAG,"ERROR" + e.toString());
            }


            return null;
        }
    }


    public class UpdateNow extends AsyncTask<String,String,String>{
        @Override
        protected String doInBackground(String... params) {
            Log.v(TAG, "UpdateNOW");
            if (!inf.isInternetAvailable()) return null;
            BufferedReader reader = null;

            try {

                //Şarkı bilgisi kontrolü

                String line = Menemen.getMenemenData(RadyoMenemenPro.BROADCASTINFO);
                Log.v(TAG, line);
                JSONObject c = new JSONObject(line).getJSONArray("info").getJSONObject(0);
                String calan = c.getString(CALAN);
                inf.kaydet(CALAN, Menemen.radiodecodefix(calan));
                inf.kaydet(DJ, c.getString(DJ));
                String artwork = c.getString(ARTWORK);
                inf.kaydet(LAST_ARTWORK_URL, artwork);
                    notifyNP();
            }

            catch (Exception e){
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
