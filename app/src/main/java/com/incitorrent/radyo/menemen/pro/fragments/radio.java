package com.incitorrent.radyo.menemen.pro.fragments;

import android.app.Fragment;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.transition.Slide;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;
import com.incitorrent.radyo.menemen.pro.services.MUSIC_INFO_SERVICE;
import com.incitorrent.radyo.menemen.pro.services.MUSIC_PLAY_SERVICE;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;
import com.incitorrent.radyo.menemen.pro.utils.radioDB;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.incitorrent.radyo.menemen.pro.RadyoMenemenPro.broadcastinfo.CALAN;
import static com.incitorrent.radyo.menemen.pro.RadyoMenemenPro.broadcastinfo.DJ;


public class radio extends Fragment implements View.OnClickListener,View.OnLongClickListener{

    Context context;
    Menemen m;
    private RecyclerView lastplayed;
    radioDB sql;
    Cursor cursor;
    RadioAdapter adapter;
    List<trackHistory> RList;
    CardView emptyview;
    TextSwitcher NPtrack;
    TextView NPdj;
    ImageView NPart,NPequ;
    CardView NPcard;
    ImageButton NPspotify,NPyoutube,NPlyric;
    LinearLayout nowplayingbox;
    AnimationDrawable frameAnimation;
    BroadcastReceiver NPreceiver;
    FloatingActionButton fab;
    ProgressBar progressbar;
    Boolean download_artwork = false;

