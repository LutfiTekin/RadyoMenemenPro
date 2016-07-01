package com.incitorrent.radyo.menemen.pro.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.incitorrent.radyo.menemen.pro.MainActivity;
import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
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
    Menemen m;
    Bitmap bit;

    Context context;
    public CapsYukle(Bitmap bit, Context context) {
        this.bit = bit;
        this.context = context;
    }


    @Override
    protected String doInBackground(Void... params) {
        m = new Menemen(context);
        uploadingimg();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bit.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
        String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
//            Log.i("CapsYukle", "Sıkıştırmadan sonra"+String.valueOf(byteSizeOf(bit)));

        Map<String,String> dataToSend = new HashMap<>();
        dataToSend.put("source", encodedImage);
        dataToSend.put("key", m.oku(RadyoMenemenPro.CAPS_API_KEY));
        String encodedStr = Menemen.getEncodedData(dataToSend);

        BufferedReader reader = null;
        try {
            URL url = new URL(RadyoMenemenPro.CAPS_API_URL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
            writer.write(encodedStr);
            writer.flush();
            StringBuilder sb = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            while((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            line = sb.toString();
            JSONObject J = new JSONObject(line);
            JSONObject Jo = J.getJSONObject("image");
            if(!J.getString("status_code").equals("200")) throw new Exception(context.getString(R.string.image_not_uploaded));
            Log.v(TAG,"JSON" + Jo.getString("date") + " " + Jo.getString("url"));
            bit.recycle();
            bit= null;
            System.gc();
            return Jo.getString("url");
        } catch (Exception e) {
            notification = new NotificationCompat.Builder(context)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_upload);
            notification.setContentTitle(context.getString(android.R.string.dialog_alert_title));
            notification.setContentText(context.getString(R.string.error_occured));
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(unid, notification.build());
            e.printStackTrace();
        } finally {
            if(reader != null) {
                try {
                    reader.close();     //Closing the
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        return null;
    }

    @Override
    protected void onPostExecute(final String s) {
        new AsyncTask<Void,Void,Boolean>(){
            @Override
            protected Boolean doInBackground(Void... params) {
                Map<String, String> dataToSend = new HashMap<>();
                dataToSend.put("nick", m.oku("username"));
                dataToSend.put("mkey", m.oku("mkey"));
                dataToSend.put("mesaj", s);
                String encodedStr = Menemen.getEncodedData(dataToSend);
                BufferedReader reader = null;
                try {
                    HttpURLConnection connection = (HttpURLConnection) new URL(RadyoMenemenPro.MESAJ_GONDER).openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                    writer.write(encodedStr);
                    writer.flush();
                    StringBuilder sb = new StringBuilder();
                    reader = new BufferedReader(new InputStreamReader(
                            connection.getInputStream(), "iso-8859-9"), 8);
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    line = sb.toString();
                    Log.v(TAG,"POST "+ line);
                    JSONObject j = new JSONObject(line).getJSONArray("post").getJSONObject(0);

                    if(j.get("status").equals("ok")) return true;
                }catch (IOException e){
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (!success) {
                    Toast.makeText(context, R.string.error_occured, Toast.LENGTH_SHORT).show();
                }
                super.onPostExecute(success);
            }
        }.execute();
        if(s!=null) uploadedimg();
    }

    public void uploadedimg() {
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

    public void uploadingimg() {
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

}