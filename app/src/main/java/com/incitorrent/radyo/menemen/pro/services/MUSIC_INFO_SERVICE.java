package com.incitorrent.radyo.menemen.pro.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

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
            Log.v(TAG,"UpdateOnBackground");
            if(!inf.isInternetAvailable()) return null;
            BufferedReader reader = null;
            //Şarkı bilgisi kontrolü
            if(inf.oku("caliyor").equals("evet")) {
                try {
                 String line = Menemen.getMenemenData(RadyoMenemenPro.BROADCASTINFO);
                    Log.v(TAG,line);
                    JSONObject c = new JSONObject(line).getJSONArray("info").getJSONObject(0);
                    String calan = c.getString("calan");
                    inf.kaydet("calan", Menemen.radiodecodefix(calan));
                    inf.kaydet("dj", c.getString("name"));
                    inf.kaydet("djnotu", c.getString("djnotu"));
                    String songid = c.getString("songid");
                    String download = c.getString("download");
                    inf.kaydet("LASTsongid", songid);
                    if (!inf.oku(RadyoMenemenPro.SAVED_MUSIC_INFO).equals(calan)) {
                        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("download_artwork", true))
                            inf.downloadImageIfNecessary(songid, c.getString("artwork"));
                        sql.addtoHistory(new radioDB.Songs(songid, null, calan, download)); // Şarkıyı kaydet
                        inf.kaydet(RadyoMenemenPro.SAVED_MUSIC_INFO, calan);
                        Log.v(TAG,"Artwork downloaded" + c.getString("artwork"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    Log.wtf("Null", e.toString());
                }
            }
            //DJ cevabını kontrol et
            if(!inf.oku("logged").equals("yok")) {
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
            return null;
        }
    }


}
