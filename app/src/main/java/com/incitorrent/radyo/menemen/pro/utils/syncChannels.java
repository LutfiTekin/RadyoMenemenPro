package com.incitorrent.radyo.menemen.pro.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lutfi on 20.05.2016.
 */
public class syncChannels extends AsyncTask<Void,Void,Void> {
    private static final String TAG = "syncChannels";
    private Context context;
    private Menemen m;

    public syncChannels(Context context) {
        this.context = context;
    }
/*Kanal değişimi durumlarında güncelleme gereksinimini
 ortadan kaldırmak için sürekli olara site ile iletişim kurup kanalları çeken class*/
    @Override
    protected Void doInBackground(Void... params) {
        m = new Menemen(context);
        if(!m.isInternetAvailable()) return null;
        try {
            String line = Menemen.getMenemenData(RadyoMenemenPro.SYNCCHANNEL);
            Log.d(TAG, "Alınan JSON:\n"+ line);
            JSONObject J = new JSONObject(line);
            JSONArray JARR = J.getJSONArray("info");
            JSONObject Jo = JARR.getJSONObject(0);
            Log.v("SERVER",Jo.getString(RadyoMenemenPro.LOW_CHANNEL));
            m.kaydet(RadyoMenemenPro.LOW_CHANNEL,Jo.getString(RadyoMenemenPro.LOW_CHANNEL));
            m.kaydet(RadyoMenemenPro.MID_CHANNEL,Jo.getString(RadyoMenemenPro.MID_CHANNEL));
            m.kaydet(RadyoMenemenPro.HIGH_CHANNEL,Jo.getString(RadyoMenemenPro.HIGH_CHANNEL));
            m.kaydet(RadyoMenemenPro.RADIO_SERVER,Jo.getString("server"));

        } catch (final Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
