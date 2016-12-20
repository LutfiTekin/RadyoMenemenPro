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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;
import com.incitorrent.radyo.menemen.pro.services.MUSIC_PLAY_SERVICE;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class podcast_now_playing extends Fragment implements SeekBar.OnSeekBarChangeListener,View.OnClickListener{
    TextView title,descr;
    ImageView rewind,forward,download;
    CardView podcastcard;
    SeekBar seekBar;
    FloatingActionButton placeholder;
    Chronometer chronometer;
    ProgressBar progressBar;
    BroadcastReceiver receiver;
    Menemen m;
    long timeWhenStopped = 0;
    LocalBroadcastManager localBroadcastManager;
    ArrayList<String> podcasts;
    public podcast_now_playing() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View podcastview = inflater.inflate(R.layout.fragment_podcast_now_playing, container, false);
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
        rewind = (ImageView) podcastview.findViewById(R.id.rewind);
        forward = (ImageView) podcastview.findViewById(R.id.forward);
        download = (ImageView) podcastview.findViewById(R.id.download);
        placeholder = (FloatingActionButton) podcastview.findViewById(R.id.placeholder);
        placeholder.setOnClickListener(this);
        rewind.setOnClickListener(this);
        forward.setOnClickListener(this);
        download.setOnClickListener(this);
        if(bundle != null){
            podcast_title = bundle.getString("title");
            podcast_descr = bundle.getString("descr");
            title.setText(podcast_title);
            descr.setText(podcast_descr);
            long podcastDur = bundle.getLong("duration");
            long current = bundle.getLong("current");
            try {
                podcasts = bundle.getStringArrayList("podcast_list");
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(podcastDur != 0) {
                setCurrentandDuration(podcastDur, current, 0);
//                startTimer();

            }
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
                    switch (action) {
                        case MUSIC_PLAY_SERVICE.PODCAST_GET_DURATION:
                            long duration = intent.getExtras().getLong("duration");
                            long currentPos = intent.getExtras().getLong("current");
                            long bufferedPos = intent.getExtras().getLong("buffer");
                            setCurrentandDuration(duration, currentPos, bufferedPos);
                            if(duration>0)
                                chronometer.setFormat("%s/" + getFormattedDuration(duration));
                            chronometer.start();
                            if(m.isPlaying())
                                placeholder.setImageResource(android.R.drawable.ic_media_pause);
                            else chronometer.stop();
                            title.setText(m.oku(RadyoMenemenPro.PLAYING_PODCAST));
                            descr.setText(m.oku(RadyoMenemenPro.broadcastinfo.PODCAST_DESCR));
                            break;
                        case MUSIC_PLAY_SERVICE.PODCAST_SEEKBAR_BUFFERING_UPDATE:
                            int buffer = intent.getExtras().getInt("buffer") / 1000;
                            int currentsec = intent.getExtras().getInt("current") / 1000;
                            seekBar.setSecondaryProgress(buffer);
                            seekBar.setProgress(currentsec);
                            break;
                        case MUSIC_PLAY_SERVICE.PODCAST_TERMINATE:
                            try {
                                getFragmentManager().beginTransaction().replace(R.id.Fcontent, new podcast()).commit();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case RadyoMenemenPro.PLAY:
                            Boolean play = intent.getExtras().getBoolean(RadyoMenemenPro.PLAY);
                            placeholder.setImageResource((play) ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
                            if (play) {
                                chronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
                                chronometer.start();
                            } else {
                                timeWhenStopped = chronometer.getBase() - SystemClock.elapsedRealtime();
                                chronometer.stop();
                            }
                            break;
                    }
                }
            }
        };
        return podcastview;
    }

    String getFormattedDuration(long duration) {
        return String.format(Locale.getDefault(), "%02d:%02d:%02d",
                                        TimeUnit.MILLISECONDS.toHours(duration),
                                        TimeUnit.MILLISECONDS.toMinutes(duration) -
                                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)),
                                        TimeUnit.MILLISECONDS.toSeconds(duration) -
                                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
    }

    private void setCurrentandDuration(long duration, long currentPos, long bufferedPos) {
        if(duration < 1) return;
        if(bufferedPos > 0)
            seekBar.setSecondaryProgress((int) bufferedPos/1000);
        seekBar.setEnabled(true);
        progressBar.setVisibility(View.INVISIBLE);
        seekBar.setMax((int) duration/1000);
        seekBar.setProgress((int) currentPos/1000);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(seekBar != null) outState.putBoolean("seekbar", seekBar.isEnabled());
        if(progressBar != null) outState.putInt("progressbar", progressBar.getVisibility());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d("PODCAST","onActivityCreated");
        if(savedInstanceState != null && chronometer != null && seekBar != null) {
            seekBar.setEnabled(savedInstanceState.getBoolean("seekbar"));
            if(savedInstanceState.getInt("progressbar") == View.INVISIBLE) progressBar.setVisibility(View.INVISIBLE);
        }
        super.onActivityCreated(savedInstanceState);
    }



    void requestPodcastDuration() {
        Intent requeststat = new Intent(MUSIC_PLAY_SERVICE.PODCAST_SEEK_FILTER);
        requeststat.putExtra("action","requeststat");
        requeststat.putExtra("podcast_list",podcasts);
        localBroadcastManager =  LocalBroadcastManager.getInstance(getActivity().getApplicationContext());
        localBroadcastManager.sendBroadcast(requeststat);
    }

    @Override
    public void onResume() {
       if(placeholder != null)
        placeholder.setImageResource(m.isPlaying() ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
       if(m.isPlaying() && seekBar.getMax() > 100)
           progressBar.setVisibility(View.INVISIBLE);
        requestPodcastDuration();
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
        //update media player service
        seekTo(seekBar.getProgress());
    }

    private void seekTo(int i) {
        Intent seek = new Intent(MUSIC_PLAY_SERVICE.PODCAST_SEEK_FILTER);
        int progress = i < seekBar.getSecondaryProgress() ? i * 1000 : (seekBar.getSecondaryProgress() * 1000) - 1000;
        seek.putExtra("seek",progress);
        seek.putExtra("action","seek");
        seekBar.setProgress(progress/1000);
        if(chronometer != null)
            chronometer.setBase(SystemClock.elapsedRealtime() - progress);
        localBroadcastManager =  LocalBroadcastManager.getInstance(getActivity().getApplicationContext());
        localBroadcastManager.sendBroadcast(seek);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.placeholder:
                getActivity().startService(new Intent(getActivity().getApplicationContext(),MUSIC_PLAY_SERVICE.class));
                break;
            case R.id.rewind:
                seekTo(seekBar.getProgress() - 5);
                break;
            case R.id.forward:
                seekTo(seekBar.getProgress() + 5);
                break;
            case R.id.download:
                m.downloadMenemenFile(m.oku(RadyoMenemenPro.broadcastinfo.PODCAST_URL), title.getText().toString(), R.drawable.podcast, "/RadyoMemenen/podcast", ".mp3", getActivity());
                 break;
        }
    }
}
