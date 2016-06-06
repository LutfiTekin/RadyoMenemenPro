package com.incitorrent.radyo.menemen.pro.fragments;

import android.app.DownloadManager;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
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
                   if(getActivity()!=null) Toast.makeText(getActivity().getApplicationContext(), String.format(context.getString(R.string.old_podcast_toast), i), Toast.LENGTH_SHORT).show();

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
        PR.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        PR.setLayoutManager(llm);
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
                Log.v("tag", "xml de sorun var");
            } else {
                Log.i("tag", "xml de sorun yok");
                Document doc = parser.getDomElement(xml); // getting DOM element
                Log.i("tag", "dom element aldii");
                NodeList nl = doc.getElementsByTagName(KEY_ITEM);
                // looping through all item nodes <item>
                int maxlength = nl.getLength();

                RList = new ArrayList<>();
                for (int i = 0; i < maxlength; i++) {
//                for (int i = 0; i < 21; i++) {
                    // creating new HashMap
                    //HashMap<String, String> map = new HashMap<String, String>();
                    Element e = (Element) nl.item(i);
                    // adding each child node to HashMap key => value
                    //map.put(KEY_TITLE, parser.getValue(e, KEY_TITLE));


                    try {
                        String phrase = parser.getValue(e, KEY_LINK).toString();
                        String delims = "=";
                        String[] tokens = phrase.split(delims);
                        String real_mp3_link = podcastlink + tokens[1];
                        Log.e("tag", real_mp3_link);

                        //StringEntity entity = new UrlEncodedFormEntity(parser,"UTF-8");
                        String title = Menemen.decodefix(parser.getValue(e, KEY_TITLE));
                        String desc = Menemen.decodefix(parser.getValue(e, KEY_DESC));
                        String duration = parser.getValue(e, KEY_DURATION);


                        Log.i("tag", desc);

                        RList.add(i, new podcast_objs(title, desc, duration, real_mp3_link));

                    } catch (NullPointerException e1) {
                        e1.printStackTrace();
                    }

                }
            }
//			pDialog.dismiss();
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
            //updateList();

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
                        .setIcon(R.mipmap.ic_podcast)
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
            personViewHolder.descr.setText(Html.fromHtml(RList.get(i).description));
            personViewHolder.duration.setText(RList.get(i).duration);
            inf.runEnterAnimation(personViewHolder.cv,i*180);
        }


        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
//        itemTouchHelper.attachToRecyclerView(recyclerView);
            super.onAttachedToRecyclerView(recyclerView);
        }


    }

    public class podcast_objs {
        String title;
        String description;
        String duration;
        String url;

        public podcast_objs(String title, String description, String duration, String url) {
            this.title = title;
            this.description = description;
            this.duration = duration;
            this.url = url;
        }
    }

}
