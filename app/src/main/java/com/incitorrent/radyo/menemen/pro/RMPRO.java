package com.incitorrent.radyo.menemen.pro;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;


/**
 * Radyo Menemen Pro Created by lutfi on 19.06.2016.
 */
public class RMPRO extends Application {
    //Google Servisleri için gerekli subclass
    public static Context context;
    public static final String TAG = RMPRO.class
            .getSimpleName();

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
        FirebaseMessaging.getInstance().subscribeToTopic("general");
        FirebaseMessaging.getInstance().subscribeToTopic("news");
        FirebaseMessaging.getInstance().subscribeToTopic("sync");
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
