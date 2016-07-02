package com.incitorrent.radyo.menemen.pro.fragments;


import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RMPRO;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class olan_biten extends Fragment {

    private static final String TAG = "OLANBITEN";
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
    public void onStop() {
        if(getActivity()!=null) {
            FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
            if(fab!=null)  fab.setVisibility(View.VISIBLE);
        }
        super.onStop();
    }

    @Override
    public void onResume() {
        if(getActivity()!=null) {
            FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
            if(fab!=null) fab.setVisibility(View.INVISIBLE);
        }
        //Analytics track
        RMPRO.getInstance().trackScreenView("Sohbet Fragment");
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
    View obview =  inflater.inflate(R.layout.fragment_olan_biten, container, false);
        recyclerView = (RecyclerView) obview.findViewById(R.id.obR);
        title = (TextView) obview.findViewById(R.id.ob_title);
        content = (TextView) obview.findViewById(R.id.ob_content);
        time = (TextView) obview.findViewById(R.id.ob_zaman);
        author = (TextView) obview.findViewById(R.id.ob_yazan);
        recyclerView.setHasFixedSize(true);
        if(getResources().getBoolean(R.bool.xlarge_landscape_mode))
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(4,StaggeredGridLayoutManager.VERTICAL));
        else  if(getResources().getBoolean(R.bool.landscape_mode))
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        else
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        m = new Menemen(context);
        new olanBiten().execute();
        return obview;
    }


    class olanBiten extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... params) {
            try {
                OBList = new ArrayList<>();
               String json;
              if(m.isInternetAvailable())  json = Menemen.getMenemenData(RadyoMenemenPro.OLAN_BITEN);
                else json = m.oku(RadyoMenemenPro.OBCACHE);
                if(json ==null || json.equals("yok")) return null;
                JSONArray arr = new JSONObject(json).getJSONArray("olan_biten");
                JSONObject c;
                for(int i = 0;i<arr.getJSONArray(0).length();i++){
                    JSONArray innerJarr = arr.getJSONArray(0);
                    c = innerJarr.getJSONObject(i);
                    OBList.add(new ob_objs(c.getString("title"),c.getString("content"),c.getString("author"),c.getString("time")));
                }
                if(m.isInternetAvailable()) {
                    m.kaydet(RadyoMenemenPro.SAVEDOB, arr.getJSONArray(0).getJSONObject(0).getString("time"));
                    m.kaydet(RadyoMenemenPro.OBCACHE, json);
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
    }


    public class OBAdapter extends RecyclerView.Adapter<OBAdapter.PersonViewHolder> {
        Context context;
        List<ob_objs> OBList;


        public class PersonViewHolder extends RecyclerView.ViewHolder {
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                personViewHolder.content.setText((Html.fromHtml(Menemen.getIncitorrentSmileys(object.content),Html.FROM_HTML_MODE_LEGACY,new Html.ImageGetter() {
                    @Override
                    public Drawable getDrawable(String source) {
                        int id = 0;
                        switch (source){
                            case "gmansmile": id= R.mipmap.smile_gman;  break;
                            case "YSB": id= R.mipmap.ysb;  break;
                            case "arap": id= R.mipmap.smile_arap;  break;
                            case "gc": id= R.mipmap.smile_keci;  break;
                            case "SBH": id= R.mipmap.smile_sbh;  break;
                            case "000lan000": id= R.mipmap.smile_lan;  break;
                            case "lann0lebowski": id= R.mipmap.smile_lann;  break;
                            case "olumlu": id= R.mipmap.smile_olumlu;  break;
                            case "lol": id= R.mipmap.smile_gulme;  break;
                            case "ayg": id= R.mipmap.smile_ayg;  break;
                            case "<sikimizdedegil>": id= R.mipmap.smile_sd;  break;
                            case "<cahil>": id = R.mipmap.smile_cahil; break;
                            case "<nereyeS>": id = R.mipmap.smile_ns; break;
                            case "<ypm>": id = R.mipmap.ypm; break;
                        }


                        Drawable d = context.getResources().getDrawable(id);
                        d.setBounds(0,0,d.getIntrinsicWidth(),d.getIntrinsicHeight());
                        return d;
                    }
                },null)));
            }else{
                personViewHolder.content.setText((Html.fromHtml(Menemen.getIncitorrentSmileys(object.content),new Html.ImageGetter() {
                    @Override
                    public Drawable getDrawable(String source) {
                        int id = 0;
                        switch (source){
                            case "gmansmile": id= R.mipmap.smile_gman;  break;
                            case "YSB": id= R.mipmap.ysb;  break;
                            case "arap": id= R.mipmap.smile_arap;  break;
                            case "gc": id= R.mipmap.smile_keci;  break;
                            case "SBH": id= R.mipmap.smile_sbh;  break;
                            case "000lan000": id= R.mipmap.smile_lan;  break;
                            case "lann0lebowski": id= R.mipmap.smile_lann;  break;
                            case "olumlu": id= R.mipmap.smile_olumlu;  break;
                            case "lol": id= R.mipmap.smile_gulme;  break;
                            case "ayg": id= R.mipmap.smile_ayg;  break;
                            case "<sikimizdedegil>": id= R.mipmap.smile_sd;  break;
                            case "<cahil>": id = R.mipmap.smile_cahil; break;
                            case "<nereyeS>": id = R.mipmap.smile_ns; break;
                            case "<ypm>": id = R.mipmap.ypm; break;
                        }


                        Drawable d = context.getResources().getDrawable(id);
                        d.setBounds(0,0,d.getIntrinsicWidth(),d.getIntrinsicHeight());
                        return d;
                    }
                },null)));

            }

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

        public ob_objs(String title, String content, String author, String time) {
            this.title = title;
            this.content = content;
            this.author = author;
            this.time = time;
        }
    }


}
