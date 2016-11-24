package com.incitorrent.radyo.menemen.pro.utils;

import android.content.Context;
import android.os.AsyncTask;

import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Created by lutfi on 20.05.2016.
 */
public class deletePost extends AsyncTask<Void,Void,Void> {
    private static final String TAG = "deletePost";
    private Context context;
    private String postid;


    public deletePost(Context context, String postid) {
        this.context = context;
        this.postid = postid;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Menemen m = new Menemen(context);
        if(!m.isInternetAvailable()) return null;
        if(!m.isLoggedIn()) return null;
        Map<String,String> dataToSend = m.getAuthMap();
        dataToSend.put("post", postid);
        BufferedReader reader = null;

        try {
            String encodedStr = Menemen.getEncodedData(dataToSend);
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
