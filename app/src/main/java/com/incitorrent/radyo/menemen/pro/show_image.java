package com.incitorrent.radyo.menemen.pro;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.bumptech.glide.Glide;
import com.incitorrent.radyo.menemen.pro.utils.TouchImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class show_image extends AppCompatActivity {
    private TouchImageView image;
    private String imageurl;
    final String root = Environment.getExternalStorageDirectory().toString();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);
        image = (TouchImageView) findViewById(R.id.image);
        final Intent mintent = getIntent();
        final Uri imageUri=mintent.getData();
        imageurl = imageUri.toString().trim();
        FloatingActionButton c_fab = (FloatingActionButton) findViewById(R.id.comment_fab);
        c_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(show_image.this, show_image_comments.class));
            }
        });
    }

    @Override
    protected void onStart() {
        Glide.with(image.getContext())
                .load(imageurl)
                .dontAnimate()
                .into(image);
        super.onStart();
    }

    @Override
    protected void onResume() {

        super.onResume();
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

    public void share() {
        String pic = save();
        Uri pictureUri = Uri.parse(pic);
        Log.v("URI", "BUILD "+ pictureUri);
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, pictureUri);
        shareIntent.setType("image/*");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
    }

    public String save() {
        final File compressed = new File(root + "/RadyoMemenen/images/compressed");
        long time = System.currentTimeMillis();
        final String fname = String.valueOf(time).substring(0,12)  + ".jpg";
        final int width = image.getWidth();
        final int height = image.getHeight();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    compressed.mkdirs();
                    File file = new File(compressed, fname);
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
        return compressed + "/" + fname;
    }
}
