package com.incitorrent.radyo.menemen.pro.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;

import java.util.List;

public class topics_messages extends Fragment {
    Menemen m;
    Context context;
    RecyclerView recyclerView;
    List<String> sohbetList;

    public topics_messages() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View tm = inflater.inflate(R.layout.topics_messages, container, false);
        context = getActivity().getApplicationContext();
        m = new Menemen(context);
        return tm;
    }


    class Topic_Msg_Objects{
        String tid,id,nick,post,time;

        Topic_Msg_Objects(String tid, String id, String nick, String post, String time) {
            this.tid = tid;
            this.id = id;
            this.nick = nick;
            this.post = post;
            this.time = time;
        }
    }
}
