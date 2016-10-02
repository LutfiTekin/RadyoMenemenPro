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

        Intent i = new Intent(context,MUSIC_PLAY_SERVICE.class);
        String dataSource = null;
        if(intent.getExtras() != null) {
            if (intent.getExtras().getBoolean("stop")) //Stop signal received from now playing notification
                dataSource = "stop";
            else if (intent.getExtras().getString("dataSource", null) != null) //dataSource received from on air notification
                dataSource = intent.getExtras().getString("dataSource");
        }
        i.putExtra("dataSource", dataSource);
        context.startService(i);
    }
}
