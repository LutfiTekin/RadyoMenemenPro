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
public class deletePost extends AsyncTask<Void,Void,Void> {
    private static final String TAG = "deletePost";
    private Context context;
    private Menemen m;

    public deletePost(Context context) {
        this.context = context;
    }
    String postid;

    public deletePost(String postid) {
        this.postid = postid;
    }

    @Override
    protected Void doInBackground(Void... params) {
        m = new Menemen(context);
        if(!m.isInternetAvailable()) return null;
        if(m.oku("logged").equals("yok")) return null;
        Map<String,String> dataToSend = new HashMap<>();
        dataToSend.put("nick", m.oku("username"));
        dataToSend.put("mkey", m.oku("mkey"));
        dataToSend.put("post", postid);
        String encodedStr = Menemen.getEncodedData(dataToSend);

        BufferedReader reader = null;

        try {
            //Converting address String to URL
            URL url = new URL(RadyoMenemenPro.MESAJ_SIL);
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
            Log.v(TAG,  line);
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
