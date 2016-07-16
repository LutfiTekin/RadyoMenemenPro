package com.incitorrent.radyo.menemen.pro.services;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Radyo Menemen Pro Created by lutfi on 16.07.2016.
 */
public class FIREBASE_CM_SERVICE extends FirebaseMessagingService{

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String msg = remoteMessage.getData().get("msg");
        String destination = remoteMessage.getTo();
        Log.v("onMessageReceived", "message received"+ msg + " " + destination);
        super.onMessageReceived(remoteMessage);
    }
}
