package com.incitorrent.radyo.menemen.pro.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
        if(!m.isInternetAvailable())
            Toast.makeText(context, R.string.toast_internet_warn, Toast.LENGTH_LONG).show();
        if (intent.getAction() != null && intent.getAction().equals(RadyoMenemenPro.Action.WIDGET_PLAY)) {
            m.bool_kaydet(RadyoMenemenPro.IS_PODCAST, false);
            dataSource = m.getRadioDataSource();
            if(m.isPlaying()) dataSource = null;
        }else if(intent.getAction() != null && intent.getAction().equals(RadyoMenemenPro.Action.WIDGET_STOP)){
            dataSource = "stop";
        }else{
        if (intent.getExtras() != null) {
            if (intent.getExtras().getBoolean("stop")) //Stop signal received from now playing notification
                dataSource = "stop";
            else if (intent.getExtras().getString(RadyoMenemenPro.DATA_SOURCE, null) != null) //dataSource received from on air notification
            {
                m.bool_kaydet(RadyoMenemenPro.IS_PODCAST, false);
                dataSource = (intent.getExtras().getString(RadyoMenemenPro.DATA_SOURCE));
                Toast.makeText(context, R.string.loading, Toast.LENGTH_SHORT).show();
            }
        }
    }
        i.putExtra(RadyoMenemenPro.DATA_SOURCE, dataSource);
        context.startService(i);
    }
}
