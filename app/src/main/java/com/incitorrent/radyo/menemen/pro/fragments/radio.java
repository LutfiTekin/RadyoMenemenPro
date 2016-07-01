package com.incitorrent.radyo.menemen.pro.fragments;

import android.app.DownloadManager;
import android.app.Fragment;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RMPRO;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;
import com.incitorrent.radyo.menemen.pro.services.MUSIC_INFO_SERVICE;
import com.incitorrent.radyo.menemen.pro.services.MUSIC_PLAY_SERVICE;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;
import com.incitorrent.radyo.menemen.pro.utils.radioDB;

import java.util.ArrayList;
import java.util.List;

import static com.incitorrent.radyo.menemen.pro.RadyoMenemenPro.broadcastinfo.*;


public class radio extends Fragment implements View.OnClickListener,View.OnLongClickListener{
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    Context context;
    Menemen m;
    private RecyclerView lastplayed;
    radioDB sql;
    Cursor cursor;
    RadioAdapter adapter;
    List<history_objs> RList;
    CardView emptyview;
    TextView NPtrack;
    TextView NPdj;
    ImageView NPart,NPequ;
    CardView NPcard;
    ImageButton NPspotify,NPyoutube,NPlyric;
    LinearLayout nowplayingbox;
    AnimationDrawable frameAnimation;
    BroadcastReceiver NPreceiver;
    BroadcastReceiver NPUpdatereceiver;


    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public radio() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment radio.
     */
    // TODO: Rename and change types and number of parameters
    public static radio newInstance(String param1, String param2) {
        radio fragment = new radio();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
       context = getActivity().getApplicationContext();
        m = new Menemen(context);
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View radioview = inflater.inflate(R.layout.fragment_radio,container,false);
        //Son çalınanlar listesini yükle
        sql = new radioDB(context,null,null,1);
        emptyview = (CardView) radioview.findViewById(R.id.emptyview);
        lastplayed=(RecyclerView)radioview.findViewById(R.id.lastplayed);
        if (lastplayed != null) lastplayed.setHasFixedSize(true);
        if(getResources().getBoolean(R.bool.landscape_mode))
            lastplayed.setLayoutManager(new GridLayoutManager(context, 4));
        else lastplayed.setLayoutManager(new LinearLayoutManager(context));
        nowplayingbox = (LinearLayout) radioview.findViewById(R.id.nowplaying_box);
        nowplayingbox.setVisibility(View.GONE); //initialy hidden
        NPtrack = (TextView) radioview.findViewById(R.id.nowplaying_track);
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

        if (NPequ != null) {
            NPequ.setVisibility(View.VISIBLE);
            frameAnimation = (AnimationDrawable)NPequ.getDrawable();
            frameAnimation.setCallback(NPequ);
            frameAnimation.setVisible(true, true);
        }

        lastplayed.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
              if(m.oku("caliyor").equals("evet") && !m.oku(CALAN).equals("yok") && !m.oku(RadyoMenemenPro.IS_PODCAST).equals("evet")) {
                  switch (newState) {
                      case RecyclerView.SCROLL_STATE_DRAGGING:
                          m.runExitAnimation(nowplayingbox, 400);
                          break;
                      case RecyclerView.SCROLL_STATE_SETTLING:
                          m.runEnterAnimation(nowplayingbox, 200);
                          break;
                  }
              }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        //Şimdi çalıyor kısmını göster
        NPreceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getExtras()!=null) {
                    if(intent.getBooleanExtra(RadyoMenemenPro.PLAY, true)) {
                      if(!m.oku(RadyoMenemenPro.IS_PODCAST).equals("evet")) m.runEnterAnimation(nowplayingbox, 200);
                        m.runEnterAnimation(NPtrack,400);
                        m.runEnterAnimation(NPcard,400);
                        m.runEnterAnimation(NPdj,600);
                        m.runEnterAnimation(NPspotify,700);
                        m.runEnterAnimation(NPyoutube,800);
                        m.runEnterAnimation(NPlyric,900);
                        frameAnimation.start();
                    }
                    else if (!m.oku(RadyoMenemenPro.IS_PODCAST).equals("evet"))
                        m.runExitAnimation(nowplayingbox, 500);
                }
                setNP();
                //Analytics track event
                RMPRO.getInstance().trackEvent("radio","listening",NPtrack.getText().toString());
            }
        };


        return radioview;
    }

    private void setNP() {
        NPtrack.setText(Html.fromHtml(m.oku(CALAN)));
        NPdj.setText(m.oku(DJ));
        if(getActivity()!=null && PreferenceManager.getDefaultSharedPreferences(context).getBoolean("download_artwork",true))
            Glide.with(getActivity()).load(m.oku(MUSIC_INFO_SERVICE.LAST_ARTWORK_URL)).error(R.mipmap.album_placeholder).into(NPart);
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
        if(getActivity()!=null)  LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).unregisterReceiver(NPreceiver);
        super.onStop();
    }

    @Override
    public void onResume() {
        setNP();
        RList = new ArrayList<>();
        cursor = sql.getHistory(20);
        cursor.moveToFirst();
        while(cursor!=null && !cursor.isAfterLast()) {
            if (cursor.getString(cursor.getColumnIndex("songid")) != null) {
                String song = cursor.getString(cursor.getColumnIndex("song"));
                String songhash = cursor.getString(cursor.getColumnIndex("hash"));
                String url = cursor.getString(cursor.getColumnIndex("url"));
                String arturl = cursor.getString(cursor.getColumnIndex("arturl"));
                Log.v("ARTURL",cursor.getString(cursor.getColumnIndex("arturl")));
                if(!cursor.getString(cursor.getColumnIndex("song")).equals(m.oku(CALAN))) //Son çalanlarda son çalanı gösterme :)
                RList.add(new history_objs(song,songhash,url,arturl));
            }
            cursor.moveToNext();
        }
        cursor.close();
        adapter = new RadioAdapter(RList);
        lastplayed.setAdapter(adapter);
        if(adapter.getItemCount() < 1) m.runEnterAnimation(emptyview,200);
        if(m.oku("caliyor").equals("evet") && !m.oku(RadyoMenemenPro.IS_PODCAST).equals("evet")) {
            setNP();
            m.runEnterAnimation(nowplayingbox, 200);
            frameAnimation.start();
        }else nowplayingbox.setVisibility(View.GONE);
        //Analytics track
        RMPRO.getInstance().trackScreenView("Radio Fragment");
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        final String track = NPtrack.getText().toString();
        try {
            if(v == NPyoutube) {
                Intent intent = new Intent(Intent.ACTION_SEARCH);
                intent.setPackage("com.google.android.youtube");
                intent.putExtra(SearchManager.QUERY, track);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                //Analytics track event
                RMPRO.getInstance().trackEvent("radio","search on youtube",track);
            }else if(v == NPspotify){
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setAction(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH);
                intent.setComponent(new ComponentName(
                        "com.spotify.music",
                        "com.spotify.music.MainActivity"));
                intent.putExtra(SearchManager.QUERY, track);
                this.startActivity(intent);
                //Analytics track event
                RMPRO.getInstance().trackEvent("radio","search on spotify",track);
            }else if(v == NPlyric){
                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                intent.putExtra(SearchManager.QUERY, track + " " + getString(R.string.lyrics)); // query contains search string
                startActivity(intent);
                //Analytics track event
                RMPRO.getInstance().trackEvent("radio","search lyrics",track);
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
        List<history_objs> RList;


        public class PersonViewHolder extends RecyclerView.ViewHolder {
            TextView song;
            ImageView art;
            CardView card;
            PersonViewHolder(View itemView) {
                super(itemView);
                song = (TextView)itemView.findViewById(R.id.song);
                art = (ImageView)itemView.findViewById(R.id.art);
                card = (CardView) itemView.findViewById(R.id.radiocard);
//                song.setOnLongClickListener(this);
//                art.setOnClickListener(this);
            }

//
//            @Override
//            public boolean onLongClick(View v) {
//                final int i = getAdapterPosition();
//                if(!RList.get(i).url.contains(".mp3")) return false;
//                else {
//                    if (!m.oku("logged").equals("yok")){
//                        Log.i("Downloadurl", RList.get(i).url + RList.get(i).song + RList.get(i).url.contains(".mp3"));
//                        new AlertDialog.Builder(context)
//                                .setTitle(RList.get(i).song)
//                                .setMessage(getString(R.string.download_file))
//                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        try {
//                                            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(RList.get(i).url));
//                                            request.setDescription(context.getString(R.string.downloading_file))
//                                                    .setTitle(RList.get(i).song)
//                                                    .allowScanningByMediaScanner();
//                                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//                                            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, RList.get(i).song + ".mp3");
//                                            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
//                                            manager.enqueue(request);
//                                            Toast.makeText(context, context.getString(R.string.downloading_file), Toast.LENGTH_SHORT).show();
//                                              } catch (Exception e) {
//                                            Toast.makeText(context, android.R.string.httpErrorBadUrl, Toast.LENGTH_SHORT).show();
//                                            e.printStackTrace();
//                                        }
//                                    }
//                                })
//                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        dialog.cancel();
//                                    }
//                                })
//                                .setIcon(R.mipmap.album_placeholder)
//                                .show();
//
//                    }
//                    return true;
//                }
//
//            }


        }
        RadioAdapter(List<history_objs> RList){
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
            personViewHolder.song.setText(Html.fromHtml(RList.get(i).song));
            if(getActivity()!=null && PreferenceManager.getDefaultSharedPreferences(context).getBoolean("download_artwork",true))
                Glide.with(getActivity().getApplicationContext()).load(RList.get(i).arturl).error(R.mipmap.album_placeholder).into(personViewHolder.art);
            int delay = i*100;
          m.runEnterAnimation(personViewHolder.card,delay);
        }


        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }


    }

    public class history_objs{
        String song,songhash,url,arturl;

        public history_objs(String song, String songhash, String url, String arturl) {
            this.song = song;
            this.songhash = songhash;
            this.url = url;
            this.arturl = arturl;
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
