package com.incitorrent.radyo.menemen.pro;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

/**
 * Radyo Menemen Pro Created by lutfi on 19.06.2016.
 */
public class RMPRO extends Application {
    //Google Analytics için gerekli subclass
    Tracker mTracker;


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
