package com.incitorrent.radyo.menemen.pro.fragments;


import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.transition.Slide;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
    ProgressBar progressBar;
    private final static String RESPONSE_AUTH_FAILED = "1";
    private final static String RESPONSE_SUCCESS = "2";
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
                Fragment topics_create = new topics_create();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    topics_create.setEnterTransition(new Slide(Gravity.TOP));
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.Fcontent, topics_create)
                        .addToBackStack(null)
                        .commit();
            }
        });
        recyclerView = (RecyclerView) topicview.findViewById(R.id.topicR);
        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new WrapContentLinearLayoutManager(getActivity().getApplicationContext()));
        progressBar = (ProgressBar) topicview.findViewById(R.id.progressbar);

        stringRequest.setShouldCache(true);
        setRetainInstance(true);
        return topicview;
    }

    @Override
    public void onResume() {
        progressBar.setVisibility(View.VISIBLE);
            queue.add(stringRequest);
            try {
                FirebaseMessaging.getInstance().subscribeToTopic("newtopic");
            } catch (Exception e) {
                e.printStackTrace();
            }
        super.onResume();
    }

    private void loadTopicsListFromDB() {
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                topicList = new ArrayList<>();
                final topicDB sql = m.getTopicDB();
                Cursor cursor;
                cursor = sql.liswOwnTopics();
                if(cursor!=null && cursor.getCount()>0){
                    getFromCursortoList(cursor);
                }

                cursor = sql.listTopıcs();
                if(cursor == null || cursor.getCount()<1) {
                    sql.close();
                    queue.add(stringRequest);
                    return null;
                }
                getFromCursortoList(cursor);

                cursor = sql.listNewTopics();
                if(cursor != null && cursor.getCount()>0){
                    getFromCursortoList(cursor);
                    cursor.close();
                }
                if(cursor!=null && !cursor.isClosed()) cursor.close();
                sql.close();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if(topicList != null && topicList.size()>0){
                    recyclerView.setAdapter(new TopicAdapter(topicList));
                }
                progressBar.setVisibility(View.GONE);
                super.onPostExecute(aVoid);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void getFromCursortoList(Cursor cursor) {
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            String id,title,descr,image,creator,tpc;
            try {
                id = cursor.getString(cursor.getColumnIndex(topicDB._TOPICID));
                title = cursor.getString(cursor.getColumnIndex(topicDB._TITLE));
                descr = cursor.getString(cursor.getColumnIndex(topicDB._DESCR));
                image = cursor.getString(cursor.getColumnIndex(topicDB._IMAGEURL));
                creator = cursor.getString(cursor.getColumnIndex(topicDB._CREATOR));
                tpc = cursor.getString(cursor.getColumnIndex(topicDB._TOPICSTR));
                if(!title.startsWith(RadyoMenemenPro.PM))
                    topicList.add(new topic_objs(id,title,descr,image,creator,tpc));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.moveToNext();
            }
        }
    }

    StringRequest stringRequest = new StringRequest(Request.Method.POST, RadyoMenemenPro.MENEMEN_TOPICS_LIST,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String response) {
                        loadTopicsList(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            loadTopicsListFromDB();
            }
        }){
            @Override
            public Priority getPriority() {
                return Priority.IMMEDIATE;
            }

            @Override
            public RetryPolicy getRetryPolicy() {
                return new RetryPolicy() {
                    @Override
                    public int getCurrentTimeout() {
                        return 5000;
                    }

                    @Override
                    public int getCurrentRetryCount() {
                        return 2;
                    }

                    @Override
                    public void retry(VolleyError error) throws VolleyError {
                        error.printStackTrace();
                    }
                };
            }

        @Override
        protected Map<String, String> getParams() throws AuthFailureError {
            return m.getAuthMap();
        }
    };


    void loadTopicsList(final String response) {
        if(response == null) return;
        m.getTopicDB().refreshTopics();
        new AsyncTask<Void,Void,Void>(){

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    topicList = new ArrayList<>();
                    JSONArray arr = new JSONObject(response).getJSONArray(topicDB.TOPICS_TABLE);
                    JSONObject c;
                    for(int i = 0;i<arr.getJSONArray(0).length();i++){
                        JSONArray innerJarr = arr.getJSONArray(0);
                        c = innerJarr.getJSONObject(i);
                        String id,title,descr,image,creator,tpc,joined,type;
                        id = c.getString("id");
                        title = c.getString("t");
                        descr = c.getString("d");
                        image = c.getString("i");
                        creator = c.getString("c");
                        tpc = c.getString("tpc");
                        joined = c.getString("j");
                        type = "1"; //Listing public topics only
                        m.getTopicDB().addtoTopicHistory(new topicDB.TOPIC(id,tpc,creator,joined,title,descr,image,type));
                        if(joined.equals("1"))
                            FirebaseMessaging.getInstance().subscribeToTopic(tpc);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                loadTopicsListFromDB();
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
            Button toggle;
            TPCViewHolder(View itemView) {
                super(itemView);
                title = (TextView) itemView.findViewById(R.id.t_title);
                description = (TextView) itemView.findViewById(R.id.t_descr);
                creator = (TextView) itemView.findViewById(R.id.t_creator);
                image = (ImageView) itemView.findViewById(R.id.t_image);
                toggle = (Button) itemView.findViewById(R.id.toggleButton);
                title.setOnClickListener(this);
                image.setOnClickListener(this);
                toggle.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                SELECTED_TOPIC_ID = topicList.get(getAdapterPosition()).id;
                if(view == image || view == title){
                    if(m.getTopicDB().isJoined(SELECTED_TOPIC_ID) || topicList.get(getAdapterPosition()).creator.equals(m.getUsername()))
                        //User is in the topic or created it go to selected topic
                        openTopic();
                    else if(!topicList.get(getAdapterPosition()).creator.equals(m.getUsername()))
                        //User is not joined the group show the join/leave toggle
                        toggle.setVisibility(View.VISIBLE);
                }else if(view == toggle){
                    queue.add(join);
                    queue.start();
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
                    switch (response) {
                        case RESPONSE_AUTH_FAILED:
                            if(context!=null)
                                Toast.makeText(context, R.string.toast_auth_error, Toast.LENGTH_SHORT).show();
                            break;
                        case RESPONSE_SUCCESS:
                            m.getTopicDB().join(SELECTED_TOPIC_ID);
                            openTopic();
                            break;
                    }
                }
            },null){
        @Override
        protected Map<String, String> getParams() throws AuthFailureError {
            Map<String, String> dataToSend = m.getAuthMap();
            dataToSend.put(topicDB._TOPICID,SELECTED_TOPIC_ID);
            return dataToSend;
        }

        @Override
        public Priority getPriority() {
            return Priority.IMMEDIATE;
        }

        @Override
        public RetryPolicy getRetryPolicy() {
            return retrypolicy;
        }
    };

    private void openTopic() {
        Fragment topic = new sohbet();
        Bundle bundle = new Bundle();
        bundle.putString(topicDB._TOPICID, SELECTED_TOPIC_ID);
        topic.setArguments(bundle);
        getFragmentManager().popBackStack("sohbet", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.Fcontent,topic)
                .addToBackStack("topic"+SELECTED_TOPIC_ID)
                .commit();
    }


//    StringRequest leave = new StringRequest(Request.Method.POST, RadyoMenemenPro.MENEMEN_TOPICS_LEAVE,
//            new Response.Listener<String>() {
//                @Override
//                public void onResponse(String response) {
//                    switch (response) {
//                        case RESPONSE_AUTH_FAILED:
//                            if(context!=null)
//                                Toast.makeText(context, R.string.toast_auth_error, Toast.LENGTH_SHORT).show();
//                            break;
//                        case RESPONSE_SUCCESS:
//                            try {
//                                String topicstr = m.getTopicDB().getTopicSTR(SELECTED_TOPIC_ID);
//                               if(topicstr!=null)
//                                   FirebaseMessaging.getInstance().unsubscribeFromTopic(topicstr);
//                                m.getTopicDB().leave(SELECTED_TOPIC_ID);
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                            break;
//                    }
//                }
//            },null){
//        @Override
//        protected Map<String, String> getParams() throws AuthFailureError {
//            Map<String, String> dataToSend = m.getAuthMap();
//            dataToSend.put(topicDB._TOPICID,SELECTED_TOPIC_ID);
//            return dataToSend;
//        }
//
//        @Override
//        public Priority getPriority() {
//            return Priority.IMMEDIATE;
//        }
//
//        @Override
//        public RetryPolicy getRetryPolicy() {
//            return retrypolicy;
//        }
//    };
private RetryPolicy retrypolicy = new RetryPolicy() {
    @Override
    public int getCurrentTimeout() {
        return 5000;
    }

    @Override
    public int getCurrentRetryCount() {
        return 5;
    }

    @Override
    public void retry(VolleyError error) throws VolleyError {
        error.printStackTrace();
    }
};

}
