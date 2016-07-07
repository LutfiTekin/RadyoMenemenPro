package com.incitorrent.radyo.menemen.pro.fragments;


import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RMPRO;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;

import static com.incitorrent.radyo.menemen.pro.RadyoMenemenPro.transitionname.*;

/**
 * A simple {@link Fragment} subclass.
 */
public class track_info extends Fragment implements View.OnClickListener{
    private ImageView art,iv_spotify,iv_youtube,iv_lyric;
    private TextView track,tv_spotify,tv_youtube,tv_lyric;

    public track_info() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View trackview = inflater.inflate(R.layout.fragment_track_info, container, false);
        art = (ImageView) trackview.findViewById(R.id.art);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            art.setTransitionName(ART);
        track = (TextView) trackview.findViewById(R.id.track);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            track.setTransitionName(CALAN);
        iv_spotify = (ImageView) trackview.findViewById(R.id.iv_spotify);
        iv_youtube = (ImageView) trackview.findViewById(R.id.iv_youtube);
        iv_lyric = (ImageView) trackview.findViewById(R.id.iv_lyric);
        tv_spotify = (TextView) trackview.findViewById(R.id.tv_spotify);
        tv_youtube = (TextView) trackview.findViewById(R.id.tv_youtube);
        tv_lyric = (TextView) trackview.findViewById(R.id.tv_lyric);
        iv_spotify.setOnClickListener(this);
        iv_youtube.setOnClickListener(this);
        iv_lyric.setOnClickListener(this);
        tv_spotify.setOnClickListener(this);
        tv_youtube.setOnClickListener(this);
        tv_lyric.setOnClickListener(this);

        return trackview;
    }

    @Override
    public void onClick(View view) {
        final String trackName = track.getText().toString();
    if(view == iv_spotify || view == tv_spotify){
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setAction(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH);
        intent.setComponent(new ComponentName(
                "com.spotify.music",
                "com.spotify.music.MainActivity"));
        intent.putExtra(SearchManager.QUERY, trackName);
        this.startActivity(intent);
        //Analytics track event
        RMPRO.getInstance().trackEvent("radio","search on spotify",trackName);
    }else if(view == iv_youtube || view == tv_youtube){
        Intent intent = new Intent(Intent.ACTION_SEARCH);
        intent.setPackage("com.google.android.youtube");
        intent.putExtra(SearchManager.QUERY, trackName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        //Analytics track event
        RMPRO.getInstance().trackEvent("radio","search on youtube",trackName);
    }else if(view == iv_lyric || view == tv_lyric){
        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
        intent.putExtra(SearchManager.QUERY, trackName + " " + getString(R.string.lyrics)); // query contains search string
        startActivity(intent);
        //Analytics track event
        RMPRO.getInstance().trackEvent("radio","search lyrics",trackName);
    }
    }
}
