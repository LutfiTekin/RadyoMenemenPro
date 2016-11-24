package com.incitorrent.radyo.menemen.pro.fragments;


import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 */
public class haykir extends Fragment implements View.OnClickListener {
    private static final String TAG = "HAYKIRFRAGMENT";
    EditText editText;
    FloatingActionButton send;
    RecyclerView shoutRV;
    TextView emptyview;
    Menemen m;
    ScheduledThreadPoolExecutor exec;
    List<Shout_Objects> ShoutList;
    ShoutAdapter ShoutAdapter;
    public haykir() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        m = new Menemen(getActivity().getApplicationContext());
        View haykirview = inflater.inflate(R.layout.fragment_haykir, container, false);
        if(getActivity()!=null) getActivity().setTitle(getString(R.string.nav_haykir)); //Toolbar title
        editText = (EditText) haykirview.findViewById(R.id.ETmesaj);
        send = (FloatingActionButton) haykirview.findViewById(R.id.mesaj_gonder_button);
        shoutRV = (RecyclerView) haykirview.findViewById(R.id.shoutRV);
        send.setOnClickListener(this);
        ShoutList = new ArrayList<>();
        shoutRV.setHasFixedSize(true);
        shoutRV.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        ShoutAdapter = new ShoutAdapter(ShoutList);
        emptyview = (TextView) haykirview.findViewById(R.id.haykir_emptyview);
        editText.setOnEditorActionListener(new EditText.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if(editText.getText().length()>0) {
                        if (Build.VERSION.SDK_INT >= 11)
                            postHaykir(editText.getText().toString());
                        editText.setText("");
                    }
                    return true;
                }
                return false;
            }
        });
            send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    postHaykir(editText.getText().toString());
                    editText.setText("");
                }
            });

        exec = new ScheduledThreadPoolExecutor(1);
        exec.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                new initShout().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        },0,RadyoMenemenPro.MUSIC_INFO_SERVICE_INTERVAL /4, TimeUnit.SECONDS);
        setRetainInstance(true);
        return haykirview;
    }

    private void postHaykir(final String mesaj) {
        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        StringRequest postRequest = new StringRequest(Request.Method.POST, RadyoMenemenPro.HAYKIR_LINK,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject j = new JSONObject(response).getJSONArray("post").getJSONObject(0);
                            if(!j.get("status").equals("ok"))
                                Toast.makeText(getActivity().getApplicationContext(), R.string.error_occured, Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity().getApplicationContext(), R.string.error_occured, Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams(){
                HashMap<String, String> dataToSend = new HashMap<>();
                dataToSend.put("nick", m.oku("username"));
                dataToSend.put("haykir", mesaj);
                return dataToSend;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(10000,10,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(postRequest);
    }

    @Override
    public void onResume() {

        super.onResume();
    }

    @Override
    public void onClick(View v) {

    }
    class initShout extends AsyncTask<Void,Void,Void> {
        @Override
        protected void onPreExecute() {
            ShoutList = new ArrayList<>();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
            StringRequest postRequest = new StringRequest(Request.Method.POST, RadyoMenemenPro.HAYKIRMALAR,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONArray arr = new JSONObject(response).getJSONArray("shout");
                                JSONObject c;
                                for(int i = 0;i<arr.getJSONArray(0).length();i++){
                                    String nick,mesaj,zaman;
                                    JSONArray innerJarr = arr.getJSONArray(0);
                                    c = innerJarr.getJSONObject(i);
                                    nick = c.getString("nick");
                                    mesaj = c.getString("haykirma");
                                    zaman = c.getString("zaman");
                                    ShoutList.add(new Shout_Objects(nick,mesaj,zaman));
                                }
                                if(ShoutList!=null && ShoutList.size() > 0) ShoutAdapter = new ShoutAdapter(ShoutList);
                                shoutRV.setAdapter(ShoutAdapter);
                                emptyview.setVisibility((ShoutAdapter.getItemCount()<1) ? View.VISIBLE : View.GONE);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }){
                @Override
                protected Map<String, String> getParams() {
                    return m.getAuthMap();
                }

                @Override
                public Priority getPriority() {
                    return Priority.IMMEDIATE;
                }
            };
            queue.add(postRequest);
            return null;
        }

    }

    public class Shout_Objects {
        String nick,shout,zaman;
        Shout_Objects(String nick, String shout, String zaman) {
            this.nick = nick;
            this.shout = shout;
            this.zaman = zaman;
        }
    }
    public class ShoutAdapter extends RecyclerView.Adapter<ShoutAdapter.PersonViewHolder> {
        Context context;
        List<Shout_Objects> ShoutList;

        public class PersonViewHolder extends RecyclerView.ViewHolder{
            TextView nick,shout,zaman;
            CardView shoutcard;
            PersonViewHolder(View itemView) {
                super(itemView);
                nick = (TextView) itemView.findViewById(R.id.username);
                shout = (TextView) itemView.findViewById(R.id.shout);
                zaman = (TextView) itemView.findViewById(R.id.zaman);
                shoutcard = (CardView) itemView.findViewById(R.id.shoutcard);

            }

        }
        ShoutAdapter(List<Shout_Objects> ShoutList){
            this.ShoutList = ShoutList;
        }

        @Override
        public PersonViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.shout_item, viewGroup,false);
            PersonViewHolder pvh = new PersonViewHolder(v);
            context = viewGroup.getContext();
            return pvh;
        }
        @Override
        public int getItemCount() {
            return ShoutList.size();
        }

        @Override
        public void onBindViewHolder(PersonViewHolder personViewHolder, int i) {
            if(ShoutList.get(i).nick.equals("Dj")) personViewHolder.shoutcard.setCardBackgroundColor(ContextCompat.getColor(context,R.color.blueAccent));
            else personViewHolder.shoutcard.setCardBackgroundColor(Color.WHITE);
            personViewHolder.nick.setText((ShoutList.get(i).nick.equals("Dj")) ? ShoutList.get(i).nick : context.getString(R.string.me));
            String title = ShoutList.get(i).shout;
            personViewHolder.shout.setText(Menemen.fromHtmlCompat(title));
            personViewHolder.zaman.setText(Menemen.getTimeAgo(ShoutList.get(i).zaman,context));
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }
    }

    @Override
    public void onDestroyView() {
        exec.shutdown();
        super.onDestroyView();
    }
}
