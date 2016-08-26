package com.incitorrent.radyo.menemen.pro.fragments;


import android.app.Fragment;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.transition.AutoTransition;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;

import java.util.concurrent.ExecutionException;

import static com.incitorrent.radyo.menemen.pro.RadyoMenemenPro.transitionname.ART;
import static com.incitorrent.radyo.menemen.pro.RadyoMenemenPro.transitionname.CALAN;

/**
 * A simple {@link Fragment} subclass.
 */
public class track_info extends Fragment implements View.OnClickListener{
    private ImageView art,iv_spotify,iv_youtube,iv_lyric;
    private TextView track,tv_spotify,tv_youtube,tv_lyric;
    private CardView card_spotify,card_youtube,card_lyric;
    Menemen m;

    public track_info() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View trackview = inflater.inflate(R.layout.fragment_track_info, container, false);
        if(getActivity()!=null) getActivity().setTitle(getString(R.string.app_name)); //Toolbar title
        art = (ImageView) trackview.findViewById(R.id.art);
        track = (TextView) trackview.findViewById(R.id.track);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            art.setTransitionName(ART);
            track.setTransitionName(CALAN);
        }
        iv_spotify = (ImageView) trackview.findViewById(R.id.iv_spotify);
        iv_youtube = (ImageView) trackview.findViewById(R.id.iv_youtube);
        iv_lyric = (ImageView) trackview.findViewById(R.id.iv_lyric);
        tv_spotify = (TextView) trackview.findViewById(R.id.tv_spotify);
        tv_youtube = (TextView) trackview.findViewById(R.id.tv_youtube);
        tv_lyric = (TextView) trackview.findViewById(R.id.tv_lyric);
        iv_spotify.setOnClickListener(this);
        iv_youtube.setOnClickListener(this);
        iv_lyric.setOnClickListener(this);
        tv_spotify.setOnClickListener(this);
        tv_youtube.setOnClickListener(this);
        tv_lyric.setOnClickListener(this);
        card_spotify = (CardView) trackview.findViewById(R.id.spotify_card);
        card_youtube = (CardView) trackview.findViewById(R.id.youtube_card);
        card_lyric = (CardView) trackview.findViewById(R.id.lyric_card);
        if(getActivity() != null){
            m = new Menemen(getActivity().getApplicationContext());
            m.runEnterAnimation(card_spotify,200);
            m.runEnterAnimation(card_youtube,300);
            m.runEnterAnimation(card_lyric,400);
        }
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            String trackName = bundle.getString("trackname", getString(R.string.music_not_found));
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
                trackName = Html.fromHtml(trackName, Html.FROM_HTML_MODE_LEGACY).toString();
            else trackName = Html.fromHtml(trackName).toString();
            track.setText(trackName);
            final String arturl = bundle.getString("arturl", null);
            if(arturl != null && getActivity() != null) {
                //Arkaplan rengini artworkten al
                final FrameLayout frame = (FrameLayout) trackview.findViewById(R.id.rel_track_info);
                final Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
                new AsyncTask<Void,Void,Integer[]>() {
                    Bitmap resim = null;
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                    }
                    @Override
                    protected Integer[] doInBackground(Void... voids) {
                        try {
                            resim = Glide.with(getActivity())
                                    .load(arturl)
                                    .asBitmap()
                                    .error(R.mipmap.album_placeholder)
                                    .into(RadyoMenemenPro.ARTWORK_IMAGE_OVERRIDE_DIM,RadyoMenemenPro.ARTWORK_IMAGE_OVERRIDE_DIM)
                                    .get();
                            Palette palette = Palette.from(resim).generate();
                            int backgroundcolor = ContextCompat.getColor(getActivity().getApplicationContext(),R.color.colorBackgroundsoft);
                            int statusbarcolor = ContextCompat.getColor(getActivity().getApplicationContext(),R.color.colorPrimaryDark);
                            int accentcolor = ContextCompat.getColor(getActivity().getApplicationContext(),R.color.colorAccent);
//                            return palette.getMutedColor(backgroundcolor);
                            int color_1 = palette.getMutedColor(backgroundcolor);
                            int color_2 = palette.getDarkMutedColor(statusbarcolor);
                            int color_3 = palette.getLightVibrantColor(accentcolor);
                            if(color_1 == backgroundcolor && color_2 == statusbarcolor){
                                //Muted renk bulunamadı vibrant renk ata
                                color_1 = palette.getVibrantColor(backgroundcolor);
                                color_2 = palette.getDarkVibrantColor(statusbarcolor);
                            }
                            return new Integer[]{color_1,color_2,color_3};
                        } catch (InterruptedException | ExecutionException | NullPointerException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                    @Override
                    protected void onPostExecute(Integer[] color) {
                        if(color != null) {
                            frame.setBackgroundColor(color[0]);
                            toolbar.setBackgroundColor(color[0]);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && getActivity() != null) {
                                Window window = getActivity().getWindow();
                                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                                window.setStatusBarColor(color[1]);
                            }
                            card_spotify.setCardBackgroundColor(color[2]);
                            card_youtube.setCardBackgroundColor(color[2]);
                            card_lyric.setCardBackgroundColor(color[2]);
                        }
                        if(resim != null)art.setImageBitmap(resim);
                        super.onPostExecute(color);
                    }
                }.execute();
            }

        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setSharedElementEnterTransition(new AutoTransition());
        }

        return trackview;
    }

    @Override
    public void onClick(View view) {
        final String trackName = track.getText().toString();
        Bundle bundle = new Bundle();
        try {
            if(view == iv_spotify || view == tv_spotify){
                bundle.putString("action","spo");
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setAction(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH);
                intent.setComponent(new ComponentName(
                        "com.spotify.music",
                        "com.spotify.music.MainActivity"));
                intent.putExtra(SearchManager.QUERY, trackName);
                this.startActivity(intent);

            }else if(view == iv_youtube || view == tv_youtube){
                bundle.putString("action","ytb");
                Intent intent = new Intent(Intent.ACTION_SEARCH);
                intent.setPackage("com.google.android.youtube");
                intent.putExtra(SearchManager.QUERY, trackName);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            }else if(view == iv_lyric || view == tv_lyric){
                bundle.putString("action","lyric");
                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                intent.putExtra(SearchManager.QUERY, trackName + " " + getString(R.string.lyrics)); // query contains search string
                startActivity(intent);
            }
            bundle.putString("track",trackName.substring(0,35));
        } catch (Exception e) {
            e.printStackTrace();
        }

        m.trackEvent("iv_s",bundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getActivity()!=null) {
            //track info ekranına geçince oynatma tuşunu animastonla gizle
            final FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
            if(fab!=null) fab.hide();
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        if(getActivity()!=null) {
            //track info ekranına geçince oynatma tuşunu animasyonla göster
            final FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
            if(fab!=null) fab.show();
            //Renkleri eski haline getir
            Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
            toolbar.setBackgroundColor(ContextCompat.getColor(getActivity().getApplicationContext(),R.color.colorPrimary));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && getActivity() != null) {
                Window window = getActivity().getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(ContextCompat.getColor(getActivity().getApplicationContext(),R.color.colorPrimaryDark));
            }
        }
    }

}
