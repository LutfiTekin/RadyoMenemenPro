package com.incitorrent.radyo.menemen.pro.fragments;


import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.incitorrent.radyo.menemen.pro.R;
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

    }
}
