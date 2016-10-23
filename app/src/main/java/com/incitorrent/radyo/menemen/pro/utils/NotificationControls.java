package com.incitorrent.radyo.menemen.pro.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;
import com.incitorrent.radyo.menemen.pro.services.MUSIC_PLAY_SERVICE;

public class NotificationControls extends BroadcastReceiver {
    Menemen m;
    public NotificationControls() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        m = new Menemen(context);
        Intent i = new Intent(context, MUSIC_PLAY_SERVICE.class);
        String dataSource = null;
        if (intent.getAction() != null && intent.getAction().equals("radyo.menemen.widget.play")) {
            String selected_channel = m.oku(PreferenceManager.getDefaultSharedPreferences(context).getString("radio_channel",RadyoMenemenPro.HIGH_CHANNEL));
            dataSource = "http://" + m.oku(RadyoMenemenPro.RADIO_SERVER) + ":" + selected_channel +  "/";
            if(m.isPlaying()) dataSource = null;
        }else if(intent.getAction() != null && intent.getAction().equals("radyo.menemen.widget.stop")){
            dataSource = "stop";
        }else{
        if (intent.getExtras() != null) {
            if (intent.getExtras().getBoolean("stop")) //Stop signal received from now playing notification
                dataSource = "stop";
            else if (intent.getExtras().getString("dataSource", null) != null) //dataSource received from on air notification
            {
                m.kaydet(RadyoMenemenPro.IS_PODCAST, "hayır");
                dataSource = (intent.getExtras().getString("dataSource"));
                Toast.makeText(context, R.string.loading, Toast.LENGTH_SHORT).show();
            }
        }
    }
        i.putExtra("dataSource", dataSource);
        context.startService(i);
    }
}
