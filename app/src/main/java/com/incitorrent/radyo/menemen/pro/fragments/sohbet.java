package com.incitorrent.radyo.menemen.pro.fragments;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;
import com.incitorrent.radyo.menemen.pro.services.FIREBASE_CM_SERVICE;
import com.incitorrent.radyo.menemen.pro.utils.CapsYukle;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;
import com.incitorrent.radyo.menemen.pro.utils.chatDB;
import com.incitorrent.radyo.menemen.pro.utils.deletePost;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link sohbet#newInstance} factory method to
 * create an instance of this fragment.
 */
public class sohbet extends Fragment implements View.OnClickListener,View.OnLongClickListener{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "SOHBETFRAG";
    private static final int RESULT_LOAD_IMAGE_CAM = 2063;
    private static final int RESULT_LOAD_IMAGE = 2064;
    private static final int PERMISSION_REQUEST_ID = 2065;
    private static final int CAM_PERMISSION_REQUEST_ID = 2066;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private EditText mesaj;
    private ImageView smilegoster;
    FloatingActionButton resimekle;
    private RecyclerView smileRV,sohbetRV;
    private FloatingActionButton mesaj_gonder;
    Menemen m;
    List<Satbax_Smiley_Objects> satbaxSmileList;
    List<Sohbet_Objects> sohbetList;
    SatbaxSmileAdapter Smileadapter;
    SohbetAdapter SohbetAdapter;
    ScheduledThreadPoolExecutor exec;
    BroadcastReceiver Chatreceiver;
    SwipeRefreshLayout swipeRV;
    chatDB sql;
    public sohbet() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment sohbet.
     */
    // TODO: Rename and change types and number of parameters
    public static sohbet newInstance(String param1, String param2) {
        sohbet fragment = new sohbet();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View sohbetView = inflater.inflate(R.layout.fragment_sohbet, container, false);
        if(getActivity()!=null) getActivity().setTitle(getString(R.string.nav_sohbet)); //Toolbar title
        m = new Menemen(getActivity().getApplicationContext());
        sql = new chatDB(getActivity().getApplicationContext(),null,null,1);
        resimekle = (FloatingActionButton) sohbetView.findViewById(R.id.resim_ekle);
        smilegoster = (ImageView) sohbetView.findViewById(R.id.smile_goster_button);
        mesaj = (EditText) sohbetView.findViewById(R.id.ETmesaj);
        mesaj.setOnEditorActionListener(new EditText.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if(mesaj.getText().toString().trim().length()>0) {
                        postToMenemen(mesaj.getText().toString());
                        mesaj.setText("");
                    }
                    return true;
                }
                return false;
            }
        });
        mesaj_gonder = (FloatingActionButton) sohbetView.findViewById(R.id.mesaj_gonder_button);
        resimekle.setOnClickListener(this);
        resimekle.setOnLongClickListener(this);
        smilegoster.setOnClickListener(this);
        mesaj_gonder.setOnClickListener(this);
        //SMILEY
        smileRV = (RecyclerView) sohbetView.findViewById(R.id.RVsmileys);
        satbaxSmileList = new ArrayList<>();
        smileRV.setHasFixedSize(true);
        LinearLayoutManager layoutManager
                = new GridLayoutManager(getActivity().getApplicationContext(),(getResources().getBoolean(R.bool.landscape_mode))? 10 : 6);
        smileRV.setLayoutManager(layoutManager);
        String smileys[] = {"gmansmile","YSB",":arap:","(gc)","SBH","lan!?","aygötüm","(S)",":cahil",":NS:",":lan!",":ypm:","(hl?)","*nopanic",":V:","demeya!?",":hmm"};
        String smileyids[] = {"smile_gman","ysb","smile_arap","smile_keci","smile_sbh","smile_lan","smile_ayg","smile_sd","smile_cahil","smile_ns","smile_lann","ypm","smile_harbimi","smile_panikyok","v","yds","eizen"};
        for(int i = 0; i< smileys.length; i++) satbaxSmileList.add(new Satbax_Smiley_Objects(smileys[i], smileyids[i]));
        Smileadapter = new SatbaxSmileAdapter(satbaxSmileList);
        smileRV.setAdapter(Smileadapter);
        //SMILEY END
        //SOHBET
        sohbetRV = (RecyclerView) sohbetView.findViewById(R.id.sohbetRV);
        sohbetList = new ArrayList<>();
        sohbetRV.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        sohbetRV.setLayoutManager(linearLayoutManager);
        SohbetAdapter = new SohbetAdapter(sohbetList);
        itemTouchHelper.attachToRecyclerView(sohbetRV); //Swipe to remove itemtouchhelper
        //SOHBETEND
        //SWIPETOREFRESH
        swipeRV = (SwipeRefreshLayout) sohbetView.findViewById(R.id.swipeRV);
        swipeRV.setColorSchemeColors(Color.RED,Color.MAGENTA,Color.BLUE,Color.CYAN);
        swipeRV.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new forceSync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
        //SWIPETOREFRESHEND
