package com.incitorrent.radyo.menemen.pro.fragments;


import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.incitorrent.radyo.menemen.pro.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class haykir extends Fragment implements View.OnClickListener {
    EditText editText;
    FloatingActionButton send;
    RecyclerView shoutRV;

    public haykir() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View haykirview = inflater.inflate(R.layout.fragment_haykir, container, false);
        editText = (EditText) haykirview.findViewById(R.id.ETmesaj);
        send = (FloatingActionButton) haykirview.findViewById(R.id.mesaj_gonder_button);
        shoutRV = (RecyclerView) haykirview.findViewById(R.id.shoutRV);
        send.setOnClickListener(this);
        return haykirview;
    }

    @Override
    public void onClick(View v) {

    }
}
