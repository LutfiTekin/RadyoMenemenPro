package com.incitorrent.radyo.menemen.pro;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.method.LinkMovementMethod;
import android.util.Log;
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

import com.bumptech.glide.Glide;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;
import com.incitorrent.radyo.menemen.pro.utils.capsDB;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class show_image_comments extends AppCompatActivity {
    ImageView toolbar_image;
    private String imageurl;
    Menemen m;
    private EditText mesaj;
    private FloatingActionButton fab;
    List<Sohbet_Objects> sohbetList;
    SohbetAdapter sohbetAdapter;
    capsDB sql;
    private RecyclerView sohbetRV;
    final Context context = show_image_comments.this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        m = new Menemen(context);
        sql = new capsDB(context,null,null,1);
        mesaj = (EditText) findViewById(R.id.ETmesaj);
        mesaj.setOnEditorActionListener(new EditText.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if(mesaj.getText().toString().trim().length()>0) {
                        //TODO Post comment
                        mesaj.setText(""); //Todo add this on method
                    }
                    return true;
                }
                return false;
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO Post comment
            }
        });
        sohbetList = new ArrayList<>();
        sohbetRV = (RecyclerView) findViewById(R.id.capsRV);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        sohbetRV.setLayoutManager(linearLayoutManager);
        sohbetRV.setHasFixedSize(true);
        sohbetAdapter = new SohbetAdapter(sohbetList);
        itemTouchHelper.attachToRecyclerView(sohbetRV);
//Onscroll Listener
        sohbetRV.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(sohbetList == null) return;
                try {
                    int LAST_POSITION_COMP_VISIBLE = linearLayoutManager.findLastVisibleItemPosition();
                    int LIST_SIZE = sohbetList.size();
                    String lastid = sohbetList.get(LIST_SIZE - 1).id;
                    if(LAST_POSITION_COMP_VISIBLE > (LIST_SIZE - 5) ){
                        //TODO LoadMore
                        Log.v("onScroll", "LAST" + LAST_POSITION_COMP_VISIBLE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        //Onscroll Listener End

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
        Glide.with(show_image_comments.this)
                .load(imageurl)
                .error(android.R.color.holo_red_light)
                .into(toolbar_image);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       onBackPressed();
        return true;
    }

    class initsohbet extends AsyncTask<Void,Void,Void> {


        @Override
        protected void onPreExecute() {
            sohbetList = new ArrayList<>();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            //Getfrom db
            Cursor cursor = sql.getHistory(20,imageurl);
            if(cursor == null) return null;
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                String id,nick,post,time;
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

        @Override
        protected void onPostExecute(Void aVoid) {
            if(sohbetList!=null) sohbetAdapter = new SohbetAdapter(sohbetList);
            if(sohbetAdapter!=null) sohbetRV.setAdapter(sohbetAdapter);
            if(sohbetList != null && sohbetList.size()>1) m.kaydet(RadyoMenemenPro.LAST_ID_SEEN_ON_CHAT ,sohbetList.get(0).id);
            super.onPostExecute(aVoid);
        }
    }


    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean isItemViewSwipeEnabled() {
            return super.isItemViewSwipeEnabled();
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
            final  int position = viewHolder.getAdapterPosition();

            if(sohbetList.get(viewHolder.getAdapterPosition()).nick.equals(m.oku("username"))) { //Kendi mesajı, silebilir
                Snackbar sn = Snackbar.make(fab, R.string.message_deleted,Snackbar.LENGTH_SHORT).setCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT || event == Snackbar.Callback.DISMISS_EVENT_SWIPE || event == Snackbar.Callback.DISMISS_EVENT_CONSECUTIVE) {
                            try {
                                //dbden sil
                                sql.deleteMSG(sohbetList.get(position).id);
                                //TODO siteyi güncelle
                                  sohbetList.remove(position);
                                if(sohbetRV!=null) sohbetRV.getAdapter().notifyItemRemoved(position); //Listeyi güncelle
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        super.onDismissed(snackbar, event);
                    }
                });
                sn.setAction(R.string.snackbar_undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(sohbetRV!=null)  sohbetRV.getAdapter().notifyItemChanged(position);
                    }
                });

                sn.show();
            } else{
                sohbetRV.getAdapter().notifyItemChanged(position);
                Toast.makeText(context, R.string.toast_only_your_message_deleted,Toast.LENGTH_SHORT).show();
            }

            //Remove swiped item from list and notify the RecyclerView

        }
    };
    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
    public class SohbetAdapter extends RecyclerView.Adapter<SohbetAdapter.chatViewHolder> {
        Context context;
        List<Sohbet_Objects> sohbetList;

        public class chatViewHolder extends RecyclerView.ViewHolder{
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
            chatViewHolder.nick.setText(sohbetList.get(i).nick.toUpperCase(Locale.US));
            chatViewHolder.mesaj.setText(m.getSpannedTextWithSmileys(sohbetList.get(i).mesaj));
            chatViewHolder.mesaj.setMovementMethod(LinkMovementMethod.getInstance());
            chatViewHolder.zaman.setText(m.getElapsed(sohbetList.get(i).zaman));
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }
    }

    public class Sohbet_Objects {
        String id,nick,mesaj,zaman;
        public Sohbet_Objects(String id, String nick, String mesaj, String zaman) {
            this.id = id;
            this.nick = nick;
            this.mesaj = mesaj;
            this.zaman = zaman;
        }
    }

}
