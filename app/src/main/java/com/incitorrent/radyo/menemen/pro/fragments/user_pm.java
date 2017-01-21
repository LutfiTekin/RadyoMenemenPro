package com.incitorrent.radyo.menemen.pro.fragments;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessaging;
import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;
import com.incitorrent.radyo.menemen.pro.utils.topicDB;

import org.json.JSONObject;

import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class user_pm extends Fragment {
    Context context;
    Menemen m;
    String nick;
    Toolbar toolbar;
    RequestQueue queue;
    ProgressBar progressbar;
    String TOPIC_ID,imageurl;
    private static final String RESPONSE_ERROR = "1";
    private static final String RESPONSE_DUPLICATE = "2";
    public user_pm() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getActivity().getApplicationContext();
        queue = Volley.newRequestQueue(context);
        m = new Menemen(context);
        // Inflate the layout for this fragment
        View userview = inflater.inflate(R.layout.fragment_user_pm, container, false);

        toolbar = (Toolbar) userview.findViewById(R.id.toolbar);

        progressbar = (ProgressBar) userview.findViewById(R.id.progressbar);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            nick = bundle.getString(RadyoMenemenPro.NICK, getString(android.R.string.unknownName));
            toolbar.setTitle(nick);
        }

    return userview;
    }

    @Override
    public void onStart() {
        if(!m.bool_oku(pmTopicTitle())){

        }
        //TODO Check if pm is setup
        //TODO Create PM topic
        super.onStart();
    }

    @Override
    public void onResume() {
        //TODO Setup ui
        super.onResume();
    }

    void setupPm(){
        Fragment topic = new sohbet();
        Bundle topicbundle = new Bundle();
        topicbundle.putString(topicDB._TOPICID, TOPIC_ID);
        topic.setArguments(topicbundle);
        getFragmentManager().popBackStack("sohbet", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.pm_container,topic)
                .addToBackStack(TOPIC_ID)
                .commit();
        if(getActivity() != null) {
            ((Toolbar)getActivity().findViewById(R.id.toolbar)).setSubtitle("");
            //TODO hide subtitle
            getActivity().setTitle(getString(R.string.pm));
        }
    }

    String pmTopicTitle(){
        return RadyoMenemenPro.PM + m.getUsername() + nick;
    }
    StringRequest getAvatarAndCreate = new StringRequest(Request.Method.POST, RadyoMenemenPro.MENEMEN_TOPICS_CREATE,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    imageurl = response.equals(RESPONSE_ERROR) ? "default" : response;
                    queue.add(create);
                }
            }, new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {

        }
    }){
        @Override
        protected Map<String, String> getParams() throws AuthFailureError {
            Map<String,String> dataToSend = m.getAuthMap();
            dataToSend.put(SearchManager.QUERY, nick);
            return dataToSend;
        }

        @Override
        public RetryPolicy getRetryPolicy() {
            return new RetryPolicy() {
                @Override
                public int getCurrentTimeout() {
                    return 6000;
                }

                @Override
                public int getCurrentRetryCount() {
                    return 0;
                }

                @Override
                public void retry(VolleyError error) throws VolleyError {
                        if(imageurl == null) imageurl = "default";
                        queue.add(create);
                    throw new VolleyError("NO RETRY");
                }
            };
        }
    };

    StringRequest create = new StringRequest(Request.Method.POST, RadyoMenemenPro.MENEMEN_TOPICS_CREATE,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    switch (response) {
                        case RESPONSE_ERROR:
                            Toast.makeText(context, R.string.error_occured, Toast.LENGTH_SHORT).show();
                            break;
                        case RESPONSE_DUPLICATE:
                            Toast.makeText(context, R.string.topics_error_duplicate, Toast.LENGTH_SHORT).show();
                            break;
                        default:

                                Toast.makeText(context, R.string.topics_new_success, Toast.LENGTH_SHORT).show();
                                try {
                                    JSONObject j = new JSONObject(response).getJSONArray("info").getJSONObject(0);
                                    FirebaseMessaging.getInstance().subscribeToTopic(j.getString(topicDB._TOPICSTR));
                                    m.getTopicDB().addtoTopicHistory(new topicDB.TOPIC(j.getString(topicDB._TOPICID), j.getString(topicDB._TOPICSTR), m.getUsername(), "1", pmTopicTitle(), RadyoMenemenPro.PM, imageurl,"2" ));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                    }
                }
            }, new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {

        }
    }){
        @Override
        protected Map<String, String> getParams() {
            Map<String, String> dataToSend = m.getAuthMap();
            dataToSend.put("title", pmTopicTitle());
            dataToSend.put("descr", RadyoMenemenPro.PM);
            dataToSend.put("type", "2"); //Private Type
            dataToSend.put("image", imageurl);
            return dataToSend;
        }

        @Override
        public RetryPolicy getRetryPolicy() {
            return super.getRetryPolicy();
        }
    };

}
