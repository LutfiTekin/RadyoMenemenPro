package com.incitorrent.radyo.menemen.pro;

import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.incitorrent.radyo.menemen.pro.services.FIREBASE_CM_SERVICE;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;
import com.incitorrent.radyo.menemen.pro.utils.capsDB;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class show_image_comments extends AppCompatActivity {
    private static final String TAG = "comments_image";
    ImageView toolbar_image;
    private String imageurl;
    Menemen m;
    private EditText ETmesaj;
    private FloatingActionButton fab;
    List<Sohbet_Objects> sohbetList;
    SohbetAdapter sohbetAdapter;
    capsDB sql;
    private RecyclerView sohbetRV;
    final Context context = show_image_comments.this;
    BroadcastReceiver Chatreceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        setContentView(R.layout.activity_show_image_comments);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar()!=null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
       if(getIntent().getExtras()!=null) imageurl = getIntent().getExtras().getString("url");
        toolbar_image = (ImageView) findViewById(R.id.toolbar_image);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar_image.setTransitionName("show_image");
            fab.setTransitionName("fab");
        }
        toolbar_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(imageurl));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(show_image_comments.this,
                            new Pair<View, String>(toolbar_image, toolbar_image.getTransitionName()),
                            new Pair<View, String>(fab, fab.getTransitionName()));
                    startActivity(intent, options.toBundle());
                }else
                    startActivity(intent);
//                onBackPressed();
            }
        });
        m = new Menemen(context);
        sql = new capsDB(context,null,null,1);
        ETmesaj = (EditText) findViewById(R.id.ETmesaj);
        ETmesaj.setOnEditorActionListener(new EditText.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if(ETmesaj.getText().toString().trim().length()>0) {
                        postComment(ETmesaj.getText().toString());
                    }
                    return true;
                }
                return false;
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postComment(ETmesaj.getText().toString());
            }
        });
        sohbetList = new ArrayList<>();
        sohbetRV = (RecyclerView) findViewById(R.id.capsRV);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        sohbetRV.setLayoutManager(linearLayoutManager);
        sohbetRV.setHasFixedSize(true);
        sohbetRV.setNestedScrollingEnabled(false);
        sohbetAdapter = new SohbetAdapter(sohbetList);
