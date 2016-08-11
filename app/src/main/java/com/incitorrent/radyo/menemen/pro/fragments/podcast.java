package com.incitorrent.radyo.menemen.pro.fragments;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Html;
import android.transition.ChangeBounds;
import android.transition.ChangeTransform;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.TransitionSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;
import com.incitorrent.radyo.menemen.pro.services.MUSIC_PLAY_SERVICE;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;
import com.incitorrent.radyo.menemen.pro.utils.XMLParser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;


public class podcast extends Fragment {
    private ProgressDialog pDialog;

    static final String KEY_ITEM = "item"; // parent node
    static final String KEY_TITLE = "title";
    static final String KEY_LINK = "link";
    static final String KEY_DESC = "description";
    static final String KEY_DURATION = "itunes:duration";
    static String xml = null;
    List<podcast_objs> RList;
    PodcastAdapter adapter;
    RecyclerView PR;
    Context context;
    Menemen inf;
    int switchToOldPodcast = 1;
    private OnFragmentInteractionListener mListener;

    public podcast() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       View podcastview = inflater.inflate(R.layout.fragment_podcast, container, false);
        context = getActivity();
        if(getActivity()!=null) getActivity().setTitle(getString(R.string.podcast)); //Toolbar title
        inf = new Menemen(context);
        final ImageView imageview = (ImageView) podcastview.findViewById(R.id.imageView);
        final TextView titlepodcast = (TextView) podcastview.findViewById(R.id.title_podcast);
        final TextView descrpodcast = (TextView) podcastview.findViewById(R.id.description_podcast);

        imageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToOldPodcast++;
                if(switchToOldPodcast<10 && switchToOldPodcast>4){
                    int i = 10-switchToOldPodcast;
                   if(getActivity()!=null)
                       Snackbar.make(imageview,String.format(context.getString(R.string.old_podcast_toast), i),Snackbar.LENGTH_SHORT).show();
//                     Toast.makeText(getActivity().getApplicationContext(), String.format(context.getString(R.string.old_podcast_toast), i), Toast.LENGTH_SHORT).show();

                }else if(switchToOldPodcast == 10){
                    titlepodcast.setText("Incitorrent");
                    descrpodcast.setText(R.string.old_podcast_descr);
                    imageview.setImageResource(R.mipmap.incitorrent);
                    if(inf.isInternetAvailable())  new LoadXML(RadyoMenemenPro.OLD_PODCASTFEED,RadyoMenemenPro.OLD_PODCASTLINK).execute();
                    else
                        Toast.makeText(context, R.string.toast_internet_warn, Toast.LENGTH_SHORT).show();
                }
            }
        });
        PR = (RecyclerView) podcastview.findViewById(R.id.podcastR);
        PR.setHasFixedSize(false);
        PR.setNestedScrollingEnabled(false);
        if(getResources().getBoolean(R.bool.xlarge_landscape_mode))
            PR.setLayoutManager(new StaggeredGridLayoutManager(4,StaggeredGridLayoutManager.VERTICAL));
        else if(getResources().getBoolean(R.bool.landscape_mode))
            PR.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        else
            PR.setLayoutManager(new LinearLayoutManager(getActivity()));
        if(inf.isInternetAvailable())  new LoadXML(RadyoMenemenPro.PODCASTFEED,RadyoMenemenPro.PODCASTLINK).execute();
        else
            Toast.makeText(context, R.string.toast_internet_warn, Toast.LENGTH_SHORT).show();
        return podcastview;
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
    public void onStop() {
        if(getActivity()!=null) {
            FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
            if(fab!=null) fab.setVisibility(View.VISIBLE);
        }
        super.onStop();
    }

    @Override
    public void onResume() {
        if(getActivity()!=null) {
            FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
            if(fab!=null) fab.setVisibility(View.INVISIBLE);
        }
        Bundle bundle = new Bundle();
        bundle.putString("podcast","onresume");
        inf.trackEvent("Podcast",bundle);
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    public class LoadXML extends AsyncTask<Void, Void, Boolean> {
    String podcast,podcastlink;

        public LoadXML(String podcast, String podcastlink) {
            this.podcast = podcast;
            this.podcastlink = podcastlink;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                pDialog = new ProgressDialog(getActivity());
                pDialog.setMessage(getString(R.string.podcast_updating_list));
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(true);
                pDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        @Override
        protected Boolean doInBackground(Void... arg0) {
            XMLParser parser = new XMLParser();
            xml = parser.getXmlFromUrl(podcast); // getting XML
            if(xml==null) return false;
            if (xml.isEmpty()) {
                return false;
            } else {
                Document doc = parser.getDomElement(xml); // getting DOM element
                NodeList nl = doc.getElementsByTagName(KEY_ITEM);
                // looping through all item nodes <item>
                int maxlength = nl.getLength();
                RList = new ArrayList<>();
                for (int i = 0; i < maxlength; i++) {
                    Element e = (Element) nl.item(i);
                    try {
                        String phrase = parser.getValue(e, KEY_LINK).toString();
                        String delims = "=";
                        String[] tokens = phrase.split(delims);
                        String real_mp3_link = podcastlink + tokens[1];
                        //StringEntity entity = new UrlEncodedFormEntity(parser,"UTF-8");
                        String title = Menemen.decodefix(parser.getValue(e, KEY_TITLE));
                        String desc = Menemen.decodefix(parser.getValue(e, KEY_DESC));
                        String duration = parser.getValue(e, KEY_DURATION);
                        RList.add(i, new podcast_objs(title, desc, duration, real_mp3_link));
                    } catch (NullPointerException e1) {
                        e1.printStackTrace();
                    }

                }
            }
            return true;

        }


        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            try {
                pDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(!result) {
                Toast.makeText(getActivity(), getString(R.string.error_occured), Toast.LENGTH_SHORT).show();
                return;
            }
            adapter = new PodcastAdapter(RList);
            PR.setAdapter(adapter);
        }
    }// load xml son

    public class PodcastAdapter extends RecyclerView.Adapter<PodcastAdapter.PersonViewHolder> {
        Context context;
        List<podcast_objs> RList;
        public class PersonViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener{
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
                    Intent playservice = new Intent(getActivity().getApplicationContext(), MUSIC_PLAY_SERVICE.class);
                    getActivity().getApplicationContext().stopService(playservice); //önce servisi durdur
                    inf.kaydet("caliyor","hayır");
                        playservice.putExtra("dataSource",RList.get(getAdapterPosition()).url);
                    inf.kaydet(RadyoMenemenPro.IS_PODCAST,"evet");
                    inf.kaydet(RadyoMenemenPro.PLAYING_PODCAST,RList.get(getAdapterPosition()).title);
                    getActivity().getApplicationContext().startService(playservice);
                    Fragment podcast_now_playing = new podcast_now_playing();
                    Bundle bundle = new Bundle();
                    bundle.putString("title",RList.get(getAdapterPosition()).title);
                    bundle.putString("descr",RList.get(getAdapterPosition()).description);
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
            }

            @Override
            public boolean onLongClick(View v) {


                new AlertDialog.Builder(context)
                        .setTitle(RList.get(getAdapterPosition()).title)
                        .setMessage(getString(R.string.download_file))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(RList.get(getAdapterPosition()).url));
                                    request.setDescription(context.getString(R.string.downloading_file))
                                            .setTitle(RList.get(getAdapterPosition()).title)
                                            .allowScanningByMediaScanner();
                                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, RList.get(getAdapterPosition()).title + ".mp3");
                                    DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                                    manager.enqueue(request);
                                    Toast.makeText(context, context.getString(R.string.downloading_file), Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    Toast.makeText(context, android.R.string.httpErrorBadUrl, Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setIcon(R.drawable.podcast)
                        .show();
                return false;
            }
        }

        PodcastAdapter(List<podcast_objs> RList) {
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
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
                title = Html.fromHtml(title, Html.FROM_HTML_MODE_LEGACY).toString();
            else title = Html.fromHtml(title).toString();
            personViewHolder.descr.setText(title);
            personViewHolder.duration.setText(RList.get(i).duration);
            inf.runEnterAnimation(personViewHolder.cv,i*180);
        }


        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }
    }

    public class podcast_objs {
        String title,description,duration,url;
        public podcast_objs(String title, String description, String duration, String url) {
            this.title = title;
            this.description = description;
            this.duration = duration;
            this.url = url;
        }
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public class DetailsTransition extends TransitionSet {
        public DetailsTransition() {
            setOrdering(ORDERING_TOGETHER);
            addTransition(new ChangeBounds()).
                    addTransition(new ChangeTransform());
        }
    }
}
