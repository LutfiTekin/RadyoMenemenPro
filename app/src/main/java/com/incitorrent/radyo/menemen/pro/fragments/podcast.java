package com.incitorrent.radyo.menemen.pro.fragments;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.transition.ChangeBounds;
import android.transition.ChangeTransform;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;
import com.incitorrent.radyo.menemen.pro.services.MUSIC_PLAY_SERVICE;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;
import com.incitorrent.radyo.menemen.pro.utils.WrapContentLinearLayoutManager;
import com.incitorrent.radyo.menemen.pro.utils.XMLParser;
import com.incitorrent.radyo.menemen.pro.utils.podcast_objs;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Map;


public class podcast extends Fragment {
    static final String KEY_ITEM = "item"; // parent node
    static final String KEY_TITLE = "title";
    static final String KEY_LINK = "link";
    static final String KEY_DESC = "description";
    static final String KEY_DURATION = "itunes:duration";
    ArrayList<podcast_objs> RList;
    PodcastAdapter adapter;
    RecyclerView PR;
    Context context;
    Menemen m;
    int switchToOldPodcast = 1;
    RequestQueue queue;
    ProgressBar progressBar;
    public podcast() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       View podcastview = inflater.inflate(R.layout.fragment_podcast, container, false);
        context = getActivity().getApplicationContext();
        if(getActivity()!=null) getActivity().setTitle(getString(R.string.podcast)); //Toolbar title
        m = new Menemen(context);
        final ImageView imageview = (ImageView) podcastview.findViewById(R.id.imageView);
        final TextView titlepodcast = (TextView) podcastview.findViewById(R.id.title_podcast);
        final TextView descrpodcast = (TextView) podcastview.findViewById(R.id.description_podcast);
        queue = Volley.newRequestQueue(context);
        imageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToOldPodcast++;
                if(switchToOldPodcast<10 && switchToOldPodcast>4){
                    int i = 10-switchToOldPodcast;
                   if(getActivity()!=null)
                       Snackbar.make(imageview,String.format(context.getString(R.string.old_podcast_toast), i),Snackbar.LENGTH_SHORT).show();

                }else if(switchToOldPodcast == 10){
                    titlepodcast.setText("Incitorrent");
                    descrpodcast.setText(R.string.old_podcast_descr);
                    imageview.setImageResource(R.mipmap.incitorrent);
                    if(m.isInternetAvailable())  loadFeed(false);
                    else
                        Toast.makeText(context, R.string.toast_internet_warn, Toast.LENGTH_SHORT).show();
                }
            }
        });
        progressBar = (ProgressBar) podcastview.findViewById(R.id.progressbar);
        PR = (RecyclerView) podcastview.findViewById(R.id.podcastR);
        PR.setHasFixedSize(false);
        PR.setNestedScrollingEnabled(false);
        if(getResources().getBoolean(R.bool.xlarge_landscape_mode))
            PR.setLayoutManager(new StaggeredGridLayoutManager(4,StaggeredGridLayoutManager.VERTICAL));
        else if(getResources().getBoolean(R.bool.landscape_mode))
            PR.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        else
            PR.setLayoutManager(new WrapContentLinearLayoutManager(getActivity().getApplicationContext()));
       if(m.oku(RadyoMenemenPro.PODCASTCACHE) != null){
           new LoadXML(RadyoMenemenPro.PODCASTLINK,
                   m.oku(RadyoMenemenPro.PODCASTCACHE), true)
                   .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
       }else loadFeed(true);
        if(!m.isInternetAvailable())
            Toast.makeText(context, R.string.toast_internet_warn, Toast.LENGTH_SHORT).show();
        setRetainInstance(true);
        return podcastview;
    }

    void loadFeed(final boolean menemen) {
        progressBar.setVisibility(View.VISIBLE);
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                (menemen) ? RadyoMenemenPro.PODCASTFEED : RadyoMenemenPro.OLD_PODCASTFEED,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(menemen) {
                            if(m.oku(RadyoMenemenPro.PODCASTCACHE) == null || !m.oku(RadyoMenemenPro.PODCASTCACHE).equals(response)) {
                                m.kaydet(RadyoMenemenPro.PODCASTCACHE, response);
                            }else {
                                hideProgressBar();
                                return;
                            }

                        }
                            new LoadXML((menemen) ? RadyoMenemenPro.PODCASTLINK : RadyoMenemenPro.OLD_PODCASTLINK, response, false).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);

                    }
                },null){
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                try {
                    String utf8String = new String(response.data, "UTF-8");
                    return Response.success(utf8String, HttpHeaderParser.parseCacheHeaders(response)) ;
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                }
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return super.getHeaders();
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
                        return 3;
                    }

                    @Override
                    public void retry(VolleyError error) throws VolleyError {
                        Log.d("Volley",error.toString());
                    }
                };
            }

        };
        queue.add(stringRequest);
    }

    void hideProgressBar() {
        try {
            progressBar.postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.GONE);
                }
            },500);
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            e.printStackTrace();
        }
    }





    public class LoadXML extends AsyncTask<Void, Void, Boolean> {
        String podcastlink,xml;
        boolean fromcache;

        LoadXML(String podcastlink, String xml, boolean fromcache) {
            this.podcastlink = podcastlink;
            this.xml = xml;
            this.fromcache = fromcache;
        }


        @Override
        protected Boolean doInBackground(Void... arg0) {
            try {
                XMLParser parser = new XMLParser();
                if(xml==null) return false;
                    Document doc = parser.getDomElement(xml); // getting DOM element
                    NodeList nl = doc.getElementsByTagName(KEY_ITEM);
                    // looping through all item nodes <item>
                    int maxlength = nl.getLength();
                    RList = new ArrayList<>();
                    ArrayList<podcast_objs> Podcast = Menemen.PodcastList;
                    Podcast.clear();
                    for (int i = 0; i < maxlength; i++) {
                        Element e = (Element) nl.item(i);
                        try {
                            String phrase = parser.getValue(e, KEY_LINK).toString();
                            String delims = "=";
                            String[] tokens = phrase.split(delims);
                            String real_mp3_link = podcastlink + tokens[1];
                            String title = Menemen.decodefix(parser.getValue(e, KEY_TITLE));
                            String desc = Menemen.decodefix(parser.getValue(e, KEY_DESC));
                            String duration = parser.getValue(e, KEY_DURATION);
                            RList.add(i, new podcast_objs(title, desc, duration, real_mp3_link));
                            Podcast.add(i,new podcast_objs(title, desc, duration, real_mp3_link));
                        } catch (Exception e1) {
                            e1.printStackTrace();
                            return false;
                        }

                    }

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;

        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if(!fromcache)
                hideProgressBar();
            if(!result && isAdded()) {
                Toast.makeText(context, getString(R.string.error_occured), Toast.LENGTH_SHORT).show();
                if(fromcache) loadFeed(true);
                return;
            }
            adapter = new PodcastAdapter(RList);
            PR.setAdapter(adapter);
            if(fromcache) loadFeed(true);
        }
    }

    public class PodcastAdapter extends RecyclerView.Adapter<PodcastAdapter.PersonViewHolder> {
        Context context;
        ArrayList<podcast_objs> RList;
        class PersonViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener{
            TextView title, duration, descr;
            CardView cv;
            RelativeLayout rel;

            PersonViewHolder(View itemView) {
                super(itemView);
                title = (TextView) itemView.findViewById(R.id.title);
                duration = (TextView) itemView.findViewById(R.id.duration);
                descr = (TextView) itemView.findViewById(R.id.descr);
                cv = (CardView) itemView.findViewById(R.id.cvP);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    cv.setTransitionName(RadyoMenemenPro.transitionname.PODCASTCARD);
                rel = (RelativeLayout) itemView.findViewById(R.id.Pitem);
                title.setOnClickListener(this);
                title.setOnLongClickListener(this);
                rel.setOnClickListener(this);
                rel.setOnLongClickListener(this);
                descr.setOnClickListener(this);
                descr.setOnLongClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if(getActivity()!=null) {
                final String podcasttitle = RList.get(getAdapterPosition()).title;
                String duration = RList.get(getAdapterPosition()).duration;
                    if (duration.length()<6) duration = "00:" + duration;
                        if(m.oku(podcasttitle)!=null && !PreferenceManager.getDefaultSharedPreferences(context).getBoolean("resume_podcast", false)){
                            //Should podcast resume? Ask player
                            new AlertDialog.Builder(new ContextThemeWrapper(getActivity(),R.style.alertDialogTheme))
                                    .setTitle(R.string.should_podcast_resume)
                                    .setMessage(podcast_now_playing.getFormattedDuration(Long.parseLong(m.oku(podcasttitle))) +
                                            "/" +
                                            duration)
                                    .setIcon(R.drawable.podcast).setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    m.bool_kaydet("resume"+podcasttitle,true);
                                    openPodcast();
                                }
                            }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    m.bool_kaydet("resume"+podcasttitle,false);
                                    openPodcast();
                                }
                            })
                                    .setCancelable(false)
                                    .show();
                        }else openPodcast();

                }
            }

            void openPodcast() {
                Intent playservice = new Intent(getActivity().getApplicationContext(), MUSIC_PLAY_SERVICE.class);
                getActivity().getApplicationContext().stopService(playservice); //önce servisi durdur
                m.setPlaying(false);
                playservice.putExtra("descr", RList.get(getAdapterPosition()).description);
                playservice.putExtra(RadyoMenemenPro.DATA_SOURCE,RList.get(getAdapterPosition()).url);
                m.bool_kaydet(RadyoMenemenPro.IS_PODCAST,true);
                m.kaydet(RadyoMenemenPro.PLAYING_PODCAST,RList.get(getAdapterPosition()).title);
                getActivity().getApplicationContext().startService(playservice);
                Fragment podcast_now_playing = new podcast_now_playing();
                Bundle bundle = new Bundle();
                bundle.putString("title",RList.get(getAdapterPosition()).title);
                bundle.putString("descr",RList.get(getAdapterPosition()).description);
                ArrayList<String> podcasts = new ArrayList<>();
                for(int i=0; i<RList.size(); i++){
                    podcasts.add(i,RList.get(i).url);
                }
                bundle.putStringArrayList("podcast_list",podcasts);
                m.kaydet(RadyoMenemenPro.broadcastinfo.PODCAST_URL,RList.get(getAdapterPosition()).url);
                podcast_now_playing.setArguments(bundle);


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    podcast_now_playing.setSharedElementEnterTransition(new DetailsTransition());
                    podcast_now_playing.setEnterTransition(new Slide());
                    setExitTransition(new Fade());
                    podcast_now_playing.setSharedElementReturnTransition(new DetailsTransition());
                    getFragmentManager()
                            .beginTransaction()
                            .addSharedElement(cv, cv.getTransitionName())
                            .addToBackStack("podcast")
                            .replace(R.id.Fcontent, podcast_now_playing)
                            .commit();
                }else {
                    getFragmentManager()
                            .beginTransaction()
                            .addToBackStack("podcast")
                            .replace(R.id.Fcontent, podcast_now_playing)
                            .commit();
                }
            }


            @Override
            public boolean onLongClick(View v) {
                m.downloadMenemenFile(RList.get(getAdapterPosition()).url, RList.get(getAdapterPosition()).title, R.drawable.podcast, "/RadyoMemenen/podcast", ".mp3", getActivity());
                return false;
            }

        }

        PodcastAdapter(ArrayList<podcast_objs> RList) {
            this.RList = RList;
        }

        @Override
        public PersonViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.podcast_item, viewGroup, false);
            PersonViewHolder pvh = new PersonViewHolder(v);
            context = viewGroup.getContext();
            return pvh;
        }

        @Override
        public int getItemCount() {
            return RList.size();
        }

        @Override
        public void onBindViewHolder(final PersonViewHolder personViewHolder, final int i) {
            personViewHolder.cv.setCardElevation(10);
            personViewHolder.title.setText(RList.get(i).title);
            String title = RList.get(i).description;
            personViewHolder.descr.setText(Menemen.fromHtmlCompat(title));
            personViewHolder.duration.setText(RList.get(i).duration);
            m.runEnterAnimation(personViewHolder.cv,i*180);
        }


        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public class DetailsTransition extends TransitionSet {
        DetailsTransition() {
            setOrdering(ORDERING_TOGETHER);
            addTransition(new ChangeBounds()).
                    addTransition(new ChangeTransform());
        }
    }
}
