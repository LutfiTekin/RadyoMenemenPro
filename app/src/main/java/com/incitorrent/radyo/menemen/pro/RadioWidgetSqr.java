package com.incitorrent.radyo.menemen.pro;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.incitorrent.radyo.menemen.pro.services.MUSIC_INFO_SERVICE;
import com.incitorrent.radyo.menemen.pro.services.MUSIC_PLAY_SERVICE;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;
import com.incitorrent.radyo.menemen.pro.utils.NotificationControls;

/**
 * Implementation of App Widget functionality.
 */
public class RadioWidgetSqr extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        Menemen m = new Menemen(context.getApplicationContext());
        Boolean isPlaying = m.isPlaying() && m.isServiceRunning(MUSIC_PLAY_SERVICE.class);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.radio_widget_sqr);
        //Go to radio/track info fragment
        Intent radiointent = new Intent(context, MainActivity.class);
        radiointent.setAction(RadyoMenemenPro.Action.RADIO);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, radiointent, 0);
        views.setOnClickPendingIntent(R.id.wlastp, pendingIntent);
        radiointent.setAction(RadyoMenemenPro.Action.TRACK_INFO_LAST);
        pendingIntent = PendingIntent.getActivity(context , 0, radiointent, 0);
        views.setOnClickPendingIntent(R.id.artwork, pendingIntent);
        //Play/Stop radio
        Intent controls = new Intent(context, NotificationControls.class);
        controls.setAction(RadyoMenemenPro.Action.WIDGET_PLAY);
        PendingIntent play = PendingIntent.getBroadcast(context.getApplicationContext(),1,controls,0);
        views.setOnClickPendingIntent(R.id.wplay, play);
        controls.setAction(RadyoMenemenPro.Action.WIDGET_STOP);
        PendingIntent stop = PendingIntent.getBroadcast(context.getApplicationContext(),2,controls,0);
        views.setOnClickPendingIntent(R.id.wstop, stop);
        if(m.oku(RadyoMenemenPro.broadcastinfo.CALAN)!=null)
            views.setTextViewText(R.id.trackname, Menemen.fromHtmlCompat(m.oku(RadyoMenemenPro.broadcastinfo.CALAN)));
        //Load image
        if(!m.oku(MUSIC_INFO_SERVICE.LAST_ARTWORK_URL).equals("default") && PreferenceManager.getDefaultSharedPreferences(context).getBoolean("download_artwork",true) && m.isConnectionFast())
            try {
                Glide.with(context.getApplicationContext())
                        .load(m.oku(MUSIC_INFO_SERVICE.LAST_ARTWORK_URL))
                        .asBitmap()
                        .override(RadyoMenemenPro.ARTWORK_IMAGE_OVERRIDE_DIM,RadyoMenemenPro.ARTWORK_IMAGE_OVERRIDE_DIM)
                        .error(R.mipmap.album_placeholder)
                        .into(new AppWidgetTarget(context.getApplicationContext(),views,R.id.artwork,appWidgetId));
            } catch (Exception e) {
                e.printStackTrace();
            }
        else views.setImageViewResource(R.id.artwork, R.mipmap.album_placeholder);
        int button_state = R.drawable.ic_play_arrow_white_36dp;
        if(isPlaying) button_state = R.drawable.ic_pause_white_36dp;
        views.setImageViewResource(R.id.wplay, button_state);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        new Menemen(context).bool_kaydet(RadyoMenemenPro.SQUARE_RADIO_WIDGET,true);
    }

    @Override
    public void onDisabled(Context context) {
        new Menemen(context).bool_kaydet(RadyoMenemenPro.SQUARE_RADIO_WIDGET,false);
    }
}