//        exec = new ScheduledThreadPoolExecutor(1);
//        exec.scheduleAtFixedRate(new Runnable() {
//            @Override
//            public void run() {
//                new sohbetPopulate().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//            }
//        },0,2, TimeUnit.SECONDS);
        Chatreceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getExtras();
              if(bundle==null)
                new initsohbet().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                else {
                  String action = bundle.getString("action");
                  if(action==null) return;
                  String id = bundle.getString("msgid");
                  if(action.equals(FIREBASE_CM_SERVICE.ADD)) {
                      String nick = bundle.getString("nick");
                      String mesaj = bundle.getString("msg");
                      if (sohbetList == null || sohbetRV == null || sohbetRV.getAdapter() == null)
                          return;
                      sohbetList.add(0, new Sohbet_Objects(id, nick, mesaj, null));
                      sohbetRV.getAdapter().notifyDataSetChanged();
                      m.kaydet(RadyoMenemenPro.LAST_ID_SEEN_ON_CHAT ,id);
                  }else if(action.equals(FIREBASE_CM_SERVICE.DELETE)){
                      sql.deleteMSG(id);
                      for(int i=0;i<sohbetList.size();i++) {
                          if (sohbetList.get(i).id.equals(id)) {
                              Log.v(TAG, "sohbetList " + id + sohbetList.get(i).mesaj);
                              sohbetList.remove(i);
                              sohbetRV.getAdapter().notifyItemRemoved(i);
                          }
                      }
                  }
              }
            }
        };
        setHasOptionsMenu(true);
        return sohbetView;

    }

    @Override
    public void onStart() {
        if(getActivity()!=null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(FIREBASE_CM_SERVICE.CHAT_BROADCAST_FILTER);
            LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver((Chatreceiver),filter);
        }
        super.onStart();
    }

    @Override
    public void onResume() {
        new initsohbet().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        m.bool_kaydet(RadyoMenemenPro.IS_CHAT_FOREGROUND,true); //Sohbet ön planda: bildirim gelmeyecek
        NotificationManagerCompat.from(getActivity().getApplicationContext()).cancel(FIREBASE_CM_SERVICE.GROUP_CHAT_NOTIFICATION);
        if(getActivity()!=null) {
            FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
            if(fab!=null)
                fab.setVisibility(View.INVISIBLE);
            m.runEnterAnimation(resimekle,250);
        }
        super.onResume();
    }

    @Override
    public void onStop() {
        m.bool_kaydet(RadyoMenemenPro.IS_CHAT_FOREGROUND,false);//Sohbet ön planda değil: bildirim gelebilir
        if(getActivity()!=null)  LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).unregisterReceiver(Chatreceiver);
        if(getActivity()!=null) {
            FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
            if(fab!=null)
         m.runEnterAnimation(fab,200);
            //fab.setVisibility(View.VISIBLE);
        }
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        //exec.shutdown();
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.sohbet_menu,menu);
       MenuItem silentN = menu.findItem(R.id.action_silent_notification);
        if(m.isNotificationMuted()) silentN.setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemid = item.getItemId();
        switch (itemid){
            case R.id.action_silent_notification:
                m.muteChatNotification();
                Snackbar.make(resimekle, R.string.mute_chat_notificaitions_for_10_min,Snackbar.LENGTH_LONG).show();
                item.setVisible(false);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.smile_goster_button:
                smileRV.setVisibility(smileRV.getVisibility() != View.VISIBLE ? View.VISIBLE : View.GONE);
                break;
            case R.id.mesaj_gonder_button:
                if(mesaj.getText().toString().trim().length()>0)postToMenemen(mesaj.getText().toString());
                mesaj.setText("");
                break;
            case R.id.resim_ekle:
              if(m.isFirstTime("resim_ekle"))  Toast.makeText(getActivity().getApplicationContext(), R.string.toast_caps_upload_cam, Toast.LENGTH_LONG).show();
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M &&getActivity().getApplicationContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
    //Dosya okuma izni yok izin iste
    AskReadPerm();
    break;
}
    Intent resimsec = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            .setType("image/*");
    startActivityForResult(resimsec, RESULT_LOAD_IMAGE);

                break;
        }
    }

    private void AskReadPerm() {
        if(getActivity()!=null)new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.permissions))
                .setMessage(getString(R.string.askperm))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.M)
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                PERMISSION_REQUEST_ID);
                    }
                })
                .setIcon(R.mipmap.album_placeholder)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_ID) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(getActivity(), R.string.toast_permission_read_storage_granted, Toast.LENGTH_SHORT)
                        .show();// Permission Granted
            else
                Toast.makeText(getActivity(), R.string.toast_permission_read_storage_denied, Toast.LENGTH_SHORT)
                        .show(); // Permission Denied

        }else if(requestCode == CAM_PERMISSION_REQUEST_ID){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(getActivity(), R.string.toast_permission_read_storage_granted, Toast.LENGTH_SHORT)
                        .show();// Permission Granted
            else
                Toast.makeText(getActivity(), R.string.toast_permission_read_storage_denied, Toast.LENGTH_SHORT)
                        .show(); // Permission Denied
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

        private void postToMenemen(final String mesaj) {
        new AsyncTask<Void,Void,Boolean>(){

            @Override
            protected Boolean doInBackground(Void... params) {
                Map<String, String> dataToSend = new HashMap<>();
                dataToSend.put("nick", m.oku("username"));
                dataToSend.put("mkey", m.oku("mkey"));
                dataToSend.put("mesaj", mesaj);
                String encodedStr = Menemen.getEncodedData(dataToSend);
                BufferedReader reader = null;
                try {
                    HttpURLConnection connection = (HttpURLConnection) new URL(RadyoMenemenPro.MESAJ_GONDER).openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                    writer.write(encodedStr);
                    writer.flush();
                    StringBuilder sb = new StringBuilder();
                    reader = new BufferedReader(new InputStreamReader(
                            connection.getInputStream(), "iso-8859-9"), 8);
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    line = sb.toString();
                    Log.v(TAG,"POST "+ line);
                    JSONObject j = new JSONObject(line).getJSONArray("post").getJSONObject(0);

            if(j.get("status").equals("ok")) return true;
            }catch (IOException | JSONException e){
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
    public boolean onLongClick(View view) {
        if(view == resimekle){
            if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && getActivity().getApplicationContext().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA},
                        CAM_PERMISSION_REQUEST_ID);
                return false;
            }
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File f = new File(android.os.Environment
                    .getExternalStorageDirectory(), "temp.jpg");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
            startActivityForResult(intent,RESULT_LOAD_IMAGE_CAM);
            return true;
        }
        return false;
    }

    //SOHBET adaptör ve sınıfı
    public class Sohbet_Objects {
        String id,nick,mesaj,zaman;
        public Sohbet_Objects(String id, String nick, String mesaj, String zaman) {
            this.id = id;
            this.nick = nick;
            this.mesaj = mesaj;
            this.zaman = zaman;
        }
    }
    public class SohbetAdapter extends RecyclerView.Adapter<SohbetAdapter.PersonViewHolder> {
        Context context;
        List<Sohbet_Objects> sohbetList;

        public class PersonViewHolder extends RecyclerView.ViewHolder{
            TextView nick,mesaj,zaman;
            PersonViewHolder(View itemView) {
                super(itemView);
                nick = (TextView) itemView.findViewById(R.id.username);
                mesaj = (TextView) itemView.findViewById(R.id.mesaj);
                zaman = (TextView) itemView.findViewById(R.id.zaman);

            }

        }
        SohbetAdapter(List<Sohbet_Objects> sohbetList){
            this.sohbetList = sohbetList;
        }

        @Override
        public PersonViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.sohbet_item, viewGroup,false);
            PersonViewHolder pvh = new PersonViewHolder(v);
            context = viewGroup.getContext();
            return pvh;
        }
        @Override
        public int getItemCount() {
            return sohbetList.size();
        }

        @Override
        public void onBindViewHolder(PersonViewHolder personViewHolder, final int i) {
    personViewHolder.nick.setText(sohbetList.get(i).nick.toUpperCase(Locale.US));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                personViewHolder.mesaj.setText((Html.fromHtml(Menemen.getIncitorrentSmileys(sohbetList.get(i).mesaj),Html.FROM_HTML_MODE_LEGACY,new Html.ImageGetter() {
                            @Override
                            public Drawable getDrawable(String source) {
                                int id = 0;
                                switch (source){
                                    case "gmansmile": id= R.mipmap.smile_gman;  break;
                                    case "YSB": id= R.mipmap.ysb;  break;
                                    case "arap": id= R.mipmap.smile_arap;  break;
                                    case "gc": id= R.mipmap.smile_keci;  break;
                                    case "SBH": id= R.mipmap.smile_sbh;  break;
                                    case "000lan000": id= R.mipmap.smile_lan;  break;
                                    case "lann0lebowski": id= R.mipmap.smile_lann;  break;
                                    case "olumlu": id= R.mipmap.smile_olumlu;  break;
                                    case "lol": id= R.mipmap.smile_gulme;  break;
                                    case "ayg": id= R.mipmap.smile_ayg;  break;
                                    case "<sikimizdedegil>": id= R.mipmap.smile_sd;  break;
                                    case "<cahil>": id = R.mipmap.smile_cahil; break;
                                    case "<nereyeS>": id = R.mipmap.smile_ns; break;
                                    case "<ypm>": id = R.mipmap.ypm; break;
                                    case "hl": id = R.mipmap.smile_harbimi; break;
                                    case "nopanic": id = R.mipmap.smile_panikyok; break;
                                    case "v": id = R.mipmap.v; break;
                                    case "yds": id = R.mipmap.yds; break;
                                    case "eizen": id = R.mipmap.eizen; break;
                                }


                                Drawable d = context.getResources().getDrawable(id);
                                d.setBounds(0,0,d.getIntrinsicWidth(),d.getIntrinsicHeight());
                                return d;
                            }
                        },null)));
            }else{
                personViewHolder.mesaj.setText((Html.fromHtml(Menemen.getIncitorrentSmileys(sohbetList.get(i).mesaj),new Html.ImageGetter() {
                    @Override
                    public Drawable getDrawable(String source) {
                        int id = 0;
                        switch (source){
                            case "gmansmile": id= R.mipmap.smile_gman;  break;
                            case "YSB": id= R.mipmap.ysb;  break;
                            case "arap": id= R.mipmap.smile_arap;  break;
                            case "gc": id= R.mipmap.smile_keci;  break;
                            case "SBH": id= R.mipmap.smile_sbh;  break;
                            case "000lan000": id= R.mipmap.smile_lan;  break;
                            case "lann0lebowski": id= R.mipmap.smile_lann;  break;
                            case "olumlu": id= R.mipmap.smile_olumlu;  break;
                            case "lol": id= R.mipmap.smile_gulme;  break;
                            case "ayg": id= R.mipmap.smile_ayg;  break;
                            case "<sikimizdedegil>": id= R.mipmap.smile_sd;  break;
                            case "<cahil>": id = R.mipmap.smile_cahil; break;
                            case "<nereyeS>": id = R.mipmap.smile_ns; break;
                            case "<ypm>": id = R.mipmap.ypm; break;
                            case "hl": id = R.mipmap.smile_harbimi; break;
                            case "nopanic": id = R.mipmap.smile_panikyok; break;
                            case "v": id = R.mipmap.v; break;
                            case "yds": id = R.mipmap.yds; break;
                            case "eizen": id = R.mipmap.eizen; break;
                        }


                        Drawable d = context.getResources().getDrawable(id);
                        d.setBounds(0,0,d.getIntrinsicWidth(),d.getIntrinsicHeight());
                        return d;
                    }
                },null)));

            }
            personViewHolder.mesaj.setMovementMethod(LinkMovementMethod.getInstance());
    personViewHolder.zaman.setText(m.getElapsed(sohbetList.get(i).zaman));
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_LOAD_IMAGE && data!=null){
            try {
                Uri selectedimage = data.getData();
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getApplicationContext().getContentResolver(), selectedimage);
                   new CapsYukle(bitmap,getActivity().getApplicationContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                Toast.makeText(getActivity().getApplicationContext(), R.string.caps_uploading, Toast.LENGTH_SHORT).show();
            }catch (Exception e){e.printStackTrace();}
        }else if(requestCode == RESULT_LOAD_IMAGE_CAM && resultCode!=0){ //resultCode 0: kameradan seçim iptal edildi
            try {
                File f = new File(Environment.getExternalStorageDirectory()
                        .toString());
                for (File temp : f.listFiles()) {
                    if (temp.getName().equals("temp.jpg")) {
                        f = temp;
                        break;
                    }
                }
                Bitmap bitmap;
                BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(),
                        bitmapOptions);
                new CapsYukle(bitmap,getActivity().getApplicationContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                Toast.makeText(getActivity().getApplicationContext(), R.string.caps_uploading, Toast.LENGTH_SHORT).show();
                f.delete();


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


//CAPS YUKLE END

    //SOHBET
//Smiley yardımcı sınıfı & adaptörü
    public class Satbax_Smiley_Objects {
        String smile,smileid;
        public Satbax_Smiley_Objects(String smile, String smileid) {
            this.smile = smile;
            this.smileid = smileid;
        }
    }
    public class SatbaxSmileAdapter extends RecyclerView.Adapter<SatbaxSmileAdapter.PersonViewHolder> {
        Context context;
        List<Satbax_Smiley_Objects> satbaxSmileList;
        public class PersonViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            ImageView smiley;
            PersonViewHolder(View itemView) {
                super(itemView);
                smiley = (ImageView)itemView.findViewById(R.id.smiley);
                smiley.setOnClickListener(this);
            }
            @Override
            public void onClick(View v) {
                mesaj.setText(mesaj.getText().toString() + " " + satbaxSmileList.get(getAdapterPosition()).smile);
                smileRV.setVisibility(View.GONE);
            }
        }
        SatbaxSmileAdapter(List<Satbax_Smiley_Objects> satbaxSmileList){
            this.satbaxSmileList = satbaxSmileList;
        }

        @Override
        public PersonViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.smile_item, viewGroup,false);
            PersonViewHolder pvh = new PersonViewHolder(v);
            context = viewGroup.getContext();
            return pvh;
        }
        @Override
        public int getItemCount() {
            return satbaxSmileList.size();
        }

        @Override
        public void onBindViewHolder(PersonViewHolder personViewHolder, final int i) {
            String smileid = satbaxSmileList.get(i).smileid;
            personViewHolder.smiley.setContentDescription(smileid);
            personViewHolder.smiley.setImageResource(getResources().getIdentifier(smileid, "mipmap", getActivity().getPackageName()));
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }
    }

