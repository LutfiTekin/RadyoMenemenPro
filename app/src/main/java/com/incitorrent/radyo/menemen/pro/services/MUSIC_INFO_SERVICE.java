package com.incitorrent.radyo.menemen.pro.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.incitorrent.radyo.menemen.pro.MainActivity;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;
import com.incitorrent.radyo.menemen.pro.utils.radioDB;

import org.json.JSONObject;

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
    public static final  String SERVICE_FILTER = "com.incitorrent.radyo.menemen.PLAYERSERVICE";
    public static final String LAST_ARTWORK_URL = "lastartwork";
    final Context context = this;
    Menemen inf;
    radioDB sql;
    Intent notification_intent;
    NotificationManager nm;
    LocalBroadcastManager broadcaster;
    ScheduledThreadPoolExecutor exec;
    public MUSIC_INFO_SERVICE() {
    }

    @Override
    public void onDestroy() {
        exec.shutdown();
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notification_intent = new Intent(context, MainActivity.class);
        notification_intent.setAction("radyo.menemen.chat");
        broadcaster = LocalBroadcastManager.getInstance(this);
        inf = new Menemen(context);
        sql = new radioDB(context,null,null,1);

        exec = new ScheduledThreadPoolExecutor(1);
        exec.scheduleAtFixedRate(new Runnable() {
            public void run() {
                new UpdateOnBackground().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
            }
        }, 10, RadyoMenemenPro.MUSIC_INFO_SERVICE_INTERVAL, TimeUnit.SECONDS); // execute every ** seconds
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        new UpdateNow().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    public class UpdateOnBackground extends AsyncTask<String,String,String>{
        @Override
        protected String doInBackground(String... params) {
            if (!inf.isInternetAvailable()) return null;
            try {
                if (inf.isPlaying()) {
                    //Şarkı bilgisi kontrolü
                        String line = Menemen.getMenemenData(RadyoMenemenPro.BROADCASTINFO_NEW);
                        JSONObject c = new JSONObject(line).getJSONArray("info").getJSONObject(0);
                        String calan = c.getString(CALAN);
                        inf.kaydet(CALAN, Menemen.radiodecodefix(calan));
                        inf.kaydet(DJ, c.getString(DJ));
                        String songid = c.getString("songid");
                        String artwork = c.getString(ARTWORK);
                        inf.kaydet(LAST_ARTWORK_URL, artwork);
                        saveTrackAndNotifyNP(calan, songid, artwork);
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }


            return null;
        }
    }

    private void saveTrackAndNotifyNP(String calan, String songid,  String artwork) {
        if (!inf.oku(RadyoMenemenPro.SAVED_MUSIC_INFO).equals(calan)) {
            sql.addtoHistory(new radioDB.Songs(songid, null, calan, "no url",artwork)); // Şarkıyı kaydet
            inf.kaydet(RadyoMenemenPro.SAVED_MUSIC_INFO, calan);
            Intent intent = new Intent(NP_FILTER);
            broadcaster.sendBroadcast(intent);
        }
    }


    public class UpdateNow extends AsyncTask<String,String,Boolean>{
        @Override
        protected Boolean doInBackground(String... params) {
            if (!inf.isInternetAvailable()) return null;

            try {
                //Şarkı bilgisi kontrolü
                String line = Menemen.getMenemenData(RadyoMenemenPro.BROADCASTINFO_NEW);
                JSONObject c = new JSONObject(line).getJSONArray("info").getJSONObject(0);
                String calan = c.getString(CALAN);
                inf.kaydet(CALAN, Menemen.radiodecodefix(calan));
                inf.kaydet(DJ, c.getString(DJ));
                String artwork = c.getString(ARTWORK);
                inf.kaydet(LAST_ARTWORK_URL, artwork);
                String songid = c.getString("songid");
                saveTrackAndNotifyNP(calan,songid,artwork);
                return true;
            }catch (Exception e){
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(result) {
                Intent intent = new Intent(SERVICE_FILTER);
                intent.putExtra("action","update");
                broadcaster.sendBroadcast(intent);
            }

            super.onPostExecute(result);
        }
    }


}
