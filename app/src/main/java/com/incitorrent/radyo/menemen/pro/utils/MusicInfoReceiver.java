package com.incitorrent.radyo.menemen.pro.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MusicInfoReceiver extends BroadcastReceiver {

    public MusicInfoReceiver() {
    }
    Menemen m;
    @Override
    public void onReceive(Context context, Intent intent) {
        m = new Menemen(context);
        if(m.isConnectedWifi())
            new syncChannels(context).execute(); //Güç kablosu bağlı ve wifi açık-> senkronize et
    }
}
