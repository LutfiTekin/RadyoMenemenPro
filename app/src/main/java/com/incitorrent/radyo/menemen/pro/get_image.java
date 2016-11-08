package com.incitorrent.radyo.menemen.pro;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.incitorrent.radyo.menemen.pro.utils.CapsYukle;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;

import java.io.InputStream;

public class get_image extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Başka uygulamadan seçilen resmi işle
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        Menemen m = new Menemen(this);
        Intent main = new Intent(this, MainActivity.class);
        if (Intent.ACTION_SEND.equals(action) && type != null && m.isLoggedIn()) {
            if (type.startsWith("image/")) {
                try {
                    Uri image = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                    InputStream is = getContentResolver().openInputStream(image);
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    new CapsYukle(bitmap,this).execute();
                    if(is != null)
                        is.close();
                } catch (Exception e) {
                    String toastmsg = getString(R.string.error_occured);
                    if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("show_full_error",false))
                        toastmsg = getString(R.string.error_occured) + "\n" + e.toString();
                    Toast.makeText(get_image.this, toastmsg, Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
            main.setAction(RadyoMenemenPro.Action.CHAT);
        }else
            Toast.makeText(get_image.this, R.string.toast_log_in_before_upload, Toast.LENGTH_SHORT).show();
        startActivity(main);
    }
}
