package com.incitorrent.radyo.menemen.pro.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.incitorrent.radyo.menemen.pro.MainActivity;
import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

/**
 * Radyo Menemen Pro Created by lutfi on 27.08.2016.
 */
public class downloadMessages extends AsyncTask<Void,Void,Void> {
    private static final String TAG = "downloadMSG";
    private Context context;
    private Menemen m;
    private Boolean is_not_flood;
    private NotificationManager notificationManager;

    public downloadMessages(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        m = new Menemen(context);
        is_not_flood = (m.getSavedTime("download_msg")) < System.currentTimeMillis();
        if(is_not_flood)
            Toast.makeText(context, R.string.toast_download_progress_msg, Toast.LENGTH_LONG).show();
        else
            Toast.makeText(context, R.string.toast_download_msg_flood_warn, Toast.LENGTH_LONG).show();
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        if(!m.isInternetAvailable()) return null;
        if(!is_not_flood) return null;
        chatDB sql = new chatDB(context,null,null,1);
        String line = Menemen.getMenemenData(RadyoMenemenPro.MESAJLAR + "&downloadall");
        try {
            JSONArray arr = new JSONObject(line).getJSONArray("mesajlar");
            JSONObject c;
            int lenght = arr.getJSONArray(0).length();
            for(int i = 0; i<lenght; i++){
                String id,nick,mesaj,zaman;
                JSONArray innerJarr = arr.getJSONArray(0);
                c = innerJarr.getJSONObject(i);
                id = c.getString("id");
                nick = c.getString("nick");
                mesaj = c.getString("post");
                zaman = c.getString("time");
                //db ye ekle
                sql.addtoHistory(new chatDB.CHAT(id,nick,mesaj,zaman));
                if((i % 10) == 0)
                    progressUpdate(lenght, i, nick + ": " + mesaj);
                Log.v(TAG,"add to history " + id + " " + nick + " progress" + i);
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if(is_not_flood) {
            downloadFinished();
            m.saveTime("download_msg",(1000*60*60*2));
        }
        super.onPostExecute(aVoid);
    }

    public void progressUpdate(int max, int progress, String s){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.mipmap.ic_chat);
        builder.setProgress(max,progress,false);
        builder.setContentTitle(context.getString(R.string.downloading_messages)).setContentText(s);
        Notification notification = builder.build();
        notificationManager.notify(RadyoMenemenPro.MSG_DOWNLOAD_PROGRESS_NOTIFICATION, notification);
    }
    public void downloadFinished(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.mipmap.ic_chat);
        builder.setAutoCancel(true);
        builder.setContentIntent(PendingIntent.getActivity(context,new Random().nextInt(200),new Intent(context, MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT));
        builder.setContentTitle(context.getString(R.string.app_name)).setContentText(context.getString(R.string.download_success));
        Notification notification = builder.build();
        notificationManager.notify(RadyoMenemenPro.MSG_DOWNLOAD_PROGRESS_NOTIFICATION, notification);
    }
}
