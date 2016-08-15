package com.incitorrent.radyo.menemen.pro;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class Browsable extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent main = new Intent(this,MainActivity.class);

        if(getIntent().getData()!=null){
            if(getIntent().getData().toString().contains("podcast"))
                main.setAction("radyo.menemen.podcast");
            else if(getIntent().getData().toString().contains("olanbiten"))
                main.setAction("radyo.menemen.news");
        }
        startActivity(main);

    }
}