//    class sohbetPopulate extends AsyncTask<Void,Void,Boolean>{
//
//        @Override
//        protected Boolean doInBackground(Void... params) {
//            String line;
//            String lastmsg;
//
//            if(m.isInternetAvailable()) {
//                if(sohbetList.size()<1) return false;
//                if(sohbetList.get(0).id.equals(Menemen.getMenemenData(RadyoMenemenPro.MESAJLAR+ "&print_last_msg")))
//                    return false;
//                else lastmsg = sohbetList.get(0).id;
//                line = Menemen.getMenemenData(RadyoMenemenPro.MESAJLAR + "&sonmsg=" + lastmsg);
//                if(line==null) return false;
//                if(line.equals(m.oku(RadyoMenemenPro.SOHBETCACHE))) return false;
//            }
//                else line = m.oku(RadyoMenemenPro.SOHBETCACHE);
//            if(line.equals("yok")) return null;
//            try {
//
//                JSONArray arr = new JSONObject(line).getJSONArray("mesajlar");
//                JSONObject c;
//                for(int i = 0;i<arr.getJSONArray(0).length();i++){
//                    String id,nick,mesaj,zaman;
//                  JSONArray innerJarr = arr.getJSONArray(0);
//                    c = innerJarr.getJSONObject(i);
//                    id = c.getString("id");
//                    nick = c.getString("nick");
//                    mesaj = c.getString("post");
//                    zaman = c.getString("time");
//                  sohbetList.add(0,new Sohbet_Objects(id,nick,mesaj,zaman));
//                }
//                if(arr.getJSONArray(0).length()<1) return false;
//                 Log.v(TAG, " SOHBETLIST" + line);
//            }catch (JSONException e){
//                e.printStackTrace();
//                return false;
//            }
//
//            return true;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean ok) {
//            if(!ok) return;
//       if(sohbetList!=null) SohbetAdapter = new SohbetAdapter(sohbetList);
//         if(sohbetRV.getAdapter()!=null)   sohbetRV.getAdapter().notifyDataSetChanged();
//            super.onPostExecute(true);
//        }
//    }

    class forceSync extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            if(!m.isInternetAvailable()) return null;
            String line = Menemen.getMenemenData(RadyoMenemenPro.MESAJLAR + "&sonmsg=1");
            try {
                JSONArray arr = new JSONObject(line).getJSONArray("mesajlar");
                JSONObject c;
                for(int i = 0;i<arr.getJSONArray(0).length();i++){
                    String id,nick,mesaj,zaman;
                    JSONArray innerJarr = arr.getJSONArray(0);
                    c = innerJarr.getJSONObject(i);
                    id = c.getString("id");
                    nick = c.getString("nick");
                    mesaj = c.getString("post");
                    zaman = c.getString("time");
                    //db ye ekle
                    sql.addtoHistory(new chatDB.CHAT(id,nick,mesaj,zaman));
                    Log.v(TAG,"add to sql " + id + " " + nick);
                }
            }catch (JSONException e){
                m.resetFirstTime("loadmessages");
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            new initsohbet().execute();
            super.onPostExecute(aVoid);
        }
    }

    class initsohbet extends AsyncTask<Void,Void,Void>{


        @Override
        protected void onPreExecute() {
            sohbetList = new ArrayList<>();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            //Getfrom db
            Cursor cursor = sql.getHistory(20);
            if(cursor == null) return null;
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                String id,nick,post,time;
                id = cursor.getString(cursor.getColumnIndex(chatDB._MSGID));
                nick = cursor.getString(cursor.getColumnIndex(chatDB._NICK));
                post = cursor.getString(cursor.getColumnIndex(chatDB._POST));
                time = cursor.getString(cursor.getColumnIndex(chatDB._TIME));
                sohbetList.add(new Sohbet_Objects(id,nick,post,time));
                cursor.moveToNext();
            }
            cursor.close();
            sql.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(sohbetList!=null) SohbetAdapter = new SohbetAdapter(sohbetList);
            if(SohbetAdapter!=null) sohbetRV.setAdapter(SohbetAdapter);
            if(swipeRV != null) swipeRV.setRefreshing(false);
            if(sohbetList != null && sohbetList.size()>1) m.kaydet(RadyoMenemenPro.LAST_ID_SEEN_ON_CHAT ,sohbetList.get(0).id);
            super.onPostExecute(aVoid);
        }
    }
    //RecyclerView callback methods for swipe to delete effects

    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean isItemViewSwipeEnabled() {
            return super.isItemViewSwipeEnabled();
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
            final  int position = viewHolder.getAdapterPosition();

            if(sohbetList.get(viewHolder.getAdapterPosition()).nick.equals(m.oku("username"))) { //Kendi mesajı, silebilir
                    Snackbar sn = Snackbar.make(smilegoster, R.string.message_deleted,Snackbar.LENGTH_SHORT).setCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar snackbar, int event) {
                    if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT || event == Snackbar.Callback.DISMISS_EVENT_SWIPE || event == Snackbar.Callback.DISMISS_EVENT_CONSECUTIVE) {
                        try {
                            //dbden sil
                            sql.deleteMSG(sohbetList.get(position).id);
                            //siteyi güncelle
                            if(getActivity()!=null)  new deletePost(getActivity().getApplicationContext(),sohbetList.get(position).id).execute();
                            sohbetList.remove(position);
                            if(sohbetRV!=null) sohbetRV.getAdapter().notifyItemRemoved(position); //Listeyi güncelle
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    super.onDismissed(snackbar, event);
                }
            });
            sn.setAction(R.string.snackbar_undo, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(sohbetRV!=null)  sohbetRV.getAdapter().notifyItemChanged(position);
                }
            });

            sn.show();
        } else{
            sohbetRV.getAdapter().notifyItemChanged(position);
        if(getActivity()!=null)    Toast.makeText(getActivity().getApplicationContext(), R.string.toast_only_your_message_deleted,Toast.LENGTH_SHORT).show();
        }

        //Remove swiped item from list and notify the RecyclerView

    }
};

ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);

}
