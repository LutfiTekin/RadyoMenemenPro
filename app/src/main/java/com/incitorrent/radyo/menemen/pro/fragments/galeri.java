package com.incitorrent.radyo.menemen.pro.fragments;


import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;
import com.incitorrent.radyo.menemen.pro.utils.EndlessParentScrollListener;
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
    NestedScrollView nestedScrollView;
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
        nestedScrollView = (NestedScrollView) galeri.findViewById(R.id.nestedscroll);
        recyclerView.setHasFixedSize(true);
        llm = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(llm);
        recyclerView.setNestedScrollingEnabled(false);
        nestedScrollView.setOnScrollChangeListener(new EndlessParentScrollListener(llm) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                Log.v(TAG,"load");
                if(Glist == null || Glist.size()<1) return;
                String lastid = Glist.get(totalItemsCount - 1).msgid;
                Log.v(TAG,"last" + Glist.get(totalItemsCount - 1).uploader + lastid + Glist.size());
                new loadMore(lastid, Glist.size()).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
            }
        });

        new loadGaleri().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return galeri;
    }



    class loadGaleri extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Glist = new ArrayList<>();
                Cursor cursor = sql.getCapsGallery(10);
                if(cursor == null) return null;
                cursor.moveToFirst();
                while (!cursor.isAfterLast()){
                    String uploader,capsurl,msgid;
                    uploader = cursor.getString(cursor.getColumnIndex(chatDB._NICK));
                    capsurl = cursor.getString(cursor.getColumnIndex(chatDB._POST));
                    msgid = cursor.getString(cursor.getColumnIndex(chatDB._MSGID));
                    Glist.add(new galeri_objects(uploader,Menemen.getCapsUrl(Menemen.fromHtmlCompat(capsurl)),msgid));
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
           if(Glist != null) {
               recyclerView.setAdapter(new GaleriAdapter(Glist));

           }
            super.onPostExecute(aVoid);
        }
    }

    class loadMore extends AsyncTask<Void,Void,Void>{
        String lastid;
        int listSize;

        loadMore(String lastid, int listSize) {
            this.lastid = lastid;
            this.listSize = listSize;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Cursor cursor = sql.loadCapsGalleryOnScroll(lastid);
                if(cursor == null) return null;
                cursor.moveToFirst();
                while(!cursor.isAfterLast()){
                    String uploader,capsurl,msgid;
                    uploader = cursor.getString(cursor.getColumnIndex(chatDB._NICK));
                    capsurl = cursor.getString(cursor.getColumnIndex(chatDB._POST));
                    msgid = cursor.getString(cursor.getColumnIndex(chatDB._MSGID));
                    Glist.add(new galeri_objects(uploader,Menemen.getCapsUrl(Menemen.fromHtmlCompat(capsurl)),msgid));
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
            super.onPostExecute(aVoid);
            if(Glist == null) return;
            if(recyclerView != null && recyclerView.getAdapter() != null){
                try {
                    recyclerView.getAdapter().notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public class GaleriAdapter extends RecyclerView.Adapter<GaleriAdapter.ViewHolder> {
        Context context;
        List<galeri_objects> Glist;
        final Boolean isLoggedIn = m.isLoggedIn();

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
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
               m.goToCapsIntent(Glist.get(getAdapterPosition()).capsurl,image,getActivity());
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
           if(getActivity()!=null)
               Glide.with(getActivity())
                   .load(Menemen.getThumbnail(Glist.get(i).capsurl))
                   .override(RadyoMenemenPro.GALLERY_IMAGE_OVERRIDE_WITDH,RadyoMenemenPro.GALLERY_IMAGE_OVERRIDE_HEIGHT)
                   .placeholder(R.drawable.default_image)
                   .centerCrop()
                   .into(viewHolder.image);

            final int count = capsSql.commentCount(Glist.get(i).capsurl);
            if(count < 1 || !isLoggedIn) {
                viewHolder.comments.setVisibility(View.GONE);
                viewHolder.uploader.setText(Glist.get(i).uploader.toUpperCase());
            }else {
                viewHolder.comments.setVisibility(View.VISIBLE);
                viewHolder.comments.setText(String.valueOf(count));
                String firstcomment = capsSql.getFirstComment(Glist.get(i).capsurl);
                if(firstcomment != null)
                    viewHolder.uploader.setText(firstcomment);
                else
                    viewHolder.uploader.setText(Glist.get(i).uploader.toUpperCase());
            }
        }


        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }


    }
    class galeri_objects{
        String uploader,capsurl,msgid;

        galeri_objects(String uploader, String capsurl, String msgid) {
            this.uploader = uploader;
            this.capsurl = capsurl;
            this.msgid = msgid;
        }
    }
}
