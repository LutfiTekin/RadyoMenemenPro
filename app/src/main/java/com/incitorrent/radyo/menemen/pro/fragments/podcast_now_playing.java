package com.incitorrent.radyo.menemen.pro.fragments;


import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.transition.AutoTransition;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;
import com.incitorrent.radyo.menemen.pro.services.MUSIC_PLAY_SERVICE;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class podcast_now_playing extends Fragment implements SeekBar.OnSeekBarChangeListener{
    TextView title,descr;
    CardView podcastcard;
    SeekBar seekBar;
    Chronometer chronometer;
    BroadcastReceiver receiver;
    ScheduledThreadPoolExecutor exec;
    Menemen m;
    long timeWhenStopped = 0;
    public podcast_now_playing() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View podcastview = inflater.inflate(R.layout.fragment_podcast_now_playing, container, false);
        exec = new ScheduledThreadPoolExecutor(1);
        m = new Menemen(getActivity().getApplicationContext());
        Bundle bundle = this.getArguments();
        String podcast_title,podcast_descr;
        title = (TextView) podcastview.findViewById(R.id.podcast_title);
        descr = (TextView) podcastview.findViewById(R.id.podcast_descr);
        podcastcard = (CardView) podcastview.findViewById(R.id.podcast_card);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            podcastcard.setTransitionName(RadyoMenemenPro.transitionname.PODCASTCARD);
        seekBar = (SeekBar) podcastview.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);
        chronometer = (Chronometer) podcastview.findViewById(R.id.chr);
        if(bundle != null){
            podcast_title = bundle.getString("title");
            podcast_descr = bundle.getString("descr");
            title.setText(podcast_title);
            descr.setText(podcast_descr);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setSharedElementEnterTransition(new AutoTransition());
        }

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent!=null) {
                    String action = intent.getExtras().getString("action");
                    if(action==null) return;
                    if(action.equals(MUSIC_PLAY_SERVICE.PODCAST_GET_DURATION)){
                        long duration = intent.getExtras().getLong("duration");
                        long currentPos = intent.getExtras().getLong("current");
                        if(duration == -1) return;
                        int sec = (int) duration/1000;
                        int currentsec = (int) currentPos/1000;
                        seekBar.setMax(sec);
                        seekBar.setProgress(currentsec);
                        Toast.makeText(getActivity().getApplicationContext(), "Duration is " + duration + " in seconds " + sec, Toast.LENGTH_SHORT).show();
                        startTimer();
                    }else if(action.equals(MUSIC_PLAY_SERVICE.PODCAST_SEEKBAR_BUFFERING_UPDATE)){
                        int buffer = intent.getExtras().getInt("buffer");
                        int max = seekBar.getMax();
                        int seconds = (max * buffer) / 100;
                        seekBar.setSecondaryProgress(seconds);
                    }else if(action.equals(MUSIC_PLAY_SERVICE.PODCAST_TERMINATE)){
                        getFragmentManager().beginTransaction().replace(R.id.Fcontent, new podcast()).commit();
                    }else if(action.equals(RadyoMenemenPro.PLAY)){
                        Boolean play = intent.getExtras().getBoolean(RadyoMenemenPro.PLAY);
                        if(play) {
                            chronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
                            chronometer.start();
                        }
                        else {
                            timeWhenStopped = chronometer.getBase() - SystemClock.elapsedRealtime();
                            chronometer.stop();
                        }
                    }
                }
            }
        };
        return podcastview;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
       if(chronometer != null) outState.putLong("chr",chronometer.getBase());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if(savedInstanceState != null && chronometer != null) {
            chronometer.setBase(savedInstanceState.getLong("chr"));
            chronometer.start();
        }
        super.onActivityCreated(savedInstanceState);
    }

    private void startTimer() {
        chronometer.start();
        exec.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if(m.oku("caliyor").equals("evet")) {
                    seekBar.setProgress(seekBar.getProgress() + 1);
                }
            }
        },0,1, TimeUnit.SECONDS);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        if(getActivity() != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(MUSIC_PLAY_SERVICE.PODCAST_PLAY_FILTER);
            filter.addAction(MUSIC_PLAY_SERVICE.PODCAST_SEEKBAR_BUFFERING_UPDATE);
            filter.addAction(MUSIC_PLAY_SERVICE.MUSIC_PLAY_FILTER);
            LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver((receiver), filter);
        }
        super.onStart();
    }

    @Override
    public void onStop() {
        if(getActivity() != null) LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
        super.onStop();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
       if(chronometer != null)
           chronometer.setBase(SystemClock.elapsedRealtime() - (i * 1000));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if(chronometer != null)
            chronometer.setBase(SystemClock.elapsedRealtime() - (seekBar.getProgress() * 1000));
        //TODO update media player service
    }

    @Override
    public void onDestroy() {
        exec.shutdown();
        super.onDestroy();
    }
}
