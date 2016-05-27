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

    @Override
    protected Void doInBackground(Void... params) {
        m = new Menemen(context);
        if(!m.isInternetAvailable()) return null;
        Map<String,String> dataToSend = new HashMap<>();
        dataToSend.put("channel", "sync");
        String encodedStr = Menemen.getEncodedData(dataToSend);
        BufferedReader reader = null;

        try {
            //Converting address String to URL
            URL url = new URL(RadyoMenemenPro.SYNCCHANNEL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
            writer.write(encodedStr);
            writer.flush();
            StringBuilder sb = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            while((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            line = sb.toString();
            Log.d(TAG,"Alınan JSON:");
            Log.d(TAG, line);

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
        } finally {
            if(reader != null) {
                try {
                    reader.close();     //Closing the
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
