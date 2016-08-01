package com.incitorrent.radyo.menemen.pro;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class show_image_comments extends AppCompatActivity {
        ImageView toolbar_image;
         private String imageurl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image_comments);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar()!=null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
       if(getIntent().getExtras()!=null) imageurl = getIntent().getExtras().getString("url");
        toolbar_image = (ImageView) findViewById(R.id.toolbar_image);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("url", imageurl);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if(savedInstanceState!=null) imageurl = savedInstanceState.getString("url");
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Glide.with(show_image_comments.this)
                .load(imageurl)
                .error(android.R.color.holo_red_light)
                .into(toolbar_image);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       onBackPressed();
        return true;
    }

}
