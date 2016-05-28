package com.incitorrent.radyo.menemen.pro.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.incitorrent.radyo.menemen.pro.services.MUSIC_PLAY_SERVICE;

public class NotificationControls extends BroadcastReceiver {
    public NotificationControls() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
//TODO bazı buglar var
        Intent i = new Intent(context,MUSIC_PLAY_SERVICE.class);
        if(intent.getExtras()!=null && intent.getExtras().getBoolean("stop"))
            i.putExtra("dataSource","stop");
        context.startService(i);

    }
}
