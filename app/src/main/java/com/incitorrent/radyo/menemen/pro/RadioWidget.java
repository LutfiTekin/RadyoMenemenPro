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
import com.incitorrent.radyo.menemen.pro.utils.Menemen;
import com.incitorrent.radyo.menemen.pro.utils.NotificationControls;

/**
 * Implementation of App Widget functionality.
 */
public class RadioWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        Menemen m = new Menemen(context.getApplicationContext());
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.radio_widget);
        //Go to radio/track info fragment
        Intent radiointent = new Intent(context, MainActivity.class);
        radiointent.setAction("radyo.menemen.play");
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, radiointent, 0);
        views.setOnClickPendingIntent(R.id.wlastp, pendingIntent);
        radiointent.setAction("radyo.menemen.track.info.last");
        pendingIntent = PendingIntent.getActivity(context , 0, radiointent, 0);
        views.setOnClickPendingIntent(R.id.artwork, pendingIntent);
        //Play/Stop radio
        Intent controls = new Intent(context, NotificationControls.class);
        controls.setAction("radyo.menemen.widget.play");
        PendingIntent play = PendingIntent.getBroadcast(context.getApplicationContext(),1,controls,0);
        views.setOnClickPendingIntent(R.id.wplay, play);
        controls.setAction("radyo.menemen.widget.stop");
        PendingIntent stop = PendingIntent.getBroadcast(context.getApplicationContext(),2,controls,0);
        views.setOnClickPendingIntent(R.id.wstop, stop);
        views.setTextViewText(R.id.trackname, Menemen.fromHtmlCompat(m.oku(RadyoMenemenPro.broadcastinfo.CALAN)));
        //Load image
        if(!m.oku(MUSIC_INFO_SERVICE.LAST_ARTWORK_URL).equals("default") && PreferenceManager.getDefaultSharedPreferences(context).getBoolean("download_artwork",true))
        Glide.with(context.getApplicationContext())
                .load(m.oku(MUSIC_INFO_SERVICE.LAST_ARTWORK_URL))
                .asBitmap()
                .error(R.mipmap.album_placeholder)
                .into(new AppWidgetTarget(context.getApplicationContext(),views,R.id.artwork,appWidgetId));
        else views.setImageViewResource(R.id.artwork, R.mipmap.album_placeholder);
        int button_state = R.drawable.ic_play_arrow_black_24dp;
        if(m.isPlaying()) button_state = R.drawable.ic_pause_black_24dp;
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
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

