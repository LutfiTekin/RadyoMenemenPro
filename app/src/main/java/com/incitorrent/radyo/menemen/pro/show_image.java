package com.incitorrent.radyo.menemen.pro;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class show_image extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);

        final Intent mintent = getIntent();
        final Bundle myBundle=mintent.getExtras();
        final Uri myURI=mintent.getData();
        final String value;
        if (myURI != null) {
            Toast.makeText(show_image.this, myURI.toString(), Toast.LENGTH_SHORT).show();
        }
    }
}
