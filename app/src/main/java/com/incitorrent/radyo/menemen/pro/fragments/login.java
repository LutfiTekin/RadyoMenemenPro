package com.incitorrent.radyo.menemen.pro.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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

import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class login extends Fragment {

    private static final String TAG = "Login Fragment";
    private EditText username,password;
    private TextView infotext;
    Menemen m;

    public login() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m = new Menemen(getActivity().getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
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
                    infotext.setText(R.string.login_error);
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
                                getFragmentManager().beginTransaction().replace(R.id.Fcontent, new sohbet()).commit();
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
                        try {
                            String line = Menemen.postMenemenData(RadyoMenemenPro.AUTH,encodedStr);
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


}
