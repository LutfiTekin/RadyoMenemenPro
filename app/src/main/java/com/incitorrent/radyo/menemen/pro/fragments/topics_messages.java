package com.incitorrent.radyo.menemen.pro.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.incitorrent.radyo.menemen.pro.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class topics_messages extends Fragment {


    public topics_messages() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.topics_messages, container, false);
    }

}
