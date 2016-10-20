package com.incitorrent.radyo.menemen.pro.fragments;


import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class olan_biten extends Fragment {

//    private static final String TAG = "OLANBITEN";
    Context context;
    RecyclerView recyclerView;
    TextView title,content,time,author;
    List<ob_objs> OBList;
    Menemen m;

    public olan_biten() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
       context = getActivity().getApplicationContext();
        super.onCreate(savedInstanceState);
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
    View obview =  inflater.inflate(R.layout.fragment_olan_biten, container, false);
        if(getActivity()!=null) getActivity().setTitle(getString(R.string.news)); //Toolbar title
        recyclerView = (RecyclerView) obview.findViewById(R.id.obR);
        title = (TextView) obview.findViewById(R.id.ob_title);
        content = (TextView) obview.findViewById(R.id.ob_content);
        time = (TextView) obview.findViewById(R.id.ob_zaman);
        author = (TextView) obview.findViewById(R.id.ob_yazan);
        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(false);
        if(getResources().getBoolean(R.bool.xlarge_landscape_mode))
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(4,StaggeredGridLayoutManager.VERTICAL));
        else  if(getResources().getBoolean(R.bool.landscape_mode))
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        else
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        m = new Menemen(context);
        olanBiten();
        return obview;
    }
    private void olanBiten(){
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest request = new StringRequest(Request.Method.GET, RadyoMenemenPro.OLAN_BITEN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String response) {
                        new AsyncTask<Void,Void,Void>(){
                            @Override
                            protected Void doInBackground(Void... params) {
                                try {
                                    OBList = new ArrayList<>();
                                    if(response ==null || response.equals("yok")) return null;
                                    JSONArray arr = new JSONObject(response).getJSONArray("olan_biten");
                                    JSONObject c;
                                    for(int i = 0;i<arr.getJSONArray(0).length();i++){
                                        JSONArray innerJarr = arr.getJSONArray(0);
                                        c = innerJarr.getJSONObject(i);
                                        OBList.add(new ob_objs(c.getString("title"),c.getString("content"),c.getString("author"),c.getString("time")));
                                    }
                                    if(m.isInternetAvailable()) {
                                        m.kaydet(RadyoMenemenPro.SAVEDOB, arr.getJSONArray(0).getJSONObject(0).getString("time"));
                                        m.kaydet(RadyoMenemenPro.OBCACHE, response);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                //Menü düğmesini güncelle
                                if(getActivity()!=null) {
                                    final NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);
                                    final TextView badge = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().
                                            findItem(R.id.nav_olanbiten));
                                    m.setBadge(badge, "");
                                }
                                recyclerView.setAdapter(new OBAdapter(OBList));
                                super.onPostExecute(aVoid);
                            }
                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                },null){
            @Override
            public Priority getPriority() {
                return Priority.IMMEDIATE;
            }
        };
        queue.add(request);
    }




    public class OBAdapter extends RecyclerView.Adapter<OBAdapter.PersonViewHolder> {
        Context context;
        List<ob_objs> OBList;


        class PersonViewHolder extends RecyclerView.ViewHolder {
            TextView title,content,author,time;
            CardView obcard;
            PersonViewHolder(View itemView) {
                super(itemView);
                title = (TextView) itemView.findViewById(R.id.ob_title);
                content = (TextView) itemView.findViewById(R.id.ob_content);
                author = (TextView) itemView.findViewById(R.id.ob_yazan);
                time = (TextView) itemView.findViewById(R.id.ob_zaman);
                obcard = (CardView) itemView.findViewById(R.id.obC);
            }
        }

        OBAdapter(List<ob_objs> OBList) {
            this.OBList = OBList;
        }

        @Override
        public PersonViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.olan_biten_item, viewGroup, false);
            PersonViewHolder pvh = new PersonViewHolder(v);
            context = viewGroup.getContext();
            return pvh;
        }

        @Override
        public int getItemCount() {
            return OBList.size();
        }

        @Override
        public void onBindViewHolder(final PersonViewHolder personViewHolder, final int i) {
            ob_objs object = OBList.get(i);
        personViewHolder.title.setText(object.title);
        personViewHolder.author.setText(object.author);
        personViewHolder.time.setText(object.time);
        personViewHolder.content.setText(m.getSpannedTextWithSmileys(object.content));

            personViewHolder.content.setMovementMethod(LinkMovementMethod.getInstance());
        m.runEnterAnimation(personViewHolder.obcard,i*250);
        }


        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }


    }

    public class ob_objs {
        String title,content,author,time;

        ob_objs(String title, String content, String author, String time) {
            this.title = title;
            this.content = content;
            this.author = author;
            this.time = time;
        }
    }


}
