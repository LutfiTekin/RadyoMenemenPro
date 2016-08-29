package com.incitorrent.radyo.menemen.pro;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;


/**
 * Radyo Menemen Pro Created by lutfi on 19.06.2016.
 */

public class RMPRO extends Application {
    //Google Servisleri için gerekli subclass
    public static Context context;
    public static final String TAG = RMPRO.class
            .getSimpleName();
//    static {
//        AppCompatDelegate.setDefaultNightMode(
//                AppCompatDelegate.MODE_NIGHT_YES);
//    }
    private static RMPRO mInstance;
    private FirebaseAnalytics mFirebaseAnalytics;
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        RMPRO.context = getApplicationContext();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        FirebaseMessaging.getInstance().unsubscribeFromTopic("general");
        FirebaseMessaging.getInstance().subscribeToTopic("news");
        FirebaseMessaging.getInstance().subscribeToTopic("sync");
        FirebaseMessaging.getInstance().subscribeToTopic("onair");
        FirebaseMessaging.getInstance().subscribeToTopic("podcast");
    }

    public static Context getContext(){
        return RMPRO.context;
    };

    public static synchronized RMPRO getInstance() {
        return mInstance;
    }

    public FirebaseAnalytics getFireBase() {
        return mFirebaseAnalytics;
    }
}
