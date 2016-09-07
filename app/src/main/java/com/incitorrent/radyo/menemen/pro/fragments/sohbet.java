package com.incitorrent.radyo.menemen.pro.fragments;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;
import com.incitorrent.radyo.menemen.pro.services.FIREBASE_CM_SERVICE;
import com.incitorrent.radyo.menemen.pro.utils.CapsYukle;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;
import com.incitorrent.radyo.menemen.pro.utils.chatDB;
import com.incitorrent.radyo.menemen.pro.utils.deletePost;
import com.incitorrent.radyo.menemen.pro.utils.trackonlineusersDB;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.incitorrent.radyo.menemen.pro.utils.Menemen.NOT_DELIVERED;
import static com.incitorrent.radyo.menemen.pro.utils.Menemen.PENDING;
import static com.incitorrent.radyo.menemen.pro.utils.Menemen.fromHtmlCompat;
import static com.incitorrent.radyo.menemen.pro.utils.Menemen.getCapsUrl;
import static com.incitorrent.radyo.menemen.pro.utils.Menemen.getEncodedData;
import static com.incitorrent.radyo.menemen.pro.utils.Menemen.getMenemenData;
import static com.incitorrent.radyo.menemen.pro.utils.Menemen.getThumbnail;
import static com.incitorrent.radyo.menemen.pro.utils.Menemen.getTimeAgo;
import static com.incitorrent.radyo.menemen.pro.utils.Menemen.getYoutubeId;
import static com.incitorrent.radyo.menemen.pro.utils.Menemen.getYoutubeThumbnail;
import static com.incitorrent.radyo.menemen.pro.utils.Menemen.postMenemenData;


public class sohbet extends Fragment implements View.OnClickListener{

