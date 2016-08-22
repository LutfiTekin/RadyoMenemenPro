package com.incitorrent.radyo.menemen.pro.fragments;


import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.show_image;
import com.incitorrent.radyo.menemen.pro.show_image_comments;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;
import com.incitorrent.radyo.menemen.pro.utils.capsDB;
import com.incitorrent.radyo.menemen.pro.utils.chatDB;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class galeri extends Fragment {
    private static final String TAG = "Galeri";
    Context context;
    RecyclerView recyclerView;
    Menemen m;
    List<galeri_objects> Glist;
    chatDB sql;
    capsDB capsSql;
    LinearLayoutManager llm;
    StaggeredGridLayoutManager sglm;
    public galeri() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        context = getActivity().getApplicationContext();
        m = new Menemen(context);
        sql = new chatDB(context,null,null,1);
        capsSql = new capsDB(context,null,null,1);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View galeri = inflater.inflate(R.layout.fragment_galeri, container, false);
        if(getActivity() != null) getActivity().setTitle(getString(R.string.nav_galeri));
        recyclerView = (RecyclerView) galeri.findViewById(R.id.galeriR);
        if(getResources().getBoolean(R.bool.xlarge_landscape_mode)) {
            sglm = new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(sglm);
        }else  if(getResources().getBoolean(R.bool.landscape_mode)) {
            sglm = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        }else {
            llm = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(llm);
        }
        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(false);
        new loadGaleri().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return galeri;
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
        super.onResume();
    }

    class loadGaleri extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Glist = new ArrayList<>();
                Cursor cursor = sql.getCapsGallery(50);
                if(cursor == null) return null;
                cursor.moveToFirst();
                while (!cursor.isAfterLast()){
                    String uploader,capsurl,msgid;
                    uploader = cursor.getString(cursor.getColumnIndex(chatDB._NICK));
                    capsurl = cursor.getString(cursor.getColumnIndex(chatDB._POST));
                    msgid = cursor.getString(cursor.getColumnIndex(chatDB._MSGID));
                    Glist.add(new galeri_objects(uploader,"http://"+ Menemen.fromHtmlCompat(capsurl),msgid));
                    cursor.moveToNext();
                }
                cursor.close();
                sql.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
           if(Glist != null) recyclerView.setAdapter(new GaleriAdapter(Glist));
            super.onPostExecute(aVoid);
        }
    }
    public class GaleriAdapter extends RecyclerView.Adapter<GaleriAdapter.ViewHolder> {
        Context context;
        List<galeri_objects> Glist;


        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            ImageView image;
            TextView uploader,comments;

            ViewHolder(View itemView) {
                super(itemView);
                image = (ImageView) itemView.findViewById(R.id.image);
                image.setOnClickListener(this);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    image.setTransitionName("show_image");
                uploader = (TextView) itemView.findViewById(R.id.uploader);
                comments = (TextView) itemView.findViewById(R.id.comment_count);

            }

            @Override
            public void onClick(View view) {
                Boolean showcomments = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("open_gallery",false);
                Intent image_intent;
                if(showcomments) {
                    image_intent = new Intent(getActivity(), show_image_comments.class);
                    image_intent.putExtra("url", Glist.get(getAdapterPosition()).capsurl);
                }else {
                    image_intent = new Intent(getActivity(), show_image.class);
                    image_intent.setData(Uri.parse(Glist.get(getAdapterPosition()).capsurl));
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(getActivity(),
                            new Pair<View, String>(image, image.getTransitionName()));
                    startActivity(image_intent, options.toBundle());
                }else
                    startActivity(image_intent);
            }
        }

        GaleriAdapter(List<galeri_objects> Glist) {
            this.Glist = Glist;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.galeri_item, viewGroup, false);
            ViewHolder pvh = new ViewHolder(v);
            context = viewGroup.getContext();
            return pvh;
        }

        @Override
        public int getItemCount() {
            return Glist.size();
        }

        @Override
        public void onBindViewHolder(final ViewHolder viewHolder, int i) {
           if(getActivity()!=null) Glide.with(getActivity()).load(Glist.get(i).capsurl).placeholder(R.drawable.default_image).centerCrop().into(viewHolder.image);
            viewHolder.uploader.setText(Glist.get(i).uploader.toUpperCase());
            final int count = capsSql.commentCount(Glist.get(i).capsurl);
            if(count < 1)
                viewHolder.comments.setVisibility(View.GONE);
            else {
                viewHolder.comments.setVisibility(View.VISIBLE);
                viewHolder.comments.setText(String.valueOf(count));
            }
        }


        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }


    }
    class galeri_objects{
        String uploader,capsurl,msgid;

        public galeri_objects(String uploader, String capsurl, String msgid) {
            this.uploader = uploader;
            this.capsurl = capsurl;
            this.msgid = msgid;
        }
    }
}
