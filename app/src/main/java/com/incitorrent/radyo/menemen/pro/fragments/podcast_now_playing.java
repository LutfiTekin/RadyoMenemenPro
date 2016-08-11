package com.incitorrent.radyo.menemen.pro.fragments;


import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.transition.AutoTransition;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

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
    FloatingActionButton placeholder;
    Chronometer chronometer;
    ProgressBar progressBar;
    BroadcastReceiver receiver;
    ScheduledThreadPoolExecutor exec;
    Menemen m;
    long timeWhenStopped = 0;
    LocalBroadcastManager localBroadcastManager;
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
        seekBar.setEnabled(false);
        seekBar.setOnSeekBarChangeListener(this);
        progressBar = (ProgressBar) podcastview.findViewById(R.id.loading);
        chronometer = (Chronometer) podcastview.findViewById(R.id.chr);
        placeholder = (FloatingActionButton) podcastview.findViewById(R.id.placeholder);
        placeholder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().startService(new Intent(getActivity().getApplicationContext(),MUSIC_PLAY_SERVICE.class));
            }
        });
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
                        seekBar.setEnabled(true);
                        progressBar.setVisibility(View.GONE);
                        seekBar.setMax(sec);
                        seekBar.setProgress(currentsec);
                        placeholder.setImageResource(android.R.drawable.ic_media_pause);
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
                        placeholder.setImageResource((play) ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
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
        if(seekBar != null) outState.putBoolean("seekbar", seekBar.isEnabled());
        if(progressBar != null) outState.putInt("progressbar", progressBar.getVisibility());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if(savedInstanceState != null && chronometer != null && seekBar != null && progressBar != null) {
            chronometer.setBase(savedInstanceState.getLong("chr"));
            chronometer.start();
            seekBar.setEnabled(savedInstanceState.getBoolean("seekbar"));
            if(savedInstanceState.getInt("progressbar") == View.GONE) progressBar.setVisibility(View.GONE);
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
        if(getActivity()!=null) {
            FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
            if(fab!=null) fab.setVisibility(View.GONE);
        }
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
        if(getActivity() != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);

                FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
                if(fab!=null) fab.setVisibility(View.VISIBLE);

        }
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
        //update media player service
        Intent seek = new Intent(MUSIC_PLAY_SERVICE.PODCAST_SEEK_FILTER);
        int progress = seekBar.getProgress() < seekBar.getSecondaryProgress() ? seekBar.getProgress() * 1000 : (seekBar.getSecondaryProgress() * 1000) - 1000;
        seek.putExtra("seek",progress);
        seekBar.setProgress(progress/1000);
        localBroadcastManager =  LocalBroadcastManager.getInstance(getActivity().getApplicationContext());
        localBroadcastManager.sendBroadcast(seek);
    }

    @Override
    public void onDestroy() {
        exec.shutdown();
        super.onDestroy();
    }
}
