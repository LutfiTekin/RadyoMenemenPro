package com.incitorrent.radyo.menemen.pro.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Integer.parseInt;

public class TriggerSongChange extends BroadcastReceiver {
    private RequestQueue queue;
    private Menemen m;
    /**
     *  Mobil key is wrong
     */
    public static final int NOT_AUTHORIZED = 1;
    /**
     * Oto is not the current dj
     */
    public static final int DJ_ON_AIR = 2;
    /**
     * User does not have menemen point
     */
    public static final int NO_MP = 4;
    /**
     * Not enough menemen point to change the song
     */
    public static final int INSUFFICENT_MP = 4;
    /**
     * One song change request is already in order
     */
    public static final int CURRENTLY_CHANGING = 5;
    /**
     * Song change request is successful
     */
    public static final int SONG_CHANGED = 6;
    /**
     * Song change request is failed
     */
    public static final int SONG_CHANGE_FAILED = 7;
    public TriggerSongChange() {
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        m = new Menemen(context);
        if(!m.isInternetAvailable()){
            Toast.makeText(context, R.string.toast_check_your_connection, Toast.LENGTH_SHORT).show();
            return;
        }
        queue = Volley.newRequestQueue(context.getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, RadyoMenemenPro.MP_CHANGE_SONG,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String toastmsg;
                        if(parseInt(response) == NOT_AUTHORIZED)
                            toastmsg = context.getString(R.string.toast_auth_error);
                        else if(parseInt(response) == DJ_ON_AIR)
                            toastmsg = context.getString(R.string.toast_sc_auto_dj);
                        else if(parseInt(response) == NO_MP)
                            toastmsg = context.getString(R.string.toast_sc_no_mp);
                        else if(parseInt(response) == INSUFFICENT_MP)
                            toastmsg = context.getString(R.string.toast_sc_inscf_mp);
                        else if (parseInt(response) == CURRENTLY_CHANGING)
                            toastmsg = context.getString(R.string.toast_sc_cur);
                        else if(parseInt(response) == SONG_CHANGED)
                            toastmsg = context.getString(R.string.toast_sc_success);
                        else toastmsg = context.getString(R.string.error_occured);
                        Toast.makeText(context, toastmsg, Toast.LENGTH_LONG).show();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, R.string.error_occured, Toast.LENGTH_SHORT).show();
                    }
        }){
            @Override
            public Priority getPriority() {
                return Priority.IMMEDIATE;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> dataToSend = new HashMap<>();
                dataToSend.put("nick", m.getUsername());
                dataToSend.put("mkey", m.getMobilKey());
                return dataToSend;
            }

            @Override
            public RetryPolicy getRetryPolicy() {
                return new DefaultRetryPolicy(5000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            }
        };
        queue.add(stringRequest);
    }



}
