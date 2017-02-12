package com.incitorrent.radyo.menemen.pro.fragments;


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;
import com.incitorrent.radyo.menemen.pro.utils.topicDB;


public class online extends Fragment {
    private static final String TAG = "Online";
    Context context;
    TextView textview;
    Toolbar toolbar;
    Menemen m;
    FirebaseDatabase database;
    DatabaseReference onlines;
    String TOPIC_ID = "0";
    public online() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View onlineView = inflater.inflate(R.layout.fragment_online, container, false);
        context = getActivity().getApplicationContext();
        m = new Menemen(context);
        if(getActivity() != null) {
            getActivity().setTitle(context.getString(R.string.online_members));
            toolbar = (android.support.v7.widget.Toolbar) getActivity().findViewById(R.id.toolbar);
        }
        textview = (TextView) onlineView.findViewById(R.id.onlineusers);
        database = FirebaseDatabase.getInstance();
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            TOPIC_ID = bundle.getString(topicDB._TOPICID);
            if(TOPIC_ID == null)
                TOPIC_ID = "0";
        }
        onlines = database.getReference("onlineusers").child("location").child(TOPIC_ID);
        onlines.orderByValue();
        return onlineView;
    }

    @Override
    public void onStart() {
        iAmOnline(true);
        onlines.addValueEventListener(trackonlineusers);
        super.onStart();
    }



    @Override
    public void onStop() {
        iAmOnline(false);
        onlines.removeEventListener(trackonlineusers);
        super.onStop();
    }

    ValueEventListener trackonlineusers = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if(dataSnapshot.getChildrenCount()<1){
                toolbar.setSubtitle("");
                textview.setText(R.string.online_user_none);
            }else if(dataSnapshot.getChildrenCount() == 1){
                toolbar.setSubtitle(R.string.toolbar_online_subtitle_one);
                textview.setText(R.string.online_user_none);
            }else{
                String users ="";
                for (DataSnapshot user : dataSnapshot.getChildren()) {
                   users = users + user.getKey() + "\n";
                }
                textview.setText(users);
                toolbar.setSubtitle(String.format(getActivity().getApplicationContext().getString(R.string.toolbar_online_subtitle), dataSnapshot.getChildrenCount()));

            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private void iAmOnline(boolean online) {
        try {
            if (online) {
                onlines.child(m.getUsername()).setValue(System.currentTimeMillis());
            } else {
                onlines.child(m.getUsername()).setValue(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
