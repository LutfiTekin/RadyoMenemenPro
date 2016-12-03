package com.incitorrent.radyo.menemen.pro.fragments;


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

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

import java.util.Map;

public class contact extends Fragment {
    Menemen m;
    Context context;
    RequestQueue queue;
    EditText ETcontact;
    private static final String AUTH_FAILED = "1";
    private static final String SUCCESS = "2";
    public contact() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View contactview = inflater.inflate(R.layout.fragment_contact, container, false);
        context = getActivity().getApplicationContext();
        getActivity().setTitle(getString(R.string.contact_us));
        queue = Volley.newRequestQueue(context);
        m = new Menemen(context);
        ETcontact = (EditText) contactview.findViewById(R.id.ETcontact);
        setHasOptionsMenu(true);
        return contactview;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.contact_menu,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_send) {
           if(ETcontact.getText().toString().trim().length() > 1 && m.isInternetAvailable())
               queue.add(post);
        }
        return super.onOptionsItemSelected(item);
    }

    StringRequest post = new StringRequest(Request.Method.POST, RadyoMenemenPro.CONTACT,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                switch (response){
                    case AUTH_FAILED:
                        Toast.makeText(context, R.string.not_delivered, Toast.LENGTH_SHORT).show();
                        break;
                    case SUCCESS:
                        Toast.makeText(context, R.string.delivered, Toast.LENGTH_SHORT).show();
                        getFragmentManager().beginTransaction().replace(R.id.Fcontent, new sohbet()).commit();
                        break;
                }
                }
            }, new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
        error.printStackTrace();
            Toast.makeText(context, R.string.delivered, Toast.LENGTH_SHORT).show();
            getFragmentManager().beginTransaction().replace(R.id.Fcontent, new sohbet()).commit();
        }
    }){
        @Override
        protected Map<String, String> getParams() throws AuthFailureError {
            Map<String, String> dataToSend = m.getAuthMap();
            dataToSend.put("icerik",ETcontact.getText().toString());
            return dataToSend;
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
                    return 5;
                }

                @Override
                public void retry(VolleyError error) throws VolleyError {
                    error.printStackTrace();
                }
            };
        }
    };

}
