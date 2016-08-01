package com.incitorrent.radyo.menemen.pro;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.incitorrent.radyo.menemen.pro.utils.TouchImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class show_image extends AppCompatActivity implements View.OnClickListener {
    private TouchImageView image;
    private String imageurl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);
        image = (TouchImageView) findViewById(R.id.image);
        final Intent mintent = getIntent();
        final Uri imageUri=mintent.getData();
        imageurl = imageUri.toString().trim();

    }

    @Override
    protected void onStart() {
        Glide.with(image.getContext())
                .load(imageurl)
                .dontAnimate()
                .centerCrop()
                .into(image);
        super.onStart();
    }

    @Override
    protected void onResume() {

        super.onResume();
    }

    @Override
    public void onClick(View view) {
     //TODO handle fab click
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.image_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_save:
            save();
                break;
            case R.id.action_share:
            share();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void share() {
        final String root = Environment.getExternalStorageDirectory().toString();
        final File compressed = new File(root + "/RadyoMenemen/images/compressed");
        final String fname =  System.currentTimeMillis() + ".jpg";
        final int width = image.getWidth();
        final int height = image.getHeight();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File file = new File(compressed, fname);
                    file.createNewFile();
                    FileOutputStream out = new FileOutputStream(file);
                    Bitmap bit = Glide.with(show_image.this).load(imageurl).asBitmap().into(width,height).get();
                    bit.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                } catch (InterruptedException | ExecutionException | IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        String pic = compressed + "/" + fname;
        Uri pictureUri = Uri.parse(pic);
        Log.v("URI", "BUILD "+ pictureUri);
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, pictureUri);
        shareIntent.setType("image/*");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
    }

    private void save() {
        final String root = Environment.getExternalStorageDirectory().toString();
        final File compressed = new File(root + "/RadyoMenemen/images/compressed");
        final String fname =  System.currentTimeMillis() + ".jpg";
        final int width = image.getWidth();
        final int height = image.getHeight();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File file = new File(compressed, fname);
                    file.createNewFile();
                    FileOutputStream out = new FileOutputStream(file);
                    Bitmap bit = Glide.with(show_image.this).load(imageurl).asBitmap().into(width,height).get();
                    bit.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                } catch (InterruptedException | ExecutionException | IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        Toast.makeText(show_image.this, compressed.toString() + fname, Toast.LENGTH_SHORT).show();
    }
}
