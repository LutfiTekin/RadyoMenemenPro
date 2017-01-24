package com.incitorrent.radyo.menemen.pro.fragments;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
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
    ImageView avatar;
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
        avatar = (ImageView) userview.findViewById(R.id.avatar);
        progressbar = (ProgressBar) userview.findViewById(R.id.progressbar);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            nick = bundle.getString(RadyoMenemenPro.NICK, getString(android.R.string.unknownName));
            toolbar.setTitle(nick.toUpperCase());
        }
        queue.add(getAvatar);
    return userview;
    }




    void setupPm(String topicid){
        TOPIC_ID = topicid;
        Log.d("USERPM",TOPIC_ID);
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
            getActivity().setTitle(getString(R.string.pm));
            if(!imageurl.equals("default")){
                setAvatarImage();
            }
        }
        progressbar.setVisibility(View.GONE);

    }

    private void setAvatarImage() {
        new AsyncTask<Void,Void,Integer[]>(){
            final int primaryText = ContextCompat.getColor(context,R.color.textColorPrimary);
            Bitmap bitmap = null;



            @Override
            protected Integer[] doInBackground(Void... voids) {
                try {
                    if(!imageurl.equals("default"))
                        bitmap = Glide.with(getActivity())
                                .load(imageurl)
                                .asBitmap()
                                .into(Target.SIZE_ORIGINAL,Target.SIZE_ORIGINAL)
                                .get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(bitmap!=null)
                    try {
                    Palette palette = Palette.from(bitmap).generate();
                    int title = palette.getVibrantSwatch() == null ? primaryText : palette.getVibrantSwatch().getTitleTextColor();
                    return new Integer[]{title};
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Integer[] colors) {
                if(bitmap!=null)
                    avatar.setImageBitmap(bitmap);
                if(colors!=null)
                    toolbar.setTitleTextColor(colors[0]);
                super.onPostExecute(colors);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    String pmTopicTitle(){
        return RadyoMenemenPro.PM + m.getUsername() + "+" + nick;
    }
    StringRequest getAvatar = new StringRequest(Request.Method.POST, RadyoMenemenPro.SEARCH_USER_AVATAR,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    imageurl = response.equals(RESPONSE_ERROR) ? "default" : response;
                    if(!nick.equals(m.getUsername()))
                        queue.add(create);
                    else getFragmentManager().beginTransaction().replace(R.id.Fcontent,new mp_transactions_list()).commit();
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
                            if(getActivity() != null)
                                getActivity().onBackPressed();
                            break;
                        default:
                                try {
                                    Log.d("USER PM ",response);
                                    JSONObject j = new JSONObject(response).getJSONArray("info").getJSONObject(0);
                                    FirebaseMessaging.getInstance().subscribeToTopic(j.getString(topicDB._TOPICSTR));
                                    m.getTopicDB().addtoTopicHistory(new topicDB.TOPIC(j.getString(topicDB._TOPICID), j.getString(topicDB._TOPICSTR), m.getUsername(), topicDB.JOINED, pmTopicTitle(), RadyoMenemenPro.PM, "default",topicDB.PRIVATE_TOPIC ));
                                    setupPm(j.getString(topicDB._TOPICID));
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
            dataToSend.put("type", topicDB.PRIVATE_TOPIC);
            dataToSend.put("image", imageurl);
            return dataToSend;
        }

        @Override
        public RetryPolicy getRetryPolicy() {
            return super.getRetryPolicy();
        }
    };

}
