package com.incitorrent.radyo.menemen.pro.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;
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
            {
                new Menemen(context).kaydet(RadyoMenemenPro.IS_PODCAST,"hayır");
                dataSource = intent.getExtras().getString("dataSource");
                Toast.makeText(context, R.string.loading, Toast.LENGTH_SHORT).show();
            }
        }
        i.putExtra("dataSource", dataSource);
        context.startService(i);
    }
}
