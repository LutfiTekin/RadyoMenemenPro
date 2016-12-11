package com.incitorrent.radyo.menemen.pro.fragments;


import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class mp_transactions_list extends Fragment {
    Context context;
    Menemen m;
    RecyclerView recyclerView;
    RequestQueue queue;
    ProgressBar progressbar;
    TextView mp_descr;
    List<transaction_details> traList;
    TransactionsAdapter adapter;
    Toolbar toolbar;
    private static final String ADD_MP = "mp_add";
    private static final String SONG_REQUEST_QUEUED = "req_queue";
    private static final String SONG_REQUEST_PLAYED = "req_playnow";
    private static final String SKIP_TRACK = "skip_track";
    private static final String MENEMEN_POINT = "mpoint";
    public mp_transactions_list() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mpview = inflater.inflate(R.layout.fragment_mp_transactions_list, container, false);
        context = getActivity().getApplicationContext();
        getActivity().setTitle(R.string.mp);
        m = new Menemen(context);
        queue = Volley.newRequestQueue(context);
        recyclerView = (RecyclerView) mpview.findViewById(R.id.mplistR);
        mp_descr = (TextView) mpview.findViewById(R.id.mp_descr);
        progressbar = (ProgressBar) mpview.findViewById(R.id.progressbar);

        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        if(m.oku(MENEMEN_POINT) != null)
            toolbar.setSubtitle(m.oku(MENEMEN_POINT) + "MP");

        return mpview;
    }

    @Override
    public void onResume() {
        queue.add(listTransactions);
        super.onResume();
    }

    StringRequest listTransactions = new StringRequest(Request.Method.POST, RadyoMenemenPro.MP_LIST_TRANSACTIONS,
            new Response.Listener<String>() {
                @Override
                public void onResponse(final String response) {
                    new AsyncTask<Void,Void,Void>(){
                        @Override
                        protected void onPreExecute() {
                            traList = new ArrayList<>();
                            recyclerView.setLayoutManager(new LinearLayoutManager(context));
                            recyclerView.setHasFixedSize(true);
                            recyclerView.setNestedScrollingEnabled(false);
                            progressbar.setVisibility(View.VISIBLE);
                            super.onPreExecute();
                        }

                        @Override
                        protected Void doInBackground(Void... voids) {
                            try {
                                Log.d("VOLLEY",response);
                                JSONArray arr = new JSONObject(response).getJSONArray("tra");
                                JSONObject c;
                                m.kaydet(MENEMEN_POINT,arr.getJSONObject(1).getString("mp"));
                                for(int i = 0;i<arr.getJSONArray(0).length();i++){
                                    JSONArray innerJarr = arr.getJSONArray(0);
                                    c = innerJarr.getJSONObject(i);
                                    traList.add(new transaction_details(c.getString("mp"),c.getString("t"),c.getString("d")));
                                }
                                adapter = new TransactionsAdapter(traList);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            progressbar.setVisibility(View.GONE);
                            recyclerView.setAdapter(adapter);
                            if(m.oku(MENEMEN_POINT) != null)
                                toolbar.setSubtitle(m.oku(MENEMEN_POINT) + "MP");
                            super.onPostExecute(aVoid);
                        }
                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }, new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
        error.printStackTrace();
        }
    }){
        @Override
        protected Map<String, String> getParams() throws AuthFailureError {
            return m.getAuthMap();
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

    public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.TraViewHolder> {
        Context context;
        List<transaction_details> traList;


        class TraViewHolder extends RecyclerView.ViewHolder{
            TextView description;
            ImageView image;
            TraViewHolder(View itemView) {
                super(itemView);
                image = (ImageView) itemView.findViewById(R.id.imageView);
                description = (TextView) itemView.findViewById(R.id.tra_descr);
            }


        }

        TransactionsAdapter(List<transaction_details> traList) {
            this.traList = traList;
        }

        @Override
        public TraViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.mp_list_item, viewGroup, false);
            TraViewHolder pvh = new TraViewHolder(v);
            context = viewGroup.getContext();
            return pvh;
        }

        @Override
        public int getItemCount() {
            return traList.size();
        }

        @Override
        public void onBindViewHolder(TraViewHolder vh, int i) {
        String description = " ";
            String type = traList.get(i).transaction_type;
            if (type.equals(SKIP_TRACK) || type.equals(SONG_REQUEST_PLAYED) || type.equals(SONG_REQUEST_QUEUED)) {
                vh.image.setImageResource(R.drawable.ic_arrow_downward_red_24dp);
            } else if (type.equals(ADD_MP)) {
                vh.image.setImageResource(R.drawable.ic_arrow_upward_green_24dp);
            }
            switch (type){
                case SKIP_TRACK:
                    description = getString(R.string.tra_skip_track);
                    break;
                case SONG_REQUEST_PLAYED:
                    description = getString(R.string.tra_req_pla);
                    break;
                case SONG_REQUEST_QUEUED:
                    description = getString(R.string.tra_req_que);
                    break;
                case ADD_MP:
                   description = getString(R.string.tra_mp_add);
                    break;
            }

            String date = getFormattedDate(traList.get(i).transaction_date,"dd MMMM hh:mm");
            String text = date + " " + description + " " + traList.get(i).mp + "MP";

            Spannable spannable = new SpannableString(text);
            final StyleSpan iss = new StyleSpan(android.graphics.Typeface.ITALIC);
            if(date != null)
                spannable.setSpan(iss, 0, date.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            vh.description.setText(spannable, TextView.BufferType.SPANNABLE);
        }


        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }


    }

    @Nullable
    private String getFormattedDate(String date, String format){
        SimpleDateFormat parser = new SimpleDateFormat(RadyoMenemenPro.CHAT_DATE_FORMAT, Locale.getDefault());
        SimpleDateFormat formater = new SimpleDateFormat(format,Locale.getDefault());
        try {
            Date d = parser.parse(date);
            return formater.format(d);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    class transaction_details{
        String mp,transaction_type,transaction_date;
        transaction_details(String mp, String transaction_type, String transaction_date) {
            this.mp = mp;
            this.transaction_type = transaction_type;
            this.transaction_date = transaction_date;
        }
    }
}
