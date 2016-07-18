package com.incitorrent.radyo.menemen.pro.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RMPRO;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link login.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link login#newInstance} factory method to
 * create an instance of this fragment.
 */
public class login extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "Login Fragment";
    private EditText username,password;
    private TextView infotext;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    Menemen m;
    private OnFragmentInteractionListener mListener;

    public login() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment login.
     */
    // TODO: Rename and change types and number of parameters
    public static login newInstance(String param1, String param2) {
        login fragment = new login();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m = new Menemen(getActivity().getApplicationContext());
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootview = inflater.inflate(R.layout.fragment_login,container,false);
        if(getActivity()!=null) getActivity().setTitle(getString(R.string.nav_login)); //Toolbar title
        username = (EditText) rootview.findViewById(R.id.usernameET);
        password = (EditText) rootview.findViewById(R.id.passET);
        final Button submit = (Button) rootview.findViewById(R.id.submit);
        final Button nologin = (Button) rootview.findViewById(R.id.no_login);
        infotext = (TextView) rootview.findViewById(R.id.textView2);
      if(nologin!=null)  nologin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
        if(getActivity()!=null){
                final Context context = getActivity().getApplicationContext();
                new AlertDialog.Builder(getActivity())
                        .setTitle(context.getString(R.string.dialog_nologin_title))
                        .setMessage(context.getString(R.string.dialog_nologin_descr))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                m.kaydet(RadyoMenemenPro.HAYKIRCACHE, "yok");
                                PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("music_only",true).apply();
                                Toast.makeText(context, R.string.music_only_mode_is_active, Toast.LENGTH_SHORT).show();
                                Intent intent = getActivity().getIntent();
                                getActivity().overridePendingTransition(0, 0);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                getActivity().finish();
                                getActivity().overridePendingTransition(0, 0);
                                startActivity(intent);
                            }
                        }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                        .setIcon(R.mipmap.ic_launcher)
                        .show();
            }
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               final String nick = username.getText().toString().trim();
                String pass = password.getText().toString();
               final String pash = Menemen.md5(Menemen.md5("radyomenemen"+ Menemen.md5(pass)));
                if(nick.length()<2 || nick.length()>16 || pass.length()<6) {
                    Toast.makeText(getActivity(), getString(R.string.error_occured), Toast.LENGTH_SHORT).show();
                    infotext.setTextColor(Color.RED);
                    infotext.setText("Bir hata oluştu \n Kullanıcı adın 16 karakterden fazla olamaz \n Şifren 6 karakterden az olamaz");
                    return;
                }

                new AsyncTask<Void,Void,String>(){
                    @Override
                    protected void onPreExecute() {
                        Toast.makeText(getActivity(), R.string.toast_connecting, Toast.LENGTH_SHORT).show();
                        super.onPreExecute();
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        if(s!=null) {
                            if (s.equals("loggedin"))
                                Toast.makeText(getActivity(), getString(R.string.toast_logged_in), Toast.LENGTH_SHORT).show();
                            else if (s.equals("signedup"))
                                Toast.makeText(getActivity(), getString(R.string.toast_signed_up), Toast.LENGTH_SHORT).show();
                            //TODO sohbeti aç
                            if(!s.equals("problem"))
                                getFragmentManager().beginTransaction().replace(R.id.Fcontent,new sohbet()).commit();
                                getActivity().recreate();
                             }
                        super.onPostExecute(s);
                    }

                    @Override
                    protected String doInBackground(Void... params) {
                        Map<String,String> dataToSend = new HashMap<>();
                        dataToSend.put("uye", nick);
                        dataToSend.put("sifre", pash);
                        String encodedStr = Menemen.getEncodedData(dataToSend);

                        BufferedReader reader = null;

                        try {
                            //Converting address String to URL
                            URL url = new URL(RadyoMenemenPro.AUTH);
                            HttpURLConnection con = (HttpURLConnection) url.openConnection();
                            con.setRequestMethod("POST");
                            con.setDoOutput(true);
                            OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                            writer.write(encodedStr);
                            writer.flush();
                            StringBuilder sb = new StringBuilder();
                            reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                            String line;
                            while((line = reader.readLine()) != null) {
                                sb.append(line + "\n");
                            }
                            line = sb.toString();
                            JSONObject J = new JSONObject(line);
                            Log.v(TAG, "VERFER "+ J.getString("durum") + " " + J.getString("mkey") + "\n" + line);
                            String durum = J.getString("durum");
                            String aksiyon = J.getString("aksiyon");
                            if(durum.equals("ok")){
                                m.kaydet("username",nick);
                                m.kaydet("mkey",J.getString("mkey"));
                                m.kaydet("logged", "evet");
                                m.setToken();
                                if(aksiyon.equals("giris")) return "loggedin";
                                else if(aksiyon.equals("kayit")) return "signedup";
                                return "ok";
                            }else throw  new Exception(durum);


                        } catch (final Exception e) {
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                public void run() {
                                    String toastmsg;
                                    switch (e.getMessage()){
                                        case "password": toastmsg = getString(R.string.toast_error_password_incorrect); break;
                                        default: toastmsg = getString(R.string.error_occured) + " " + e.getMessage(); break;
                                    }

                                    Toast.makeText(getActivity(), toastmsg, Toast.LENGTH_LONG).show();

                                }
                            });

                            e.printStackTrace();
                        } finally {
                            if(reader != null) {
                                try {
                                    reader.close();     //Closing the
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        return "problem";
                    }
                }.execute();
            }
        });
        return rootview;
    }

    @Override
    public void onResume() {

        super.onResume();
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
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
