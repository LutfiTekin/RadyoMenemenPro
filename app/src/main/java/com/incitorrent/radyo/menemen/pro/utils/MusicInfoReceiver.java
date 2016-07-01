package com.incitorrent.radyo.menemen.pro.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.incitorrent.radyo.menemen.pro.services.MUSIC_INFO_SERVICE;

public class MusicInfoReceiver extends BroadcastReceiver {
    private static final String TAG = "MusicInfoReceiver" ;
    //internet bağlantısı değiştiğinde veya telefon başlatıldığında MUSIC_INFO_SERVICE i başlat

    public MusicInfoReceiver() {
    }
    Menemen m;
    @Override
    public void onReceive(Context context, Intent intent) {
        m = new Menemen(context);
//        final Boolean notify_when_onair = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifications_on_air", true);
//     if(m.isInternetAvailable() && notify_when_onair) context.startService(new Intent(context, MUSIC_INFO_SERVICE.class));
//     if(!m.isInternetAvailable() && m.oku("caliyor").equals("evet")) {
//         //internet bağlantısı kopunca duraklat
//         context.startService(new Intent(context, MUSIC_PLAY_SERVICE.class));
//         Log.v(TAG,"Radyo Çalarken bağlantı gitti");
//     }
//        Log.v(TAG,"onReceive isInternetAvailable" + m.isInternetAvailable());
        if(m.isInternetAvailable()) context.startService(new Intent(context, MUSIC_INFO_SERVICE.class));
    }
}
