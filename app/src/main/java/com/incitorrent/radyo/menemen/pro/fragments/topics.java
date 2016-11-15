package com.incitorrent.radyo.menemen.pro.fragments;


import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Slide;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class topics extends Fragment {

    Menemen m;
    Context context;
    RecyclerView recyclerView;
    List<topic_objs> topicList;
    RequestQueue queue;
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
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        queue.add(stringRequest);
        setRetainInstance(true);
        return topicview;
    }

    @Override
    public void onResume() {
        super.onResume();
    }


        StringRequest stringRequest = new StringRequest(Request.Method.GET, RadyoMenemenPro.MENEMEN_TOPICS_LIST,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String response) {
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
                                    topicList.add(new topic_objs(c.getString("t"),c.getString("d"),c.getString("i"),c.getString("c"),c.getString("tpc")));
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




    public class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.TPCViewHolder> {
        Context context;
        List<topic_objs> topicList;


        class TPCViewHolder extends RecyclerView.ViewHolder {
            TextView title,description,creator;
            ImageView image;
            TPCViewHolder(View itemView) {
                super(itemView);
                title = (TextView) itemView.findViewById(R.id.t_title);
                description = (TextView) itemView.findViewById(R.id.t_descr);
                creator = (TextView) itemView.findViewById(R.id.t_creator);
                image = (ImageView) itemView.findViewById(R.id.t_image);
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
        String title,description,image,creator,topicstr;

        public topic_objs(String title, String description, String image, String creator, String topicstr) {
            this.title = title;
            this.description = description;
            this.image = image;
            this.creator = creator;
            this.topicstr = topicstr;
        }
    }
}
