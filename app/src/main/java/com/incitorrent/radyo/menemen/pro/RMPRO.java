package com.incitorrent.radyo.menemen.pro;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

/**
 * Radyo Menemen Pro Created by lutfi on 19.06.2016.
 */
public class RMPRO extends Application {
    //Google Analytics için gerekli subclass
    Tracker mTracker;
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public void startTracking() {
        if (mTracker == null) {
            GoogleAnalytics ga;
            ga = GoogleAnalytics.getInstance(this);
            mTracker = ga.newTracker(R.xml.analytics_tracker);
            ga.enableAutoActivityReports(this);
        }
    }
    public Tracker getTracker(){
        startTracking();
        return mTracker;
    }



}
