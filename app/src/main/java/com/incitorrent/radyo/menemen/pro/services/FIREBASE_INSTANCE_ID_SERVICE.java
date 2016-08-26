package com.incitorrent.radyo.menemen.pro.services;

import com.google.firebase.iid.FirebaseInstanceIdService;
import com.incitorrent.radyo.menemen.pro.RMPRO;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;

public class FIREBASE_INSTANCE_ID_SERVICE extends FirebaseInstanceIdService {
    Menemen m = new Menemen(RMPRO.getContext());

    @Override
    public void onTokenRefresh() {
      if(m.isLoggedIn()) m.setToken();
        super.onTokenRefresh();
    }
}
