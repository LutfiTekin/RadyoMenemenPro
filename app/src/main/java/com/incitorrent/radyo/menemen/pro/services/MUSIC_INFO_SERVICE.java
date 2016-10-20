package com.incitorrent.radyo.menemen.pro.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.incitorrent.radyo.menemen.pro.MainActivity;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;
import com.incitorrent.radyo.menemen.pro.utils.radioDB;

import org.json.JSONObject;

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
    private RequestQueue queue;
    public MUSIC_INFO_SERVICE() {
    }

    @Override
    public void onDestroy() {
        queue.stop();
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
        queue = Volley.newRequestQueue(context);
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



    private void saveTrackAndNotifyNP(String calan, String songid,  String artwork) {
        if (!inf.oku(RadyoMenemenPro.SAVED_MUSIC_INFO).equals(calan)) {
            sql.addtoHistory(new radioDB.Songs(songid, null, calan, "no url",artwork)); // Şarkıyı kaydet
            inf.kaydet(RadyoMenemenPro.SAVED_MUSIC_INFO, calan);
            Intent intent = new Intent(NP_FILTER);
            intent.putExtra("calan",calan);
            Log.v("TRACK NP",calan);
            broadcaster.sendBroadcast(intent);
        }
    }


    public class UpdateNow extends AsyncTask<String,String,Boolean>{
        @Override
        protected Boolean doInBackground(String... params) {
            if (!inf.isInternetAvailable()) return null;
                //Şarkı bilgisi kontrolü
                StringRequest stringRequest = new StringRequest(Request.Method.GET, RadyoMenemenPro.BROADCASTINFO_NEW,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject c = new JSONObject(response).getJSONArray("info").getJSONObject(0);
                                    String calan = c.getString(CALAN);
                                    inf.kaydet(CALAN, Menemen.radiodecodefix(calan));
                                    inf.kaydet(DJ, c.getString(DJ));
                                    String songid = c.getString("songid");
                                    String artwork = c.getString(ARTWORK);
                                    inf.kaydet(LAST_ARTWORK_URL, artwork);
                                    saveTrackAndNotifyNP(calan, songid, artwork);
                                }catch(Exception e){
                                    e.printStackTrace();
                                }
                            }
                        },null){
                    @Override
                    public Priority getPriority() {
                        return Priority.IMMEDIATE;
                    }
                };
                stringRequest.setRetryPolicy(new DefaultRetryPolicy(1000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                queue.add(stringRequest);
                return true;
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