//        itemTouchHelper.attachToRecyclerView(sohbetRV); //şimdilik kapalı
//Onscroll Listener
//        sohbetRV.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                if(sohbetList == null) return;
//                try {
//                    int LAST_POSITION_COMP_VISIBLE = linearLayoutManager.findLastVisibleItemPosition();
//                    int LIST_SIZE = sohbetList.size();
//                    String lastid = sohbetList.get(LIST_SIZE - 1).id;
//                    if(LAST_POSITION_COMP_VISIBLE > (LIST_SIZE - 5) ){
//                        //TODO LoadMore
//                        Log.v("onScroll", "LAST" + LAST_POSITION_COMP_VISIBLE);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
        //Onscroll Listener End
        Chatreceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getExtras();
                if(bundle==null)
                   Log.v(TAG,"init");
                else {
                    String action = bundle.getString("action");
                    if(action==null) return;
                    String id = bundle.getString("msgid");
                    if(action.equals(FIREBASE_CM_SERVICE.ADD)) {
                        String nick = bundle.getString("nick");
                        String mesaj = bundle.getString("comment");
                        if (sohbetList == null || sohbetRV == null || sohbetRV.getAdapter() == null)
                            return;
                        sohbetList.add(0, new Sohbet_Objects(id, nick, mesaj, null));
                        sohbetRV.getAdapter().notifyDataSetChanged();
                        m.kaydet(RadyoMenemenPro.LAST_ID_SEEN_ON_CAPS + imageurl ,id);
                    }else if(action.equals(FIREBASE_CM_SERVICE.DELETE)){
                        sql.deleteMSG(id);
                        for(int i=0;i<sohbetList.size();i++) {
                            if (sohbetList.get(i).id.equals(id)) {
                                Log.v(TAG, "sohbetList " + id + sohbetList.get(i).mesaj);
                                sohbetList.remove(i);
                                sohbetRV.getAdapter().notifyItemRemoved(i);
                            }
                        }
                    }
                }
            }
        };
        initcomments();
    }




    private void postComment(final String mesaj) {
        if(mesaj == null) return;
        if(mesaj.length() < 1) return;
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest postRequest = new StringRequest(Request.Method.POST, RadyoMenemenPro.POST_COMMENT_CAPS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ETmesaj.setText("");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(show_image_comments.this, getString(R.string.error_occured), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> dataToSend = new HashMap<>();
                dataToSend.put("nick", m.getUsername());
                dataToSend.put("capsurl", imageurl);
                dataToSend.put("comment", mesaj);
                dataToSend.put("ETmesaj", mesaj);
                return dataToSend;
            }

            @Override
            public Priority getPriority() {
                return Priority.IMMEDIATE;
            }

            @Override
            public RetryPolicy getRetryPolicy() {
                return new DefaultRetryPolicy(7000,0,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            }
        };
        queue.add(postRequest);
    }

    @Override
    protected void onStart() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(FIREBASE_CM_SERVICE.CAPS_BROADCAST_FILTER);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver((Chatreceiver),filter);
        super.onStart();
    }

    @Override
    protected void onStop() {
        m.bool_kaydet(RadyoMenemenPro.IS_CHAT_FOREGROUND + imageurl, false);//Sohbet ön planda değil: bildirim gelebilir
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(Chatreceiver);
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("url", imageurl);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if(savedInstanceState!=null) imageurl = savedInstanceState.getString("url");
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        m.bool_kaydet(RadyoMenemenPro.IS_CHAT_FOREGROUND + imageurl,true); //Sohbet ön planda: bildirim gelmeyecek
        Glide.with(show_image_comments.this)
                .load(Menemen.getThumbnail(imageurl))
                .override(RadyoMenemenPro.GALLERY_IMAGE_OVERRIDE_WITDH, RadyoMenemenPro.GALLERY_IMAGE_OVERRIDE_HEIGHT)
                .error(android.R.color.holo_red_light)
                .into(toolbar_image);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       onBackPressed();
        return true;
    }

    private void initcomments(){
        if(!sql.isHistoryExist(imageurl, m.getUsername())) {
            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest postRequest = new StringRequest(Request.Method.POST, RadyoMenemenPro.GET_COMMENT_CAPS,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(final String response) {
                            new AsyncTask<Void, Void, Void>() {
                                @Override
                                protected void onPreExecute() {
                                    sohbetList = new ArrayList<>();
                                    super.onPreExecute();
                                }

                                @Override
                                protected Void doInBackground(Void... voids) {
                                    try {
                                        String id, nick, post, time;
                                        JSONArray arr = new JSONObject(response).getJSONArray("mesajlar");
                                        JSONObject c;
                                        for (int i = 0; i < arr.getJSONArray(0).length(); i++) {
                                            JSONArray innerJarr = arr.getJSONArray(0);
                                            c = innerJarr.getJSONObject(i);
                                            id = c.getString("id");
                                            nick = c.getString("nick");
                                            post = c.getString("post");
                                            time = c.getString("time");
                                            //db ye ekle
                                            sql.addtoHistory(new capsDB.CAPS(id, imageurl, nick, post, time));
                                            sohbetList.add(new Sohbet_Objects(id,nick,post,time));
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Void aVoid) {
                                    if(sohbetList!=null) sohbetAdapter = new SohbetAdapter(sohbetList);
                                    if(sohbetAdapter!=null) sohbetRV.setAdapter(sohbetAdapter);
                                    if(sohbetList != null && sohbetList.size()>1) m.kaydet(RadyoMenemenPro.LAST_ID_SEEN_ON_CHAT ,sohbetList.get(0).id);
                                    super.onPostExecute(aVoid);
                                }
                            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                    }, null) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    HashMap<String, String> dataToSend = new HashMap<>();
                    dataToSend.put("capsurl", imageurl);
                    return dataToSend;
                }

                @Override
                public Priority getPriority() {
                    return Priority.IMMEDIATE;
                }
            };
            queue.add(postRequest);
        }else{
            new AsyncTask<Void,Void,Void>(){
                @Override
                protected void onPreExecute() {
                    sohbetList = new ArrayList<>();
                    super.onPreExecute();
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    if(sohbetList!=null) sohbetAdapter = new SohbetAdapter(sohbetList);
                    if(sohbetAdapter!=null) sohbetRV.setAdapter(sohbetAdapter);
                    if(sohbetList != null && sohbetList.size()>1) m.kaydet(RadyoMenemenPro.LAST_ID_SEEN_ON_CHAT ,sohbetList.get(0).id);
                    super.onPostExecute(aVoid);
                }

                @Override
                protected Void doInBackground(Void... voids) {
                    Cursor cursor = sql.getHistory(20,imageurl);
                    String id,nick,post,time;
                    if(cursor == null) return null;
                    cursor.moveToFirst();
                    while(!cursor.isAfterLast()){
                        id = cursor.getString(cursor.getColumnIndex(capsDB._MSGID));
                        nick = cursor.getString(cursor.getColumnIndex(capsDB._NICK));
                        post = cursor.getString(cursor.getColumnIndex(capsDB._POST));
                        time = cursor.getString(cursor.getColumnIndex(capsDB._TIME));
                        sohbetList.add(new Sohbet_Objects(id,nick,post,time));
                        cursor.moveToNext();
                    }
                    cursor.close();
                    sql.close();
                    return null;
                }
            }.execute();
        }
    }




//    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
//        @Override
//        public boolean isItemViewSwipeEnabled() {
//            return super.isItemViewSwipeEnabled();
//        }
//
//        @Override
//        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
//            return false;
//        }
//
//        @Override
//        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
//            final  int position = viewHolder.getAdapterPosition();
//
//            if(sohbetList.get(viewHolder.getAdapterPosition()).nick.equals(m.oku("username"))) { //Kendi mesajı, silebilir
//                Snackbar sn = Snackbar.make(fab, R.string.message_deleted,Snackbar.LENGTH_SHORT).setCallback(new Snackbar.Callback() {
//                    @Override
//                    public void onDismissed(Snackbar snackbar, int event) {
//                        if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT || event == Snackbar.Callback.DISMISS_EVENT_SWIPE || event == Snackbar.Callback.DISMISS_EVENT_CONSECUTIVE) {
//                            try {
//                                //dbden sil
//                                sql.deleteMSG(sohbetList.get(position).id);
//                                //TODO siteyi güncelle
//                                  sohbetList.remove(position);
//                                if(sohbetRV!=null) sohbetRV.getAdapter().notifyItemRemoved(position); //Listeyi güncelle
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//                        super.onDismissed(snackbar, event);
//                    }
//                });
//                sn.setAction(R.string.snackbar_undo, new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        if(sohbetRV!=null)  sohbetRV.getAdapter().notifyItemChanged(position);
//                    }
//                });
//
//                sn.show();
//            } else{
//                sohbetRV.getAdapter().notifyItemChanged(position);
//                Toast.makeText(context, R.string.toast_only_your_message_deleted,Toast.LENGTH_SHORT).show();
//            }
//
//            //Remove swiped item from list and notify the RecyclerView
//
//        }
//    };
    //TODO implement itemtouch helper
//    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
    public class SohbetAdapter extends RecyclerView.Adapter<SohbetAdapter.chatViewHolder> {
        Context context;
        List<Sohbet_Objects> sohbetList;

        class chatViewHolder extends RecyclerView.ViewHolder{
            TextView nick,mesaj,zaman;
            chatViewHolder(View itemView) {
                super(itemView);
                nick = (TextView) itemView.findViewById(R.id.username);
                mesaj = (TextView) itemView.findViewById(R.id.mesaj);
                zaman = (TextView) itemView.findViewById(R.id.zaman);
            }

        }
        SohbetAdapter(List<Sohbet_Objects> sohbetList){
            this.sohbetList = sohbetList;
        }

        @Override
        public chatViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.sohbet_item, viewGroup,false);
            chatViewHolder pvh = new chatViewHolder(v);
            context = viewGroup.getContext();
            return pvh;
        }
        @Override
        public int getItemCount() {
            return sohbetList.size();
        }

        @Override
        public void onBindViewHolder(chatViewHolder chatViewHolder, final int i) {
            chatViewHolder.nick.setText(sohbetList.get(i).nick);
            chatViewHolder.mesaj.setText(m.getSpannedTextWithSmileys(sohbetList.get(i).mesaj));
            chatViewHolder.mesaj.setMovementMethod(LinkMovementMethod.getInstance());
            chatViewHolder.zaman.setText(Menemen.getTimeAgo(sohbetList.get(i).zaman,context));
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }
    }

    public class Sohbet_Objects {
        String id,nick,mesaj,zaman;
        Sohbet_Objects(String id, String nick, String mesaj, String zaman) {
            this.id = id;
            this.nick = nick;
            this.mesaj = mesaj;
            this.zaman = zaman;
        }
    }

}
