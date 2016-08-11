package com.incitorrent.radyo.menemen.pro.fragments;


import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.transition.AutoTransition;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;
import com.incitorrent.radyo.menemen.pro.services.MUSIC_PLAY_SERVICE;


public class podcast_now_playing extends Fragment {
    TextView title,descr;
    CardView podcastcard;
    SeekBar seekBar;
    BroadcastReceiver receiver;

    public podcast_now_playing() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View podcastview = inflater.inflate(R.layout.fragment_podcast_now_playing, container, false);
        Bundle bundle = this.getArguments();
        String podcast_title,podcast_descr;
        title = (TextView) podcastview.findViewById(R.id.podcast_title);
        descr = (TextView) podcastview.findViewById(R.id.podcast_descr);
        podcastcard = (CardView) podcastview.findViewById(R.id.podcast_card);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            podcastcard.setTransitionName(RadyoMenemenPro.transitionname.PODCASTCARD);
        seekBar = (SeekBar) podcastview.findViewById(R.id.seekBar);
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
                    }else if(action.equals(MUSIC_PLAY_SERVICE.PODCAST_SEEKBAR_BUFFERING_UPDATE)){
                        int buffer = intent.getExtras().getInt("buffer");
                        int max = seekBar.getMax();
                        int seconds = (max * buffer) / 100;
                        seekBar.setSecondaryProgress(seconds);
                    }else if(action.equals(MUSIC_PLAY_SERVICE.PODCAST_TERMINATE)){
                        getFragmentManager().beginTransaction().replace(R.id.Fcontent, new podcast()).commit();
                    }
                }
            }
        };
        return podcastview;
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
            LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver((receiver), filter);
        }
        super.onStart();
    }

    @Override
    public void onStop() {
        if(getActivity() != null) LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
        super.onStop();
    }
}
