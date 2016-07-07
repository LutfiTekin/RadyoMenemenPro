package com.incitorrent.radyo.menemen.pro.fragments;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.incitorrent.radyo.menemen.pro.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class track_info extends Fragment {


    public track_info() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_track_info, container, false);
    }

}
