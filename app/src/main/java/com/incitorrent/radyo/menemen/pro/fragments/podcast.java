package com.incitorrent.radyo.menemen.pro.fragments;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;
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
    private OnFragmentInteractionListener mListener;

    public podcast() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(getActivity()!=null) {
            FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
           if(fab!=null) fab.setVisibility(View.INVISIBLE);
        }
       View podcastview = inflater.inflate(R.layout.fragment_podcast, container, false);
        context = getActivity();
        inf = new Menemen(context);
        PR = (RecyclerView) podcastview.findViewById(R.id.podcastR);
        PR.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        PR.setLayoutManager(llm);
        if(inf.isInternetAvailable())  new LoadXML().execute();
        else
            Toast.makeText(context, "internet yok", Toast.LENGTH_SHORT).show();
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    public class LoadXML extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage(getString(R.string.podcast_updating_list));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }


        @Override
        protected Boolean doInBackground(Void... arg0) {
            XMLParser parser = new XMLParser();
            xml = parser.getXmlFromUrl(RadyoMenemenPro.PODCASTFEED); // getting XML
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
                if (maxlength > 10) maxlength = 10;
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
                        String real_mp3_link = RadyoMenemenPro.PODCASTLINK + tokens[1];
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
            pDialog.dismiss();
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


        public class PersonViewHolder extends RecyclerView.ViewHolder {
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
            personViewHolder.title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            personViewHolder.title.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Boolean result = false;

                    Log.i("Downloadurl", RList.get(i).url + RList.get(i).title);



                    return true;
                }
            });


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
