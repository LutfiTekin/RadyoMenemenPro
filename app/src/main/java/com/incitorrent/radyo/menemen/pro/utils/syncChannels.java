package com.incitorrent.radyo.menemen.pro.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessaging;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by lutfi on 20.05.2016.
 */
public class syncChannels extends AsyncTask<Void,Void,Void> {
    private static final String TAG = "syncChannels";
    private Context context;
    private Menemen m;
    private RequestQueue queue;
    /**
     * Kanal değişimi durumlarında güncelleme gereksinimini
     *ortadan kaldırmak için sürekli olara site ile iletişim kurup kanalları çeken class
     * @param context
     */
    public syncChannels(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... params) {
        m = new Menemen(context);
        queue = Volley.newRequestQueue(context);
        if(!m.isInternetAvailable()) return null;
            StringRequest stringRequest = new StringRequest(Request.Method.GET, RadyoMenemenPro.SYNCCHANNEL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject J = new JSONObject(response);
                                JSONArray JARR = J.getJSONArray("info");
                                JSONObject Jo = JARR.getJSONObject(0);
                                m.kaydet(RadyoMenemenPro.LOW_CHANNEL,Jo.getString(RadyoMenemenPro.LOW_CHANNEL));
                                m.kaydet(RadyoMenemenPro.MID_CHANNEL,Jo.getString(RadyoMenemenPro.MID_CHANNEL));
                                m.kaydet(RadyoMenemenPro.HIGH_CHANNEL,Jo.getString(RadyoMenemenPro.HIGH_CHANNEL));
                                m.kaydet(RadyoMenemenPro.RADIO_SERVER,Jo.getString("server"));
                                m.kaydet(RadyoMenemenPro.CAPS_API_KEY,Jo.getString("capsapikey"));
                                if(m.isLoggedIn()) {
                                    if(m.isFirstTime("tokenset"))
                                        m.setToken();
                                }
                                syncTopics();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    },null);
        stringRequest.setRetryPolicy(m.menemenRetryPolicy());
        queue.add(stringRequest);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Log.d(TAG,"Syncronized");
        super.onPostExecute(aVoid);
    }

    private void syncTopics() {
        try {
            FirebaseMessaging.getInstance().subscribeToTopic("news");
            FirebaseMessaging.getInstance().subscribeToTopic("sync");
            FirebaseMessaging.getInstance().subscribeToTopic("onair");
            FirebaseMessaging.getInstance().subscribeToTopic("podcast");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