    public radio() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
       context = getActivity().getApplicationContext();
        m = new Menemen(context);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View radioview = inflater.inflate(R.layout.fragment_radio,container,false);
        if(getActivity()!=null) {
            getActivity().setTitle(getString(R.string.app_name));
            //pressing the volume keys changes media volume even if not playing
            getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
        }
        //Son çalınanlar listesini yükle
        sql = new radioDB(context,null,null,1);
        fab = (FloatingActionButton) radioview.findViewById(R.id.fab);
        progressbar = (ProgressBar) radioview.findViewById(R.id.progressbar);
        progressbar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(context,R.color.BufferingPBcolor), android.graphics.PorterDuff.Mode.MULTIPLY);
        emptyview = (CardView) radioview.findViewById(R.id.emptyview);
        lastplayed=(RecyclerView)radioview.findViewById(R.id.lastplayed);
        if (lastplayed != null) lastplayed.setHasFixedSize(true);
        if(getResources().getBoolean(R.bool.landscape_mode))
            lastplayed.setLayoutManager(new GridLayoutManager(context, 4));
        else lastplayed.setLayoutManager(new LinearLayoutManager(context));
        itemTouchHelper.attachToRecyclerView(lastplayed); //Swipe to remove itemtouchhelper
        nowplayingbox = (LinearLayout) radioview.findViewById(R.id.nowplaying_box);
        nowplayingbox.setVisibility(View.GONE); //initialy hidden
        NPtrack = (TextSwitcher) radioview.findViewById(R.id.nowplaying_track);
        NPtrack.setFactory(mFactory);
        NPtrack.setText(getString(R.string.ph_np_track));
        Animation in = AnimationUtils.loadAnimation(context,android.R.anim.fade_in);
        Animation out = AnimationUtils.loadAnimation(context,android.R.anim.fade_out);
        NPtrack.setInAnimation(in);
        NPtrack.setOutAnimation(out);
        NPdj = (TextView) radioview.findViewById(R.id.nowplaying_dj);
        NPart = (ImageView) radioview.findViewById(R.id.nowplaying_art);
        NPequ = (ImageView) radioview.findViewById(R.id.nowplaying_equ);
        NPcard = (CardView) radioview.findViewById(R.id.cardviewart);
        NPlyric = (ImageButton) radioview.findViewById(R.id.lyric);
        NPspotify = (ImageButton) radioview.findViewById(R.id.spotify);
        NPyoutube = (ImageButton) radioview.findViewById(R.id.youtube);
        NPlyric.setOnClickListener(this);
        NPlyric.setOnLongClickListener(this);
        NPspotify.setOnClickListener(this);
        NPspotify.setOnLongClickListener(this);
        NPyoutube.setOnClickListener(this);
        NPyoutube.setOnLongClickListener(this);
        NPart.setOnClickListener(this);
        NPtrack.setOnClickListener(this);
        fab.setOnClickListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            NPart.setTransitionName(RadyoMenemenPro.transitionname.ART);
            NPtrack.setTransitionName(RadyoMenemenPro.transitionname.CALAN);
        }
        if (NPequ != null) {
            NPequ.setVisibility(View.VISIBLE);
            frameAnimation = (AnimationDrawable)NPequ.getDrawable();
            frameAnimation.setCallback(NPequ);
            frameAnimation.setVisible(true, true);
        }

        lastplayed.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                Boolean isPlayingValid = m.isPlaying() && !m.oku(CALAN).equals("yok") && !m.bool_oku(RadyoMenemenPro.IS_PODCAST);
                  switch (newState) {
                      case RecyclerView.SCROLL_STATE_DRAGGING:
                          if(isPlayingValid) m.runExitAnimation(nowplayingbox, 400);
                          if(fab != null) fab.hide();
                          break;
                      case RecyclerView.SCROLL_STATE_IDLE:
                      case RecyclerView.SCROLL_STATE_SETTLING:
                          if(isPlayingValid) m.runEnterAnimation(nowplayingbox, 200);
                          if(fab != null) fab.show();
                          break;
                  }

                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        //Şimdi çalıyor kısmını göster
        NPreceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String calan = null;
                if(intent.getExtras()!=null) {
                    String action = intent.getAction();
                    if (action == null) return;
                        if (action.equals(MUSIC_PLAY_SERVICE.MUSIC_PLAY_FILTER)) {
                            progressbar.setVisibility(View.INVISIBLE);
                            Boolean isPlaying = intent.getBooleanExtra(RadyoMenemenPro.PLAY, true);
                            fab.setImageResource((isPlaying) ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
                            if (isPlaying) {
                                if (!m.bool_oku(RadyoMenemenPro.IS_PODCAST))
                                    m.runEnterAnimation(nowplayingbox, 200);
                                m.runEnterAnimation(NPtrack, 400);
                                m.runEnterAnimation(NPcard, 400);
                                m.runEnterAnimation(NPdj, 600);
                                m.runEnterAnimation(NPspotify, 700);
                                m.runEnterAnimation(NPyoutube, 800);
                                m.runEnterAnimation(NPlyric, 900);
                                frameAnimation.start();
                            } else if (!m.bool_oku(RadyoMenemenPro.IS_PODCAST))
                                m.runExitAnimation(nowplayingbox, 500);
                    }else if(action.equals(MUSIC_INFO_SERVICE.NP_FILTER))
                        calan = intent.getExtras().getString("calan",null);
                    setNP(calan);
                }
            }
        };
        download_artwork = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("download_artwork",true) && m.isConnectionFast();
        return radioview;
    }

    private void setNP(String calan) {
        calan = (calan == null) ? m.oku(CALAN) : calan;
        progressbar.setVisibility(View.INVISIBLE);
        NPdj.setText(m.oku(DJ));
        if(download_artwork)
            setNPimage(NPart);
        else
            NPtrack.setText(Menemen.fromHtmlCompat(calan));
    }

    private void setNPimage(final ImageView imageView) {
        final Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        if(imageView == null || toolbar == null) return;
        new AsyncTask<Void,Void,Integer[]>() {
            Bitmap resim = null;
            final int accentcolor = ContextCompat.getColor(context,R.color.colorAccent);
            final int colorbgsofter = ContextCompat.getColor(context,R.color.colorBackgroundsofter);
            final int textcolor = ContextCompat.getColor(context,R.color.textColorPrimary);
            final int backgroundcolor = ContextCompat.getColor(getActivity().getApplicationContext(),R.color.colorBackgroundsoft);
            final int statusbarcolor = ContextCompat.getColor(getActivity().getApplicationContext(),R.color.colorPrimaryDark);
            @Override
            protected Integer[] doInBackground(Void... voids) {
                try {
                    if(m.oku(MUSIC_INFO_SERVICE.LAST_ARTWORK_URL).equals("default")) return null;
                    resim = Glide.with(getActivity())
                            .load(m.oku(MUSIC_INFO_SERVICE.LAST_ARTWORK_URL))
                            .asBitmap()
                            .error(R.mipmap.album_placeholder)
                            .into(RadyoMenemenPro.ARTWORK_IMAGE_OVERRIDE_DIM,RadyoMenemenPro.ARTWORK_IMAGE_OVERRIDE_DIM)
                            .get();
                    Palette palette = Palette.from(resim).generate();
                    int color_1 = palette.getVibrantColor(accentcolor);
                    int color_2 = palette.getLightVibrantColor(accentcolor);
                    int color_3 = palette.getDarkMutedColor(colorbgsofter);
                    int color_4 = palette.getVibrantColor(textcolor);
                    int color_5 = palette.getMutedColor(backgroundcolor);
                    int color_6 = palette.getDarkVibrantColor(statusbarcolor);
                    if(color_5 == backgroundcolor && color_6 == statusbarcolor){
                        color_5 = palette.getVibrantColor(backgroundcolor);
                        color_6 = palette.getDarkVibrantColor(statusbarcolor);
                    }
                    return new Integer[]{color_1,color_2,color_3,color_4,color_5,color_6};
                } catch (InterruptedException | ExecutionException | NullPointerException e) {
                    e.printStackTrace();
                }
                return null;
            }
            @Override
            protected void onPostExecute(Integer[] color) {
                NPtrack.setText(Menemen.fromHtmlCompat(m.oku(CALAN)));
                addOneTrackToList();
                if(color != null) {
                    fab.setBackgroundTintList(ColorStateList.valueOf(color[0]));
                    fab.setRippleColor(color[1]);
                    nowplayingbox.setBackgroundColor(color[2]);
                    NPdj.setTextColor(color[3]);
                    ((TextView)NPtrack.getCurrentView()).setTextColor(color[3]);
                    ((TextView)NPtrack.getNextView()).setTextColor(color[3]);
                    if(toolbar!=null)
                        toolbar.setBackgroundColor(color[4]);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && getActivity() != null) {
                        Window window = getActivity().getWindow();
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                        window.setStatusBarColor(Menemen.adjustAlpha(color[5],0.5f));
                    }
                }
                if(imageView != null) {
                    if (resim != null)
                        imageView.setImageBitmap(resim);
                    else imageView.setImageResource(R.mipmap.album_placeholder);
                }
                super.onPostExecute(color);
            }
        }.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    /**
     *Add  previously played track to recyclerview
     */
    private void addOneTrackToList(){
        new AsyncTask<Void,Void,Boolean>(){

            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    if(RList != null) {
                        cursor = sql.getHistory(2);
                        cursor.moveToFirst();
                        Boolean added = false;
                        while(cursor!=null && !cursor.isAfterLast()) {
                            if (cursor.getString(cursor.getColumnIndex("songid")) != null) {
                                String song = cursor.getString(cursor.getColumnIndex("song"));
                                String songhash = cursor.getString(cursor.getColumnIndex("hash"));
                                String arturl = cursor.getString(cursor.getColumnIndex("arturl"));
                                if(!cursor.getString(cursor.getColumnIndex("song")).equals(m.oku(CALAN))){
                                    trackHistory lastTrack = new trackHistory(song, songhash, arturl);
                                    if(!RList.get(0).song.equals(song)) {
                                        RList.add(0, lastTrack);
                                        added = true;
                                        break;
                                    }
                                }
                            }
                            cursor.moveToNext();
                        }
                        cursor.close();
                        sql.close();
                        return added;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                try {
                    if(lastplayed.getAdapter() != null && RList != null && result) {
                        lastplayed.getAdapter().notifyItemInserted(0);
                        lastplayed.scrollToPosition(0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                super.onPostExecute(result);
            }
        }.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    @Override
    public void onStart() {
      if(getActivity()!=null) {
          IntentFilter filter = new IntentFilter();
          filter.addAction(MUSIC_PLAY_SERVICE.MUSIC_PLAY_FILTER);
          filter.addAction(MUSIC_INFO_SERVICE.NP_FILTER);
          LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver((NPreceiver),filter);
      }
        super.onStart();
    }

    @Override
    public void onStop() {
        if(getActivity()!=null) {
            LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).unregisterReceiver(NPreceiver);
            //Renkleri eski haline getir
            final Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
            toolbar.setBackgroundColor(ContextCompat.getColor(getActivity().getApplicationContext(),R.color.colorPrimary));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && getActivity() != null) {
                Window window = getActivity().getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Menemen.adjustAlpha(ContextCompat.getColor(getActivity().getApplicationContext(),R.color.colorPrimaryDark),0.5f));
            }
        }
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        if(fab!=null) fab.hide();
        if(getActivity()!=null)
            getActivity().setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        m.selectChannelAuto();
            new AsyncTask<Void,Void,Void>(){
                @Override
                protected void onPreExecute() {
                    RList = new ArrayList<>();
                    super.onPreExecute();
                }

                @Override
                protected Void doInBackground(Void... voids) {
                    try {
                        if(!m.isServiceRunning(MUSIC_PLAY_SERVICE.class))
                            m.setPlaying(false);
                        cursor = sql.getHistory(20);
                        cursor.moveToFirst();
                        while(cursor!=null && !cursor.isAfterLast()) {
                            if (cursor.getString(cursor.getColumnIndex("songid")) != null) {
                                String song = cursor.getString(cursor.getColumnIndex("song"));
                                String songhash = cursor.getString(cursor.getColumnIndex("hash"));
                                String arturl = cursor.getString(cursor.getColumnIndex("arturl"));
                                if(!cursor.getString(cursor.getColumnIndex("song")).equals(m.oku(CALAN))) //Son çalanlarda son çalanı gösterme :)
                                    RList.add(new trackHistory(song,songhash,arturl));
                            }
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
                    adapter = new RadioAdapter(RList);
                    lastplayed.setAdapter(adapter);
                    if(adapter.getItemCount() < 1) m.runEnterAnimation(emptyview,200);
                    if(m.isPlaying() && !m.bool_oku(RadyoMenemenPro.IS_PODCAST)) {
                        fab.setImageResource((m.isPlaying()) ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
                        setNP(null);
                        m.runEnterAnimation(nowplayingbox, 200);
                        frameAnimation.start();
                    }else nowplayingbox.setVisibility(View.GONE);
                    if (fab != null) {
                        fab.show();
                        fab.setVisibility(View.VISIBLE);
                    }
                    super.onPostExecute(aVoid);
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        super.onResume();
    }


    @Override
    public void onClick(View v) {
        final String track = Menemen.fromHtmlCompat(m.oku(CALAN));
        try {
            if(v == NPyoutube) {
                Intent intent = new Intent(Intent.ACTION_SEARCH);
                intent.setPackage("com.google.android.youtube");
                intent.putExtra(SearchManager.QUERY, track);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }else if(v == NPspotify){
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setAction(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH);
                intent.setComponent(new ComponentName(
                        "com.spotify.music",
                        "com.spotify.music.MainActivity"));
                intent.putExtra(SearchManager.QUERY, track);
                this.startActivity(intent);
            }else if(v == NPlyric){
                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                intent.putExtra(SearchManager.QUERY, track + " " + getString(R.string.lyrics)); // query contains search string
                startActivity(intent);
            }else if(v == NPart || v == NPtrack){
                String title = m.oku(CALAN);
                String arturl = m.oku(MUSIC_INFO_SERVICE.LAST_ARTWORK_URL);
                openTrackInfo(Menemen.fromHtmlCompat(title),arturl,NPart);
            }else if(v == fab){
                if(!m.isInternetAvailable()){
                    Toast.makeText(context, R.string.toast_internet_warn, Toast.LENGTH_SHORT).show();
                    return;
                }
                progressbar.setVisibility(View.VISIBLE);
                //Podcast çalmıyor
                m.bool_kaydet(RadyoMenemenPro.IS_PODCAST,false);
                Intent  radyoservis = new Intent(context, MUSIC_PLAY_SERVICE.class);
                //Ayarlardan seçilmiş kanalı bul
                String selected_channel = m.oku(PreferenceManager.getDefaultSharedPreferences(context).getString("radio_channel",RadyoMenemenPro.HIGH_CHANNEL));
                String dataSource = "http://" + m.oku(RadyoMenemenPro.RADIO_SERVER) + ":" + selected_channel +  "/";
                //Oluşturulan servis intentine datasource ekle
                radyoservis.putExtra("dataSource",dataSource);
                //data source ile servisi başlat
                context.startService(radyoservis);
            }
        } catch (Exception e) {
            e.printStackTrace();
            String toastmsg = getString(R.string.error_occured);
            if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean("show_full_error",false))
                toastmsg = getString(R.string.error_occured) + "\n" + e.toString();
           if(getActivity()!=null) Toast.makeText(context, toastmsg, Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public boolean onLongClick(View v) {
        if(v == NPyoutube || v == NPspotify || v == NPlyric){
            ImageButton ib = (ImageButton)v;
           if(getActivity()!=null) Toast.makeText(context, ib.getContentDescription().toString(), Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }


    public class RadioAdapter extends RecyclerView.Adapter<RadioAdapter.PersonViewHolder>{
        Context context;
        List<trackHistory> RList;


        class PersonViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView song;
            ImageView art;
            CardView card;
            PersonViewHolder(View itemView) {
                super(itemView);
                song = (TextView)itemView.findViewById(R.id.song);
                art = (ImageView)itemView.findViewById(R.id.art);
                card = (CardView) itemView.findViewById(R.id.radiocard);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    art.setTransitionName(RadyoMenemenPro.transitionname.ART);
                    song.setTransitionName(RadyoMenemenPro.transitionname.CALAN);
                }

                art.setOnClickListener(this);
                song.setOnClickListener(this);
                card.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                final String trackName = RList.get(getAdapterPosition()).song;
                final String artUrl = RList.get(getAdapterPosition()).arturl;
               openTrackInfo(trackName,artUrl,art);
            }

        }
        RadioAdapter(List<trackHistory> RList){
            this.RList = RList;
        }

        @Override
        public PersonViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.radio_item, viewGroup,false);
            PersonViewHolder pvh = new PersonViewHolder(v);
            context = viewGroup.getContext();
            return pvh;
        }

        @Override
        public int getItemCount() {
            return RList.size();
        }
        @Override
        public void onBindViewHolder(PersonViewHolder personViewHolder, int i) {
            String title = RList.get(i).song;
            personViewHolder.song.setText(Menemen.fromHtmlCompat(title));
            if(getActivity()!=null && download_artwork)
                Glide.with(getActivity().getApplicationContext())
                        .load(RList.get(i).arturl)
                        .placeholder(R.mipmap.album_placeholder)
                        .override(RadyoMenemenPro.ARTWORK_IMAGE_OVERRIDE_DIM,RadyoMenemenPro.ARTWORK_IMAGE_OVERRIDE_DIM)
                        .error(R.mipmap.album_placeholder)
                        .into(personViewHolder.art);
//            int delay = i*100;
//          m.runEnterAnimation(personViewHolder.card,delay);
        }


        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }


    }

    private void openTrackInfo(String trackName, String artUrl, ImageView art) {
        Fragment track_info = new track_info();
        Bundle bundle = new Bundle();
        bundle.putString("trackname", trackName);
        bundle.putString("arturl",artUrl);
        track_info.setArguments(bundle);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            track_info.setEnterTransition(new Slide());

            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.Fcontent, track_info)
                    .addToBackStack(null)
                    .commit();

    }
//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    public class DetailsTransition extends TransitionSet {
//        DetailsTransition() {
//            setOrdering(ORDERING_TOGETHER);
//            addTransition(new ChangeBounds()).
//                    addTransition(new ChangeTransform()).
//                    addTransition(new ChangeImageTransform());
//        }
//    }
    public class trackHistory {
        String song,songhash,arturl;

        trackHistory(String song, String songhash, String arturl) {
            this.song = song;
            this.songhash = songhash;
            this.arturl = arturl;
        }
    }
    //RecyclerView callback methods for swipe to delete effects

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

                Snackbar sn = Snackbar.make(lastplayed, R.string.track_deleted,Snackbar.LENGTH_SHORT).setCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT || event == Snackbar.Callback.DISMISS_EVENT_SWIPE || event == Snackbar.Callback.DISMISS_EVENT_CONSECUTIVE) {
                            sql.deleteTrack(RList.get(position).songhash);
                             RList.remove(position);
                            if(lastplayed!=null) lastplayed.getAdapter().notifyItemRemoved(position); //Listeyi güncelle
                        }
                        super.onDismissed(snackbar, event);
                    }
                });
                sn.setAction(R.string.snackbar_undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(lastplayed!=null)  lastplayed.getAdapter().notifyItemChanged(position);
                    }
                });
                sn.show();
            //Remove swiped item from list and notify the RecyclerView

        }
    };

    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);

    /**
     * The {@link android.widget.ViewSwitcher.ViewFactory} used to create {@link android.widget.TextView}s that the
     * {@link android.widget.TextSwitcher} will switch between.
     */
    private ViewSwitcher.ViewFactory mFactory = new ViewSwitcher.ViewFactory() {

        @Override
        public View makeView() {
            // Create a new TextView
            TextView t = new TextView(getActivity());
            t.setMaxLines(2);
            t.setMinLines(2);
            t.setGravity(Gravity.CENTER_HORIZONTAL);
            TextViewCompat.setTextAppearance(t,R.style.NowPlayingTextAppereance);
            return t;
        }
    };

}
