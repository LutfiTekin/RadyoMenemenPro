package com.incitorrent.radyo.menemen.pro.fragments;


import android.app.DownloadManager;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.transition.AutoTransition;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;
import com.incitorrent.radyo.menemen.pro.services.MUSIC_PLAY_SERVICE;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;

import java.util.concurrent.ScheduledThreadPoolExecutor;
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
            long duration = bundle.getLong("duration");
            long current = bundle.getLong("current");
            if(duration != 0) {
                setCurrentandDuration(duration, current);
                startTimer();
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
                    if(action.equals(MUSIC_PLAY_SERVICE.PODCAST_GET_DURATION)){
                        long duration = intent.getExtras().getLong("duration");
                        long currentPos = intent.getExtras().getLong("current");
                        setCurrentandDuration(duration, currentPos);
                        placeholder.setImageResource(android.R.drawable.ic_media_pause);
                        startTimer();
                    }else if(action.equals(MUSIC_PLAY_SERVICE.PODCAST_SEEKBAR_BUFFERING_UPDATE)){
                        int buffer = intent.getExtras().getInt("buffer");
                        int max = seekBar.getMax();
                        int seconds = (max * buffer) / 100;
                        seekBar.setSecondaryProgress(seconds);
                        int currentsec = intent.getExtras().getInt("current")/1000;
                        seekBar.setProgress(currentsec);
                    }else if(action.equals(MUSIC_PLAY_SERVICE.PODCAST_TERMINATE)){
                        try {
                            getFragmentManager().beginTransaction().replace(R.id.Fcontent, new podcast()).commit();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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

    private void setCurrentandDuration(long duration, long currentPos) {
        if(duration < 1) return;
        int sec = (int) duration/1000;
        int currentsec = (int) currentPos/1000;
        seekBar.setEnabled(true);
        progressBar.setVisibility(View.GONE);
        seekBar.setMax(sec);
        seekBar.setProgress(currentsec);
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
            Log.v("chr", "" + savedInstanceState.getLong("chr"));
            long diff = SystemClock.elapsedRealtime() - savedInstanceState.getLong("chr");
            chronometer.setBase(diff);
            chronometer.start();
            seekBar.setEnabled(savedInstanceState.getBoolean("seekbar"));
            if(savedInstanceState.getInt("progressbar") == View.GONE) progressBar.setVisibility(View.GONE);
        }
        super.onActivityCreated(savedInstanceState);
    }

    private void startTimer() {
        chronometer.start();
        try {
            exec.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    if(m.isPlaying()) {
                        seekBar.setProgress(seekBar.getProgress() + 1);
                    }
                }
            },0,1, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
       if(placeholder != null)
        placeholder.setImageResource(m.isPlaying() ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
       if(m.isPlaying() && seekBar.getMax() > 100)
           progressBar.setVisibility(View.GONE);
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
        try {
            exec.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
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
        try {
            exec.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                downloadPodcast(getActivity().getApplicationContext(), title.getText().toString(), m.oku(RadyoMenemenPro.broadcastinfo.PODCAST_URL));
                break;
        }
    }

    private void downloadPodcast(final Context context,final String title,final String url) {
        new AlertDialog.Builder(new ContextThemeWrapper(getActivity(),R.style.alertDialogTheme))
                .setTitle(title)
                .setMessage(getString(R.string.download_file))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                            request.setDescription(context.getString(R.string.downloading_file))
                                    .setTitle(title)
                                    .allowScanningByMediaScanner();
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title + ".mp3");
                            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                            manager.enqueue(request);
                            Toast.makeText(context, context.getString(R.string.downloading_file), Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(context, android.R.string.httpErrorBadUrl, Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setIcon(R.drawable.podcast)
                .show();
    }
}
