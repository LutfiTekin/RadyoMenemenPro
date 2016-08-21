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
public class galeri extends Fragment {


    public galeri() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View galeri = inflater.inflate(R.layout.fragment_galeri, container, false);
        if(getActivity() != null) getActivity().setTitle(getString(R.string.nav_galeri));
        return galeri;
    }

}
