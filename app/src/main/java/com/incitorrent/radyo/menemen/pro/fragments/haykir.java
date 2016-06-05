package com.incitorrent.radyo.menemen.pro.fragments;


import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 */
public class haykir extends Fragment implements View.OnClickListener {
    private static final String TAG = "HAYKIRFRAGMENT";
    EditText editText;
    FloatingActionButton send;
    RecyclerView shoutRV;

    Menemen m;
    Context mcontext;
    List<Shout_Objects> ShoutList;
    ShoutAdapter ShoutAdapter;
    public haykir() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        mcontext = context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        m = new Menemen(mcontext);
        View haykirview = inflater.inflate(R.layout.fragment_haykir, container, false);
        editText = (EditText) haykirview.findViewById(R.id.ETmesaj);
        send = (FloatingActionButton) haykirview.findViewById(R.id.mesaj_gonder_button);
        shoutRV = (RecyclerView) haykirview.findViewById(R.id.shoutRV);
        send.setOnClickListener(this);
        ShoutList = new ArrayList<>();
        shoutRV.setHasFixedSize(true);
        shoutRV.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        ShoutAdapter = new ShoutAdapter(ShoutList);

        editText.setOnEditorActionListener(new EditText.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if(editText.getText().length()>0) {
                        if (Build.VERSION.SDK_INT >= 11)
                            postHaykir(editText.getText().toString());
                        editText.setText("");
                    }
                    return true;
                }
                return false;
            }
        });
            send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    postHaykir(editText.getText().toString());
                    editText.setText("");
                }
            });
        ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
        exec.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                new initShout().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        },0,RadyoMenemenPro.MUSIC_SERVICE_INFO_INTERVAL/4, TimeUnit.SECONDS);
        return haykirview;
    }

    private void postHaykir(final String mesaj) {
        new AsyncTask<Void,Void,Boolean>(){
            @Override
            protected Boolean doInBackground(Void... params) {
                Map<String, String> dataToSend = new HashMap<>();
                dataToSend.put("nick", m.oku("username"));
                dataToSend.put("haykir", mesaj);
                String encodedStr = Menemen.getEncodedData(dataToSend);
                try {
                    String line = Menemen.postMenemenData(RadyoMenemenPro.HAYKIR_LINK,encodedStr);
                    Log.v(TAG,"POST "+ line);
                    JSONObject j = new JSONObject(line).getJSONArray("post").getJSONObject(0);

                    if(j.get("status").equals("ok")) return true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (!success) {
                    Toast.makeText(getActivity().getApplicationContext(), R.string.error_occured, Toast.LENGTH_SHORT).show();
                }
                super.onPostExecute(success);
            }
        }.execute();
    }

    @Override
    public void onClick(View v) {

    }
    class initShout extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... params) {
            String line;

            if(m.isInternetAvailable()) {
                Map<String,String> dataToSend = new HashMap<>();
                dataToSend.put("nick", m.oku("username"));
                dataToSend.put("mkey", m.oku("mkey"));
                String encodedstr = Menemen.getEncodedData(dataToSend);
                line = Menemen.postMenemenData(RadyoMenemenPro.HAYKIRMALAR,encodedstr);
                Log.v(TAG,line);
            }else
                line = m.oku(RadyoMenemenPro.HAYKIRCACHE);
            if(line.equals("yok")) return null;
            try {
                ShoutList = new ArrayList<>();
                JSONArray arr = new JSONObject(line).getJSONArray("shout");
                JSONObject c;
                for(int i = 0;i<arr.getJSONArray(0).length();i++){
                    String nick,mesaj,zaman;
                    JSONArray innerJarr = arr.getJSONArray(0);
                    c = innerJarr.getJSONObject(i);
                    nick = c.getString("nick");
                    mesaj = c.getString("haykirma");
                    zaman = c.getString("zaman");
                    ShoutList.add(new Shout_Objects(nick,mesaj,zaman));
                }
                if(m.isInternetAvailable()) m.kaydet(RadyoMenemenPro.HAYKIRCACHE,line);
                if(!m.isInternetAvailable()) ShoutList.add(0,new Shout_Objects("Radyo Menemen",getString(R.string.toast_check_your_connection),null));
                Log.v(TAG, " ShoutLIST" + line);
            }catch (JSONException e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(ShoutList!=null) ShoutAdapter = new ShoutAdapter(ShoutList);
            shoutRV.setAdapter(ShoutAdapter);
            super.onPostExecute(aVoid);
        }
    }

    public class Shout_Objects {
        String nick,shout,zaman;
        public Shout_Objects(String nick, String shout, String zaman) {
            this.nick = nick;
            this.shout = shout;
            this.zaman = zaman;
        }
    }
    public class ShoutAdapter extends RecyclerView.Adapter<ShoutAdapter.PersonViewHolder> {
        Context context;
        List<Shout_Objects> ShoutList;

        public class PersonViewHolder extends RecyclerView.ViewHolder{
            TextView nick,shout,zaman;
            CardView shoutcard;
            PersonViewHolder(View itemView) {
                super(itemView);
                nick = (TextView) itemView.findViewById(R.id.username);
                shout = (TextView) itemView.findViewById(R.id.shout);
                zaman = (TextView) itemView.findViewById(R.id.zaman);
                shoutcard = (CardView) itemView.findViewById(R.id.shoutcard);

            }

        }
        ShoutAdapter(List<Shout_Objects> ShoutList){
            this.ShoutList = ShoutList;
        }

        @Override
        public PersonViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.shout_item, viewGroup,false);
            PersonViewHolder pvh = new PersonViewHolder(v);
            context = viewGroup.getContext();
            return pvh;
        }
        @Override
        public int getItemCount() {
            return ShoutList.size();
        }

        @Override
        public void onBindViewHolder(PersonViewHolder personViewHolder, int i) {
            if(ShoutList.get(i).nick.equals("Dj")) personViewHolder.shoutcard.setCardBackgroundColor(ContextCompat.getColor(context,R.color.blueAccent));
            else personViewHolder.shoutcard.setCardBackgroundColor(Color.WHITE);
            personViewHolder.nick.setText((ShoutList.get(i).nick.equals("Dj")) ? ShoutList.get(i).nick : context.getString(R.string.me));
            personViewHolder.shout.setText(Html.fromHtml(ShoutList.get(i).shout));
            personViewHolder.zaman.setText(m.getElapsed(ShoutList.get(i).zaman));
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }
    }

}
