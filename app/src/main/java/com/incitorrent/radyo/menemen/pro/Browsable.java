package com.incitorrent.radyo.menemen.pro;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.incitorrent.radyo.menemen.pro.utils.topicDB;

public class Browsable extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent main = new Intent(this,MainActivity.class);

        if(getIntent().getData()!=null){
            if(getIntent().getData().toString().contains("podcast"))
                main.setAction(RadyoMenemenPro.Action.PODCAST);
            else if(getIntent().getData().toString().contains("olanbiten"))
                main.setAction(RadyoMenemenPro.Action.OLAN_BITEN);
            else if(getIntent().getData().toString().contains(topicDB._TOPICID)) {
                main.setAction(RadyoMenemenPro.Action.TOPIC_MESSAGES);
                //https://radyomenemen.com?tid=8&str=topicstr
                main.putExtra(topicDB._TOPICID,getIntent().getData().getQueryParameter(topicDB._TOPICID));
                Log.d("BROWSABLE",getIntent().getData().toString());
            }
        }
        startActivity(main);

    }
}
