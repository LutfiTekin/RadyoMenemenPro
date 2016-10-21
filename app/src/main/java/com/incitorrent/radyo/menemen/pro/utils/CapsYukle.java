package com.incitorrent.radyo.menemen.pro.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.incitorrent.radyo.menemen.pro.MainActivity;
import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Radyo Menemen Pro Created by lutfi on 3.06.2016.
 */
//CAPS YUKLE
public class CapsYukle extends AsyncTask<Void, Void, String> {
    NotificationCompat.Builder notification;
    private static final String TAG = "CAPSYUKLE";
    private static final int unid = 600613;
    private Menemen m;
    private Bitmap bit;
    private RequestQueue queue;

    Context context;
    public CapsYukle(Bitmap bit, Context context) {
        this.bit = bit;
        this.context = context;
    }


    @Override
    protected String doInBackground(Void... params) {
        m = new Menemen(context);
        queue = Volley.newRequestQueue(context);
        uploadingimg();
        StringRequest postRequest = new StringRequest(Request.Method.POST, RadyoMenemenPro.CAPS_API_URL,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        Log.v("Volley", response);
                        try {
                            JSONObject J = new JSONObject(response);
                            JSONObject Jo = J.getJSONObject("image");
                            if(!J.getString("status_code").equals("200")) throw new Exception(context.getString(R.string.image_not_uploaded));
                            bit.recycle();
                            bit = null;
                            final String imageurl = Jo.getString("url");
                            registerCaps(imageurl);
                            postMessage(imageurl);
                        } catch (Exception e) {
                            notification = new NotificationCompat.Builder(context)
                                    .setAutoCancel(true)
                                    .setSmallIcon(R.drawable.ic_upload)
                                    .setContentTitle(context.getString(android.R.string.dialog_alert_title))
                                    .setContentText(context.getString(R.string.error_occured));
                            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                            nm.notify(unid, notification.build());
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.v("Volley",error.toString());
                    }
                }
        ) {

            @Override
            protected Map<String, String> getParams(){
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                if(byteSizeOf(bit)>3440000){
                    bit = resizeBitmap(bit,720);
                }
                bit.compress(Bitmap.CompressFormat.PNG, 70, byteArrayOutputStream);
                String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
                Map<String,String> dataToSend = new HashMap<>();
                dataToSend.put("source", encodedImage);
                dataToSend.put("key", m.oku(RadyoMenemenPro.CAPS_API_KEY));
                return dataToSend;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(10000,2,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(postRequest);
        return null;
    }


    private void registerCaps(final String name) {
        StringRequest postRequest = new StringRequest(Request.Method.POST, RadyoMenemenPro.REGISTER_CAPS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    }
                },
                null
        ) {
            @Override
            protected Map<String, String> getParams(){
                HashMap<String, String> dataToSend = new HashMap<>();
                dataToSend.put("nick", m.oku("username"));
                dataToSend.put("imagehash", name);
                dataToSend.put("mkey", m.oku("mkey"));
                return dataToSend;
            }
        };
        queue.add(postRequest);
    }
    private void uploadedimg() {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        notification = new NotificationCompat.Builder(context);
        notification.setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_upload)
                .setLargeIcon(bitmap)
                .setOngoing(false)
                .setLocalOnly(true)
                .setTicker(context.getString(R.string.caps_uploaded))
                .setWhen(System.currentTimeMillis())
                .setContentTitle(context.getString(R.string.caps_uploaded))
                .setContentText(context.getString(R.string.caps_uploaded_sub))
                .setContentIntent(PendingIntent.getActivity(context, new Random().nextInt(100), new Intent(context, MainActivity.class), PendingIntent.FLAG_CANCEL_CURRENT));
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(unid, notification.build());

    }

    private void uploadingimg() {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        notification = new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_upload)
                .setLargeIcon(bitmap)
                .setProgress(0,0,true)
                .setOngoing(true)
                .setLocalOnly(true)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(context.getString(R.string.caps_uploading))
                .setContentText(context.getString(R.string.caps_uploading_in_progress))
                .setContentIntent(PendingIntent.getActivity(context, new Random().nextInt(100), new Intent(context, MainActivity.class), PendingIntent.FLAG_CANCEL_CURRENT));
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(unid, notification.build());
    }
    private static int byteSizeOf(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return bitmap.getAllocationByteCount();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            return bitmap.getByteCount();
        } else {
            return bitmap.getRowBytes() * bitmap.getHeight();
        }
    }

    private Bitmap resizeBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 0) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
    private void postMessage(final String imageurl){
        StringRequest postRequest = new StringRequest(Request.Method.POST, RadyoMenemenPro.MESAJ_GONDER,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject j = new JSONObject(response).getJSONArray("post").getJSONObject(0);
                            if(!j.get("status").equals("ok"))
                                Toast.makeText(context, R.string.error_occured, Toast.LENGTH_SHORT).show();
                            else uploadedimg();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        ) {

            @Override
            protected Map<String, String> getParams(){
                Map<String, String> dataToSend = new HashMap<>();
                dataToSend.put("nick", m.oku("username"));
                dataToSend.put("mkey", m.oku("mkey"));
                dataToSend.put("mesaj", imageurl);
                return dataToSend;
            }

            @Override
            public RetryPolicy getRetryPolicy() {
                return new DefaultRetryPolicy(10000,0,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            }
        };
        queue.add(postRequest);
    }
}