package com.incitorrent.radyo.menemen.pro.fragments;


import android.app.Fragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.transition.Slide;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;

/**
 * A simple {@link Fragment} subclass.
 */
public class topics extends Fragment {

    Menemen m;
    Context context;

    public topics() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setRetainInstance(true);
        context = getActivity().getApplicationContext();
        m = new Menemen(context);
        if(getActivity() != null) getActivity().setTitle(getString(R.string.nav_topics));
        // Inflate the layout for this fragment
        View topicview = inflater.inflate(R.layout.fragment_topics, container, false);
        FloatingActionButton create = (FloatingActionButton) topicview.findViewById(R.id.create_fab);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment track_info = new topics_create();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    track_info.setEnterTransition(new Slide(Gravity.TOP));
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.Fcontent, track_info)
                        .addToBackStack(null)
                        .commit();
            }
        });
        return topicview;
    }

}
