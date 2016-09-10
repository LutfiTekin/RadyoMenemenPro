package com.incitorrent.radyo.menemen.pro.fragments;


import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.services.FIREBASE_CM_SERVICE;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;
import com.incitorrent.radyo.menemen.pro.utils.trackonlineusersDB;

/**
 * A simple {@link Fragment} subclass.
 */
public class online extends Fragment {
    private static final String TAG = "Online";
    trackonlineusersDB sql;
    Context context;
    BroadcastReceiver receiver;
    TextView textview;
    Toolbar toolbar;
    Menemen m;
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
        sql = new trackonlineusersDB(context, null, null, 1);
        textview = (TextView) onlineView.findViewById(R.id.onlineusers);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent!=null) {
                    String action = intent.getAction();
                    if(action.equals(FIREBASE_CM_SERVICE.USERS_ONLINE_BROADCAST_FILTER)){
                        int count = intent.getExtras().getInt("count",0);
                        if(count > 0){
                            //Online Üyeler
                            new setOnlineUsers().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                        }
                    }
                }
            }
        };
        return onlineView;
    }

    @Override
    public void onStart() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(FIREBASE_CM_SERVICE.USERS_ONLINE_BROADCAST_FILTER);
        LocalBroadcastManager.getInstance(context).registerReceiver((receiver),filter);
        new setOnlineUsers().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        super.onStart();
    }



    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
        super.onStop();
    }

    class setOnlineUsers extends AsyncTask<Void,Void,String>{
        @Override
        protected String doInBackground(Void... voids) {
            try {
                Cursor cursor = sql.getOnlineUserList(m.getUsername());
                if(cursor == null) return null;
                cursor.moveToFirst();
                String users ="";
                while (!cursor.isAfterLast()){
                    String nick = cursor.getString(cursor.getColumnIndex(trackonlineusersDB._NICK));
                    users = users + nick + "\n";
                    cursor.moveToNext();
                }
                cursor.close();
                return users;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String res) {
            if(res != null && res.length()>0){
                textview.setText(res);
            }
            try {
                final int count = sql.getOnlineUserCount();
                if(toolbar!=null) {
                    if (count > 0) {
                        if (count == 1) {
                            toolbar.setSubtitle(R.string.toolbar_online_subtitle_one);
                            textview.setText(R.string.online_user_none);
                        }else
                            toolbar.setSubtitle(String.format(getActivity().getApplicationContext().getString(R.string.toolbar_online_subtitle), count));
                    } else {
                        toolbar.setSubtitle("");
                        textview.setText(R.string.online_user_none);
                    }
                }
                sql.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            super.onPostExecute(res);
        }
    }

}