    private static final String TAG = "SOHBETFRAG";
    private static final int RESULT_LOAD_IMAGE_CAM = 2063;
    private static final int RESULT_LOAD_IMAGE = 2064;
    private static final int PERMISSION_REQUEST_ID = 2065;
    private EditText mesaj;
    private ImageView smilegoster;
    FloatingActionButton resimekle,scrollTop;
    private RecyclerView smileRV,sohbetRV;
    private Boolean first_visit = true;
    Menemen m;
    List<Satbax_Smiley_Objects> satbaxSmileList;
    List<Sohbet_Objects> sohbetList;
    SatbaxSmileAdapter Smileadapter;
    SohbetAdapter SohbetAdapter;
    BroadcastReceiver Chatreceiver;
    SwipeRefreshLayout swipeRV;
    chatDB sql;
    LinearLayoutManager linearLayoutManager;
    CardView image_pick;
    ImageView take_photo,pick_gallery,cancel_image_pick;
    Toolbar toolbar;
    public sohbet() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View sohbetView = inflater.inflate(R.layout.fragment_sohbet, container, false);
        if(getActivity()!=null) {
            getActivity().setTitle(getString(R.string.nav_sohbet)); //Toolbar title
            toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        }
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
        ImageButton mesaj_gonder = (ImageButton) sohbetView.findViewById(R.id.mesaj_gonder_button);
        scrollTop = (FloatingActionButton) sohbetView.findViewById(R.id.scrolltoTop);
        image_pick = (CardView) sohbetView.findViewById(R.id.image_picker_card);
        take_photo =  (ImageView) sohbetView.findViewById(R.id.take_photo);
        pick_gallery = (ImageView) sohbetView.findViewById(R.id.pick_from_gallery);
        cancel_image_pick = (ImageView) sohbetView.findViewById(R.id.cancel_image_pick);
        resimekle.setOnClickListener(this);
        smilegoster.setOnClickListener(this);
        mesaj_gonder.setOnClickListener(this);
        scrollTop.setOnClickListener(this);
        take_photo.setOnClickListener(this);
        pick_gallery.setOnClickListener(this);
        cancel_image_pick.setOnClickListener(this);
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
        linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        sohbetRV.setLayoutManager(linearLayoutManager);
        SohbetAdapter = new SohbetAdapter(sohbetList);
        itemTouchHelper.attachToRecyclerView(sohbetRV); //Swipe to remove itemtouchhelper
        //Onscroll Listener
        sohbetRV.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(sohbetList == null) return;
                try {
                    int LAST_POSITION_COMP_VISIBLE = linearLayoutManager.findLastVisibleItemPosition();
                    int LIST_SIZE = sohbetList.size();
                    String lastid = sohbetList.get(LIST_SIZE - 1).id;
                    if(LAST_POSITION_COMP_VISIBLE > (LIST_SIZE - 5) ){
                        Log.v(TAG, "loadmore" + lastid);
                        new loadMore(lastid, LIST_SIZE).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                    }
                    if(LAST_POSITION_COMP_VISIBLE > 100 ) {
                        scrollTop.show();
                        if(getActivity()!=null) {
                            toolbar.setSubtitle(getTimeAgo(sohbetList.get(LAST_POSITION_COMP_VISIBLE).zaman, getActivity().getApplicationContext()));
                        }
                    }
                    else if(LAST_POSITION_COMP_VISIBLE < 20) {
                        if(scrollTop.getVisibility() == View.VISIBLE) scrollTop.hide();
                        if(getActivity()!=null && toolbar != null) {
                            trackonlineusersDB sql = new trackonlineusersDB(getActivity().getApplicationContext(),null,null,1);
                            final int count = sql.getOnlineUserCount();
                            if(count > 0 && toolbar !=null) {
                                if(count == 1)
                                    toolbar.setSubtitle(R.string.toolbar_online_subtitle_one);
                                else
                                    toolbar.setSubtitle(String.format(getActivity().getApplicationContext().getString(R.string.toolbar_online_subtitle), count));
                            }else toolbar.setSubtitle("");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        //Onscroll Listener End

        //SOHBETEND
        //SWIPETOREFRESH
        swipeRV = (SwipeRefreshLayout) sohbetView.findViewById(R.id.swipeRV);
        swipeRV.setColorSchemeColors(Color.GRAY,Color.BLUE,Color.RED,Color.MAGENTA,Color.CYAN);
        swipeRV.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new forceSync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
        Chatreceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getExtras();
              if(bundle==null)
                new initsohbet(20,0).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
              else {
                  if (intent.getAction().equals(FIREBASE_CM_SERVICE.CHAT_BROADCAST_FILTER)) {
                  String action = bundle.getString("action");
                  if (action == null) return;
                  String id = bundle.getString("msgid");
                  if (action.equals(FIREBASE_CM_SERVICE.ADD)) {
                      String nick = bundle.getString("nick");
                      String mesaj = bundle.getString("msg");
                      if (sohbetList == null || sohbetRV == null || sohbetRV.getAdapter() == null)
                          return;
                      if(sohbetList.get(0).mesaj.equals(mesaj) && sohbetList.get(0).nick.equals(nick))
                          sohbetList.set(0, new Sohbet_Objects(id, nick, mesaj, null));
                      else {
                          //Fallback and search for it
                          for (int i = 0; i < sohbetList.size(); i++) {
                              if (sohbetList.get(i).mesaj.equals(mesaj)) {
                                  sohbetList.remove(i);
                                  sohbetRV.getAdapter().notifyItemRemoved(i);
                              }
                          }
                          sohbetList.add(0, new Sohbet_Objects(id, nick, mesaj, null));
                      }
                      sohbetRV.getAdapter().notifyDataSetChanged();
                      playChatSound();
                      m.kaydet(RadyoMenemenPro.LAST_ID_SEEN_ON_CHAT, id);
                  } else if (action.equals(FIREBASE_CM_SERVICE.DELETE)) {
                      sql.deleteMSG(id);
                      for (int i = 0; i < sohbetList.size(); i++) {
                          if (sohbetList.get(i).id.equals(id)) {
                              Log.v(TAG, "sohbetList " + id + sohbetList.get(i).mesaj);
                              sohbetList.remove(i);
                              sohbetRV.getAdapter().notifyItemRemoved(i);
                          }
                      }
                  }
              }else {
                      if (intent.getAction().equals(FIREBASE_CM_SERVICE.USERS_ONLINE_BROADCAST_FILTER)) {
                          int count = intent.getExtras().getInt("count", 0);
                          if (count > 0 && toolbar != null) {
                              if(count == 1)
                                  toolbar.setSubtitle(R.string.toolbar_online_subtitle_one);
                              else
                                  toolbar.setSubtitle(String.format(getActivity().getApplicationContext().getString(R.string.toolbar_online_subtitle), count));

                          }
                      }
                  }
              }
            }
        };
        setRetainInstance(true);
        setHasOptionsMenu(true);
        return sohbetView;

    }

