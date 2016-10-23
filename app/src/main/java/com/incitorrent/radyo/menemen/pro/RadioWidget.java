package com.incitorrent.radyo.menemen.pro;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;
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
        Menemen m = new Menemen(context);
        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.radio_widget);
        //Go to radio fragment
        Intent radiointent = new Intent(context, MainActivity.class);
        radiointent.setAction("radyo.menemen.play");
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, radiointent, 0);
        views.setOnClickPendingIntent(R.id.wlastp, pendingIntent);
        //Play/Stop radio
        Intent controls = new Intent(context, NotificationControls.class);
        String selected_channel = m.oku(PreferenceManager.getDefaultSharedPreferences(context).getString("radio_channel",RadyoMenemenPro.HIGH_CHANNEL));
        String dataSource = "http://" + m.oku(RadyoMenemenPro.RADIO_SERVER) + ":" + selected_channel +  "/";
        controls.putExtra("dataSource",dataSource);
        PendingIntent play = PendingIntent.getBroadcast(context,1,controls,0);
        views.setOnClickPendingIntent(R.id.wplay, play);
        views.setTextViewText(R.id.trackname, m.oku(RadyoMenemenPro.broadcastinfo.CALAN));
        //Load image
        Glide.with(context.getApplicationContext())
                .load(m.oku(MUSIC_INFO_SERVICE.LAST_ARTWORK_URL))
                .asBitmap()
                .error(R.mipmap.album_placeholder)
                .into(new AppWidgetTarget(context.getApplicationContext(),views,R.id.artwork,appWidgetId));
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
        Log.v("WİDGETUPDATE", dataSource);


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

