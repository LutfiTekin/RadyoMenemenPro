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
    private boolean isUserCameDirectly = false;
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
       if(getIntent().getExtras()!=null) {
           imageurl = getIntent().getExtras().getString("url");
           isUserCameDirectly = getIntent().getExtras().getBoolean("isUserCameDirectly");
       }
        toolbar_image = (ImageView) findViewById(R.id.toolbar_image);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar_image.setTransitionName("show_image");
            fab.setTransitionName("fab");
        }
        toolbar_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //If user came frome gallery when toolbar image click events goes to full image
                returnBehavior();
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

    void returnBehavior() {
        if (isUserCameDirectly) {
            onBackPressed();
        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(imageurl));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(show_image_comments.this,
                        new Pair<View, String>(toolbar_image, toolbar_image.getTransitionName()),
                        new Pair<View, String>(fab, fab.getTransitionName()));
                startActivity(intent, options.toBundle());
            }else
                startActivity(intent);
        }
    }


    private void postComment(final String mesaj) {
        if(mesaj == null) return;
        if(mesaj.length() < 1) return;
        ETmesaj.setText("");
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest postRequest = new StringRequest(Request.Method.POST, RadyoMenemenPro.POST_COMMENT_CAPS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

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
                return dataToSend;
            }

            @Override
            public Priority getPriority() {
                return Priority.IMMEDIATE;
            }

            @Override
            public RetryPolicy getRetryPolicy() {
                return m.menemenRetryPolicy();
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
       returnBehavior();
        return true;
    }

    private void initcomments(){
        if(!sql.isHistoryExist(imageurl)) {
            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest postRequest = new StringRequest(Request.Method.POST, RadyoMenemenPro.GET_COMMENT_CAPS,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(final String response) {
                            Log.d(TAG,"Volley response " + response);
                            new AsyncTask<Void, Void, Boolean>() {
                                @Override
                                protected void onPreExecute() {
                                    sohbetList = new ArrayList<>();
                                    super.onPreExecute();
                                }

                                @Override
                                protected Boolean doInBackground(Void... voids) {
                                    try {
                                        if(response.equals("problem")) return false;
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
                                protected void onPostExecute(Boolean aVoid) {
                                    if (aVoid != null && !aVoid){
                                        if (sohbetList != null)
                                            sohbetAdapter = new SohbetAdapter(sohbetList);
                                        if (sohbetAdapter != null) sohbetRV.setAdapter(sohbetAdapter);
                                        if (sohbetList != null && sohbetList.size() > 1)
                                            m.kaydet(RadyoMenemenPro.LAST_ID_SEEN_ON_CHAT, sohbetList.get(0).id);
                                    }
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
                public RetryPolicy getRetryPolicy() {
                    return new RetryPolicy() {
                        @Override
                        public int getCurrentTimeout() {
                            return RadyoMenemenPro.MENEMEN_TIMEOUT;
                        }

                        @Override
                        public int getCurrentRetryCount() {
                            return 5;
                        }

                        @Override
                        public void retry(VolleyError error) throws VolleyError {
                            Toast.makeText(context, "Yorumlar indirilemedi yeniden deneniyor", Toast.LENGTH_SHORT).show();
                        }
                    };
                }

                @Override
                public Priority getPriority() {
                    return Priority.HIGH;
                }
            };
            postRequest.setShouldCache(false);
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
                    Cursor cursor = sql.getHistory(imageurl);
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
