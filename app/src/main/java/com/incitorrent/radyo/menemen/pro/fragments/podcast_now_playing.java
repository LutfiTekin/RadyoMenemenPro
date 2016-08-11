package com.incitorrent.radyo.menemen.pro.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.incitorrent.radyo.menemen.pro.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class podcast_now_playing extends Fragment {


    public podcast_now_playing() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_podcast_now_playing, container, false);
    }

}
