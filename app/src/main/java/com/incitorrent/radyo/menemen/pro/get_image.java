package com.incitorrent.radyo.menemen.pro;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.incitorrent.radyo.menemen.pro.utils.CapsYukle;

import java.io.IOException;

public class get_image extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Başka uygulamadan seçilen resmi işle
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        Log.v("GET IMAGE"," action:" + intent.getAction() + " type" + type );
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                try {

                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM));
                    new CapsYukle(bitmap,this).execute();
                } catch (IOException e) {
                    Toast.makeText(get_image.this, R.string.error_occured + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }
        startActivity(new Intent(this,MainActivity.class));
    }
}