    /**
     * Activate/Deactivate secret chat sound
     */
    private void setChatSound(){
        if(m.bool_oku("catcut")){
            m.bool_kaydet("catcut",false);
            Toast.makeText(getActivity().getApplicationContext(), android.R.string.no , Toast.LENGTH_SHORT).show();
        }else {
            m.bool_kaydet("catcut",true);
            Toast.makeText(getActivity().getApplicationContext(), android.R.string.yes , Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Play secret chat sound
     */
    private void playChatSound() {
        if(getActivity() != null)
        if(PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getBoolean("chat_sound",false)){
            int sound = new Random().nextInt(5);
            try {
                MediaPlayer mPlayer;
                mPlayer = MediaPlayer.create(getActivity().getApplicationContext(), Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                        + "://" + getActivity().getApplicationContext().getPackageName() + "/raw/s" + sound ));
                if(mPlayer!=null) mPlayer.start();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onStart() {
        if(getActivity()!=null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(FIREBASE_CM_SERVICE.CHAT_BROADCAST_FILTER);
            filter.addAction(FIREBASE_CM_SERVICE.USERS_ONLINE_BROADCAST_FILTER);
            LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver((Chatreceiver),filter);
        }
        super.onStart();
    }

    @Override
    public void onResume() {
       if(first_visit) new initsohbet(20,0).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        m.bool_kaydet(RadyoMenemenPro.IS_CHAT_FOREGROUND,true); //Sohbet ön planda: bildirim gelmeyecek
        NotificationManagerCompat.from(getActivity().getApplicationContext()).cancel(FIREBASE_CM_SERVICE.GROUP_CHAT_NOTIFICATION);
        m.runEnterAnimation(resimekle,250);
        iAmOnline();
        super.onResume();
    }

    private void iAmOnline() {
        if(m.getSavedTime("online_push") > System.currentTimeMillis()) return;
        m.saveTime("online_push",(1000 * 60 * 2));
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String,String> dataToSend = new HashMap<>();
                dataToSend.put("nick", m.getUsername());
                dataToSend.put("mkey", m.getMobilKey());
                String encodedStr = getEncodedData(dataToSend);
                postMenemenData(RadyoMenemenPro.PUSH_ONLINE_SIGNAL, encodedStr);
            }
        }).start();
    }

    @Override
    public void onStop() {
        m.bool_kaydet(RadyoMenemenPro.IS_CHAT_FOREGROUND,false);//Sohbet ön planda değil: bildirim gelebilir
        if(getActivity()!=null)  LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).unregisterReceiver(Chatreceiver);
        if(toolbar != null) toolbar.setSubtitle("");
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
    int first_visible_view;
    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSavedInstanceState");
        if(linearLayoutManager != null){
            first_visible_view = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
            if(first_visible_view > 20) {
                outState.putInt("first_visible_view", first_visible_view);
                Log.d(TAG, "onSavedInstanceState last visible view position saved " + first_visible_view );
            }
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        if(savedInstanceState != null && sohbetRV != null && sohbetList != null){
            first_visible_view = savedInstanceState.getInt("first_visible_view");
            first_visit = true;
            if(first_visible_view > 0){
                    new initsohbet(first_visible_view + 20, first_visible_view).execute();
                    first_visit = false;
            }
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.sohbet_menu,menu);
       MenuItem silentN = menu.findItem(R.id.action_silent_notification);
        if(m.getSavedTime(RadyoMenemenPro.MUTE_NOTIFICATION) > System.currentTimeMillis())
            silentN.setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemid = item.getItemId();
        switch (itemid){
            case R.id.action_silent_notification:
                m.saveTime("mute_notif",(1000*60*10));
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
                m.runEnterAnimation(image_pick, 0);
                resimekle.hide();
                break;
            case R.id.take_photo:
                takePhoto();
                image_pick.setVisibility(View.GONE);
                resimekle.show();
                break;
            case R.id.pick_from_gallery:
                selectFromGallery();
                image_pick.setVisibility(View.GONE);
                resimekle.show();
                break;
            case R.id.cancel_image_pick:
                m.runExitAnimation(image_pick,0);
                resimekle.show();
                break;
            case R.id.scrolltoTop:
                try {
                    sohbetRV.scrollToPosition(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private void takePhoto() {
//        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && getActivity().getApplicationContext().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(new String[]{Manifest.permission.CAMERA},
//                    CAM_PERMISSION_REQUEST_ID);
//            return;
//        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final Uri uri = tempUri();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        m.kaydet(RadyoMenemenPro.LASTURI, uri.toString());
        startActivityForResult(intent,RESULT_LOAD_IMAGE_CAM);
    }

    private void selectFromGallery() {
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M &&getActivity().getApplicationContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //Dosya okuma izni yok izin iste
            AskReadPerm();
            return;
        }
        Intent resimsec = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                .setType("image/*");
        startActivityForResult(resimsec, RESULT_LOAD_IMAGE);
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

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

        private void postToMenemen(final String mesaj) {
        new AsyncTask<Void,Void,Boolean>(){
            @Override
            protected void onPreExecute() {
                if(mesaj.toLowerCase().equals("çatçut")) {
                    setChatSound();
                }else if(sohbetList != null && sohbetRV != null){
                    sohbetList.add(0,new Sohbet_Objects(null,m.getUsername(),mesaj, PENDING));
                    if(sohbetRV.getAdapter() != null)
                        sohbetRV.getAdapter().notifyDataSetChanged();
                }

                super.onPreExecute();
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                if(mesaj.toLowerCase().equals("çatçut")) return true;
                Map<String, String> dataToSend = new HashMap<>();
                dataToSend.put("nick", m.getUsername());
                dataToSend.put("mkey", m.getMobilKey());
                dataToSend.put("mesaj", mesaj);
                String encodedStr = getEncodedData(dataToSend);
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
                    if(sohbetList != null && sohbetRV != null){
                        if(sohbetList.get(0).nick.equals(m.getUsername()) && sohbetList.get(0).mesaj.equals(mesaj))
                            sohbetList.set(0,new Sohbet_Objects(null,m.getUsername(),mesaj, NOT_DELIVERED));
                        else {
                            for (int i = 0; i < sohbetList.size(); i++) {
                                if (sohbetList.get(i).mesaj.equals(mesaj)) {
                                    sohbetList.set(i,new Sohbet_Objects(null,m.getUsername(),mesaj, NOT_DELIVERED));
                                    sohbetRV.getAdapter().notifyDataSetChanged();
                                }
                            }
                        }
                        if(sohbetRV.getAdapter() != null)
                            sohbetRV.getAdapter().notifyDataSetChanged();
                        if(m.isFirstTime(NOT_DELIVERED))
                            Toast.makeText(getActivity().getApplicationContext(), R.string.toast_msg_not_sent, Toast.LENGTH_LONG).show();
                    }
                }
                super.onPostExecute(success);
            }
        }.execute();
    }



    /**
     * Creates a uri before picking image from camera
     * @return temproray image uri
     */
    private Uri tempUri(){
        ContentResolver cr = getActivity().getContentResolver();
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "temp");
        return cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
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
    public class SohbetAdapter extends RecyclerView.Adapter<sohbet.SohbetAdapter.chatViewHolder> {
        Context context;
        List<Sohbet_Objects> sohbetList;

        public class chatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            TextView nick,mesaj,zaman;
            CardView card;
            ImageView caps;
            chatViewHolder(View itemView) {
                super(itemView);
                nick = (TextView) itemView.findViewById(R.id.username);
                mesaj = (TextView) itemView.findViewById(R.id.mesaj);
                zaman = (TextView) itemView.findViewById(R.id.zaman);
                card = (CardView) itemView.findViewById(R.id.sohbetcard);
                caps = (ImageView) itemView.findViewById(R.id.caps);
                caps.setOnClickListener(this);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    caps.setTransitionName("show_image");
                card.setOnClickListener(this);
                mesaj.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                if (view == caps) {
                    if(sohbetList.get(getAdapterPosition()).mesaj.contains("radyomenemen.com/images")) {
                        String capsurl = getCapsUrl(fromHtmlCompat(sohbetList.get(getAdapterPosition()).mesaj));
                        m.goToCapsIntent(capsurl,caps,getActivity());
                    }else if(sohbetList.get(getAdapterPosition()).mesaj.contains("youtube.com/watch") || sohbetList.get(getAdapterPosition()).mesaj.contains("youtu.be/")){
                        m.openYoutubeLink(getYoutubeId(Menemen.fromHtmlCompat(sohbetList.get(getAdapterPosition()).mesaj)));
                    }
                } else {
                    String zaman_val = sohbetList.get(getAdapterPosition()).zaman;
                    try {
                        if (zaman_val.equals(PENDING)) return;
                        if (zaman_val.equals(NOT_DELIVERED)) {
                            final String mesaj = sohbetList.get(getAdapterPosition()).mesaj;
                            sohbetList.remove(getAdapterPosition());
                            sohbetRV.getAdapter().notifyItemRemoved(getAdapterPosition());
                            postToMenemen(mesaj);
                            return;
                        }
                        if (zaman.getText().toString().equals(zaman_val))
                            zaman.setText(getTimeAgo(zaman_val, context));
                        else zaman.setText(zaman_val);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }


        }
        SohbetAdapter(List<Sohbet_Objects> sohbetList){
            this.sohbetList = sohbetList;
        }

        @Override
        public chatViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.sohbet_item, viewGroup,false);
            chatViewHolder pvh = new chatViewHolder(v);
            context = viewGroup.getContext();
            return pvh;
        }
        @Override
        public int getItemCount() {
            return sohbetList.size();
        }

        @Override
        public void onBindViewHolder(chatViewHolder chatViewHolder, int i) {
            chatViewHolder.nick.setText(sohbetList.get(i).nick);
            chatViewHolder.mesaj.setText(m.getSpannedTextWithSmileys(sohbetList.get(i).mesaj));
            chatViewHolder.mesaj.setMovementMethod(LinkMovementMethod.getInstance());
            chatViewHolder.zaman.setText(getTimeAgo(sohbetList.get(i).zaman,context));
        }


        @Override
        public void onViewAttachedToWindow(chatViewHolder chatViewHolder) {
            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("show_thumbnail", true)) {
                if(chatViewHolder.mesaj.getText().toString().contains("radyomenemen.com/images")){
                    //resim urlsi içeriyorum
                    loadCapsinChat(chatViewHolder, getThumbnail(getCapsUrl(fromHtmlCompat(chatViewHolder.mesaj.getText().toString()))) );
                }else if(chatViewHolder.mesaj.getText().toString().contains("youtube.com/watch") || chatViewHolder.mesaj.getText().toString().contains("youtu.be/")){
                    Log.v(TAG,"youtube " + getYoutubeThumbnail(getYoutubeId(fromHtmlCompat(chatViewHolder.mesaj.getText().toString()))));
                    loadCapsinChat(chatViewHolder, getYoutubeThumbnail(getYoutubeId(fromHtmlCompat(chatViewHolder.mesaj.getText().toString()))));
                }else chatViewHolder.caps.setImageDrawable(null);
            }
            super.onViewAttachedToWindow(chatViewHolder);
        }

        private void loadCapsinChat(chatViewHolder chatViewHolder, String mesaj) {
            try {
                Glide.with(context)
                        .load(mesaj)
                        .override(RadyoMenemenPro.GALLERY_IMAGE_OVERRIDE_WITDH / 2, RadyoMenemenPro.GALLERY_IMAGE_OVERRIDE_HEIGHT / 2)
                        .into(chatViewHolder.caps);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
                final Uri saveduri = Uri.parse(m.oku(RadyoMenemenPro.LASTURI));
                Log.v(TAG,"temp " + saveduri);
                Bitmap bitmap;
                InputStream image_stream = getActivity().getContentResolver().openInputStream(saveduri);
                bitmap= BitmapFactory.decodeStream(image_stream);
                new CapsYukle(bitmap,getActivity().getApplicationContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                Toast.makeText(getActivity().getApplicationContext(), R.string.caps_uploading, Toast.LENGTH_SHORT).show();
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


    class forceSync extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            if(!m.isInternetAvailable()) return null;
            String line = getMenemenData(RadyoMenemenPro.MESAJLAR + "&sonmsg=1");
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
            }catch (Exception e){
                m.resetFirstTime("loadmessages");
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            new initsohbet(20,0).execute();
            super.onPostExecute(aVoid);
        }
    }

    class initsohbet extends AsyncTask<Void,Void,Void>{
        int limit;
        int scroll;

        public initsohbet(int limit, int scroll) {
            this.limit = limit;
            this.scroll = scroll;
        }

        @Override
        protected void onPreExecute() {
            sohbetList = new ArrayList<>();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            //Getfrom db
            try {
                Cursor cursor = sql.getHistory(limit);
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
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.d(TAG, "initsohbet " + limit + " " + scroll);
            if(sohbetList!=null) SohbetAdapter = new SohbetAdapter(sohbetList);
            if(SohbetAdapter!=null) sohbetRV.setAdapter(SohbetAdapter);
            if(swipeRV != null) swipeRV.setRefreshing(false);
            if(sohbetList != null && sohbetList.size()>1 && sohbetList.get(0).id != null) m.kaydet(RadyoMenemenPro.LAST_ID_SEEN_ON_CHAT ,sohbetList.get(0).id);
            if(sohbetRV != null) sohbetRV.scrollToPosition(scroll);
            super.onPostExecute(aVoid);
        }
    }

    class loadMore extends AsyncTask<Void,Void,Void>{
        String lastid;
        int listSize;

        public loadMore(String lastid, int listSize) {
            this.lastid = lastid;
            this.listSize = listSize;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Cursor cursor = sql.getHistoryOnScroll(lastid);
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
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(sohbetList == null) return;
            if(sohbetRV != null && sohbetRV.getAdapter() != null){
              notifyItemsSync();
            }
        }

        private synchronized void notifyItemsSync() {
            try {
                sohbetRV.getAdapter().notifyItemRangeInserted(listSize,20);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
