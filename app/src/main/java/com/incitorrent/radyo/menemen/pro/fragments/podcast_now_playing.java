package com.incitorrent.radyo.menemen.pro.fragments;


import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.transition.AutoTransition;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;


public class podcast_now_playing extends Fragment {
    TextView title,descr;
    CardView podcastcard;

    public podcast_now_playing() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
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
        if(bundle != null){
            podcast_title = bundle.getString("title");
            podcast_descr = bundle.getString("descr");
            title.setText(podcast_title);
            descr.setText(podcast_descr);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setSharedElementEnterTransition(new AutoTransition());
        }
        return podcastview;
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
