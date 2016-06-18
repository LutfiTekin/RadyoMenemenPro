package com.incitorrent.radyo.menemen.pro;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
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
        Uri image = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                   if(getRealPathFromURI(image)!=null) {
                       Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse("file://" + getRealPathFromURI(image)));
                    new CapsYukle(bitmap,this).execute();
                   }else Toast.makeText(get_image.this, R.string.error_occured, Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(get_image.this, getString(R.string.error_occured) + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.v("GET IMAGE", e.getMessage() + " PARCELABLE " + getRealPathFromURI((Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM)) );
                    e.printStackTrace();
                }
            }
        }
        startActivity(new Intent(this,MainActivity.class));
    }
    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if(cursor!=null)cursor.moveToFirst(); else return null;
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        String realpath = cursor.getString(idx);
        cursor.close();
        return realpath;
    }
}
