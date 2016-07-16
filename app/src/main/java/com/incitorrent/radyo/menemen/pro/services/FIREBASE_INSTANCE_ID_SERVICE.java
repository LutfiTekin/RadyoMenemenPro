package com.incitorrent.radyo.menemen.pro.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.incitorrent.radyo.menemen.pro.RMPRO;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;

import java.util.HashMap;
import java.util.Map;

public class FIREBASE_INSTANCE_ID_SERVICE extends FirebaseInstanceIdService {
    Menemen m = new Menemen(RMPRO.getContext());

    @Override
    public void onTokenRefresh() {
        final String token = FirebaseInstanceId.getInstance().getToken();
        Map<String, String> dataToSend = new HashMap<>();
        dataToSend.put("nick", m.oku("username"));
        dataToSend.put("token", token);
        String encodedStr = Menemen.getEncodedData(dataToSend);
        Menemen.postMenemenData(RadyoMenemenPro.TOKEN_ADD,encodedStr);
        super.onTokenRefresh();
    }
}
