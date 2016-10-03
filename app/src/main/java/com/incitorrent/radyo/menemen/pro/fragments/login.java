package com.incitorrent.radyo.menemen.pro.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
    private TextInputLayout til_username, til_password;
    private Context context;
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
        context = getActivity().getApplicationContext();
        username = (EditText) rootview.findViewById(R.id.usernameET);
        password = (EditText) rootview.findViewById(R.id.passET);
        til_username = (TextInputLayout) rootview.findViewById(R.id.til_username);
        til_password = (TextInputLayout) rootview.findViewById(R.id.til_password);
        username.addTextChangedListener(new MyTextWatcher(username));
        password.addTextChangedListener(new MyTextWatcher(password));
        final Button submit = (Button) rootview.findViewById(R.id.submit);
        final Button nologin = (Button) rootview.findViewById(R.id.no_login);
      if(nologin!=null)  nologin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
        if(getActivity()!=null){
                final Context context = getActivity().getApplicationContext();
              new AlertDialog.Builder(new ContextThemeWrapper(getActivity(),R.style.alertDialogTheme))
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
                        .setCancelable(false)
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
                if(!validatePass() || !validateName()) return;
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
                            if(!s.equals("problem"))
                                getFragmentManager().beginTransaction().replace(R.id.Fcontent, new sohbet()).commit();
                                setNavOnLoggedin();
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
                                m.bool_kaydet("loggedin", true);
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
    public void onStart() {
        if(username.getText().toString().trim().length() == 0 && password.getText().toString().trim().length() == 0){
            til_username.setErrorEnabled(false);
            til_password.setErrorEnabled(false);
        }

        super.onStart();
    }

    private void setNavOnLoggedin() {
        if(getActivity() == null) return;
        NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);
        View hview = navigationView.getHeaderView(0);
        TextView header_txt = (TextView) hview.findViewById(R.id.header_txt);
        navigationView.getMenu().findItem(R.id.nav_chat).setVisible(true);
        navigationView.getMenu().findItem(R.id.nav_login).setVisible(false);
        navigationView.getMenu().findItem(R.id.nav_shout).setVisible(true);
        navigationView.getMenu().findItem(R.id.nav_logout).setVisible(true);
        header_txt.setText(m.oku("username").toUpperCase());
    }

    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.passET:
                    validatePass();
                    break;
                case R.id.usernameET:
                    validateName();
                    break;
            }
        }
    }

    private boolean validatePass() {
        if (password.getText().toString().trim().length() < 6) {
            til_password.setError(context.getString(R.string.error_password));
            requestFocus(password);
            return false;
        } else {
            til_password.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validateName() {
        if (username.getText().toString().trim().length() < 1) {
            til_username.setError(context.getString(R.string.error_empty_username));
            requestFocus(username);
            return false;
        } else if(username.getText().toString().trim().length() > 16){
            til_username.setError(context.getString(R.string.error_long_username));
            requestFocus(username);
            return false;
        } else {
            til_username.setErrorEnabled(false);
        }
        return true;
    }
    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }
}
