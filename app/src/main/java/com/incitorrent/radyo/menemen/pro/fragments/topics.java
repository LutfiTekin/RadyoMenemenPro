package com.incitorrent.radyo.menemen.pro.fragments;


import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.firebase.messaging.FirebaseMessaging;
import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;
import com.incitorrent.radyo.menemen.pro.utils.WrapContentLinearLayoutManager;
import com.incitorrent.radyo.menemen.pro.utils.topicDB;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class topics extends Fragment {

    Menemen m;
    Context context;
    RecyclerView recyclerView;
    List<topic_objs> topicList;
    RequestQueue queue;
    String SELECTED_TOPIC_ID = "0";
    public topics() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setRetainInstance(true);
        context = getActivity().getApplicationContext();
        m = new Menemen(context);
        queue = Volley.newRequestQueue(context);
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
        recyclerView = (RecyclerView) topicview.findViewById(R.id.topicR);
        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new WrapContentLinearLayoutManager(getActivity().getApplicationContext()));
        stringRequest.setShouldCache(true);
        setRetainInstance(true);
        return topicview;
    }

    @Override
    public void onResume() {
        if(m.isFirstTime("loadTopicList")) {
            queue.add(stringRequest);
            try {
                FirebaseMessaging.getInstance().subscribeToTopic("newtopic");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            loadTopicsListFromDB();
        }
        super.onResume();
    }

    private void loadTopicsListFromDB() {
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                topicList = new ArrayList<>();
                final topicDB sql = m.getTopicDB();
                Cursor cursor = sql.listTopıcs();
                if(cursor == null || cursor.getCount()<1) {
                    sql.close();
                    queue.add(stringRequest);
                    return null;
                }
                cursor.moveToFirst();
                while(!cursor.isAfterLast()){
                    String id,title,descr,image,creator,tpc;
                    id = cursor.getString(cursor.getColumnIndex(topicDB._TOPICID));
                    title = cursor.getString(cursor.getColumnIndex(topicDB._TITLE));
                    descr = cursor.getString(cursor.getColumnIndex(topicDB._DESCR));
                    image = cursor.getString(cursor.getColumnIndex(topicDB._IMAGEURL));
                    creator = cursor.getString(cursor.getColumnIndex(topicDB._CREATOR));
                    tpc = cursor.getString(cursor.getColumnIndex(topicDB._TOPICSTR));
                    topicList.add(new topic_objs(id,title,descr,image,creator,tpc));
                    cursor.moveToNext();
                }
                cursor.close();
                sql.close();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if(topicList != null && topicList.size()>0){
                    recyclerView.setAdapter(new TopicAdapter(topicList));
                }
                super.onPostExecute(aVoid);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    StringRequest stringRequest = new StringRequest(Request.Method.GET, RadyoMenemenPro.MENEMEN_TOPICS_LIST,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String response) {
                        loadTopicsList(response);
                        Log.d("VOLL",response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public Priority getPriority() {
                return Priority.IMMEDIATE;
            }

            @Override
            public RetryPolicy getRetryPolicy() {
                return m.menemenRetryPolicy();
            }

        };


    void loadTopicsList(final String response) {
        if(response == null) return;
        new AsyncTask<Void,Void,Void>(){

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    topicList = new ArrayList<>();
                    JSONArray arr = new JSONObject(response).getJSONArray("topics");
                    JSONObject c;
                    for(int i = 0;i<arr.getJSONArray(0).length();i++){
                        JSONArray innerJarr = arr.getJSONArray(0);
                        c = innerJarr.getJSONObject(i);
                        String id,title,descr,image,creator,tpc;
                        id = c.getString("id");
                        title = c.getString("t");
                        descr = c.getString("d");
                        image = c.getString("i");
                        creator = c.getString("c");
                        tpc = c.getString("tpc");
                        m.getTopicDB().addtoTopicHistory(new topicDB.TOPIC(id,tpc,creator,"0",title,descr,image));
                        topicList.add(new topic_objs(id,title,descr,image,creator,tpc));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if(topicList != null && topicList.size()>0){
                    recyclerView.setAdapter(new TopicAdapter(topicList));
                }
                super.onPostExecute(aVoid);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.TPCViewHolder> {
        Context context;
        List<topic_objs> topicList;


        class TPCViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            TextView title,description,creator;
            ImageView image;
            ToggleButton toggle;
            TPCViewHolder(View itemView) {
                super(itemView);
                title = (TextView) itemView.findViewById(R.id.t_title);
                description = (TextView) itemView.findViewById(R.id.t_descr);
                creator = (TextView) itemView.findViewById(R.id.t_creator);
                image = (ImageView) itemView.findViewById(R.id.t_image);
                toggle = (ToggleButton) itemView.findViewById(R.id.toggleButton);
                title.setOnClickListener(this);
                image.setOnClickListener(this);
                toggle.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                if(view == image || view == title){
                    //TODO eğer grupta değilse katıl butonunu göster, gruptaysa gruba git
                if(!topicList.get(getAdapterPosition()).creator.equals(m.getUsername()))
                    toggle.setVisibility(View.VISIBLE);
                }else if(view == toggle){
                    boolean isChecked = ((ToggleButton) view).isChecked();
                    SELECTED_TOPIC_ID = topicList.get(getAdapterPosition()).id;
                    queue.add((isChecked) ? join : leave);
                }
            }
        }

        TopicAdapter(List<topic_objs> topicList) {
            this.topicList = topicList;
        }

        @Override
        public TPCViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.topic_list_item, viewGroup, false);
            TPCViewHolder pvh = new TPCViewHolder(v);
            context = viewGroup.getContext();
            return pvh;
        }

        @Override
        public int getItemCount() {
            return topicList.size();
        }

        @Override
        public void onBindViewHolder(TPCViewHolder vh, int i) {
            topic_objs t = topicList.get(i);
            vh.title.setText(t.title);
            vh.description.setText(t.description);
            vh.creator.setText(t.creator);
            Glide.with(getActivity())
                    .load(RadyoMenemenPro.CAPS_IMAGES_PATH + t.image)
                    .placeholder(R.drawable.default_image)
                    .error(R.drawable.default_image)
                    .into(vh.image);
        }


        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }


    }


    class topic_objs{
        String id,title,description,image,creator,topicstr;

        topic_objs(String id, String title, String description, String image, String creator, String topicstr) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.image = image;
            this.creator = creator;
            this.topicstr = topicstr;
        }
    }

    StringRequest join = new StringRequest(Request.Method.POST, RadyoMenemenPro.MENEMEN_TOPICS_JOIN,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("VOLLRESP",response);
                }
            },
            new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {

        }
    }){
        @Override
        protected Map<String, String> getParams() throws AuthFailureError {
            Map<String, String> dataToSend = m.getAuthMap();
            dataToSend.put("topicid",SELECTED_TOPIC_ID);
            return dataToSend;
        }

        @Override
        public RetryPolicy getRetryPolicy() {
            return m.menemenRetryPolicy();
        }
    };
    StringRequest leave = new StringRequest(Request.Method.POST, RadyoMenemenPro.MENEMEN_TOPICS_LEAVE,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("VOLLRESP",response);
                }
            },
            new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {

        }
    }){
        @Override
        protected Map<String, String> getParams() throws AuthFailureError {
            Map<String, String> dataToSend = m.getAuthMap();
            dataToSend.put("topicid",SELECTED_TOPIC_ID);
            return dataToSend;
        }

        @Override
        public RetryPolicy getRetryPolicy() {
            return m.menemenRetryPolicy();
        }
    };
}
