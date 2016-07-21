package com.incitorrent.radyo.menemen.pro.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lutfi on 20.05.2016.
 */
public class syncChannels extends AsyncTask<Void,Void,Void> {
    private static final String TAG = "syncChannels";
    private Context context;
    private Menemen m;

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
            m.kaydet(RadyoMenemenPro.CAPS_API_KEY,Jo.getString("capsapikey"));
            if(m.oku("logged").equals("evet") && m.isFirstTime("tokenset")) m.setToken();
            if(m.isFirstTime("loadmessages")) loadMSGs();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Sitedeki son 20 mesajı yükle
     */
    private void loadMSGs() {
       chatDB sql = new chatDB(context,null,null,1);
       String line = Menemen.getMenemenData(RadyoMenemenPro.MESAJLAR + "&sonmsg=1");
        try {
            JSONArray arr = new JSONObject(line).getJSONArray("mesajlar");
            JSONObject c;
            for(int i = 0;i<arr.getJSONArray(0).length();i++){
                String id,nick,mesaj,zaman;
                JSONArray innerJarr = arr.getJSONArray(0);
                c = innerJarr.getJSONObject(i);
                id = c.getString("id");
                nick = c.getString("nick");
                mesaj = c.getString("post");
                zaman = c.getString("time");
                //db ye ekle
              sql.addtoHistory(new chatDB.CHAT(id,nick,mesaj,zaman));
                Log.v(TAG,"add to history " + id + " " + nick);
            }
        }catch (JSONException e){
            m.resetFirstTime("loadmessages");
            e.printStackTrace();
        }
    }


}
