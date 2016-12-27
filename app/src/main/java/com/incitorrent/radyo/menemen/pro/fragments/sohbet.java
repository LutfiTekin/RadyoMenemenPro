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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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
import android.support.v7.widget.SimpleItemAnimator;
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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;
import com.incitorrent.radyo.menemen.pro.services.FIREBASE_CM_SERVICE;
import com.incitorrent.radyo.menemen.pro.utils.CapsYukle;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;
import com.incitorrent.radyo.menemen.pro.utils.WrapContentLinearLayoutManager;
import com.incitorrent.radyo.menemen.pro.utils.chatDB;
import com.incitorrent.radyo.menemen.pro.utils.deletePost;
import com.incitorrent.radyo.menemen.pro.utils.topicDB;
import com.incitorrent.radyo.menemen.pro.utils.trackonlineusersDB;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.incitorrent.radyo.menemen.pro.utils.Menemen.DELIVERED;
import static com.incitorrent.radyo.menemen.pro.utils.Menemen.NOT_DELIVERED;
import static com.incitorrent.radyo.menemen.pro.utils.Menemen.PENDING;
import static com.incitorrent.radyo.menemen.pro.utils.Menemen.fromHtmlCompat;
import static com.incitorrent.radyo.menemen.pro.utils.Menemen.getCapsUrl;
import static com.incitorrent.radyo.menemen.pro.utils.Menemen.getFormattedDate;
import static com.incitorrent.radyo.menemen.pro.utils.Menemen.getThumbnail;
import static com.incitorrent.radyo.menemen.pro.utils.Menemen.getTimeAgo;
import static com.incitorrent.radyo.menemen.pro.utils.Menemen.getYoutubeId;
import static com.incitorrent.radyo.menemen.pro.utils.Menemen.getYoutubeThumbnail;


public class sohbet extends Fragment implements View.OnClickListener{

    private static final String TAG = "SOHBETFRAG";
    private static final int RESULT_LOAD_IMAGE_CAM = 2063;
    private static final int RESULT_LOAD_IMAGE = 2064;
    private static final int PERMISSION_REQUEST_ID = 2065;
    private EditText mesaj;
    private ImageView smilegoster;
    FloatingActionButton resimekle,scrollTop;
    private RecyclerView smileRV,sohbetRV;
    Menemen m;
    Context context;
    List<Satbax_Smiley_Objects> satbaxSmileList;
    List<Sohbet_Objects> sohbetList;
    SatbaxSmileAdapter Smileadapter;
    SohbetAdapter SohbetAdapter;
    BroadcastReceiver Chatreceiver;
    SwipeRefreshLayout swipeRV;
    LinearLayoutManager linearLayoutManager;
    CardView image_pick;
    ImageView take_photo,pick_gallery,cancel_image_pick;
    Toolbar toolbar;
    private Handler mHandler;
    private boolean isScrolling = false;
    /**
     * A boolean value for differenciating Topic messages by given id or General Chat
     * Since they are both using same template and fragment
     */
    private boolean TOPIC_MODE = false;
    private String TOPIC_ID = "0";
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
        context = getActivity().getApplicationContext();
        m = new Menemen(context);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            TOPIC_ID = bundle.getString(topicDB._TOPICID);
            if(TOPIC_ID != null)
                //Get Topic id and switch the topic mode on
                TOPIC_MODE = true;
            else
                //Getting topic id is failed set TOPIC_ID to initial value
                TOPIC_ID = "0";
        }
        if(getActivity()!=null) {
            if(TOPIC_MODE)
                getActivity().setTitle(m.getTopicDB().getTopicInfo(TOPIC_ID,topicDB._TITLE));
            else
                getActivity().setTitle(getString(R.string.nav_sohbet));
            toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
            if(toolbar!=null && TOPIC_MODE)
                m.setToolbarSubtitleMarquee(toolbar,m.getTopicDB().getTopicInfo(TOPIC_ID,topicDB._DESCR));
        }

        resimekle = (FloatingActionButton) sohbetView.findViewById(R.id.resim_ekle);
        smilegoster = (ImageView) sohbetView.findViewById(R.id.smile_goster_button);
        mesaj = (EditText) sohbetView.findViewById(R.id.ETmesaj);
        mesaj.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if(mesaj.getText().toString().trim().length()>0) {
                        postToMenemen(mesaj.getText().toString());
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
                = new GridLayoutManager(context,(getResources().getBoolean(R.bool.landscape_mode))? 10 : 6);
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
        linearLayoutManager = new WrapContentLinearLayoutManager(context);
        sohbetRV.setLayoutManager(linearLayoutManager);
        sohbetRV.setHasFixedSize(true);
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
                    if(LAST_POSITION_COMP_VISIBLE > (LIST_SIZE - 5) )
                        new loadMore(lastid, LIST_SIZE).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                    if(LAST_POSITION_COMP_VISIBLE > 100 ) {
                        scrollTop.show();
                        if(getActivity()!=null) {
                            toolbar.setSubtitle(getTimeAgo(sohbetList.get(LAST_POSITION_COMP_VISIBLE).zaman, context));
                        }
                    }
                    else if(LAST_POSITION_COMP_VISIBLE < 20) {
                        if(scrollTop.getVisibility() == View.VISIBLE) scrollTop.hide();
                        if(getActivity()!=null && toolbar != null) {
                            trackonlineusersDB sql = new trackonlineusersDB(context,null,null,1);
                            final int count = sql.getOnlineUserCount(null);
                            if(count > 0 && toolbar !=null) {
                                if(count == 1)
                                    toolbar.setSubtitle(R.string.toolbar_online_subtitle_one);
                                else
                                    toolbar.setSubtitle(String.format(context.getString(R.string.toolbar_online_subtitle), count));
                            }else toolbar.setSubtitle("");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                isScrolling = (newState == RecyclerView.SCROLL_STATE_DRAGGING) || (newState == RecyclerView.SCROLL_STATE_SETTLING);
                super.onScrollStateChanged(recyclerView, newState);
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
                forceSyncMSGs();
            }
        });
        Chatreceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getExtras();
              if(bundle==null)
                new initsohbet(20,0).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
              else {
                  String Action = intent.getAction();
                  switch (Action) {
                      case FIREBASE_CM_SERVICE.CHAT_BROADCAST_FILTER:
                          try {
                              if(TOPIC_MODE){
                                  String topicid = bundle.getString("tid");
                                  if(topicid == null) return;
                                  if(!topicid.equals(TOPIC_ID)) return;
                              }
                              String action = bundle.getString("action");
                              if (action == null) return;
                              String id = bundle.getString("msgid");
                              if (action.equals(FIREBASE_CM_SERVICE.ADD)) {
                                  String nick = bundle.getString("nick");
                                  String mesaj = bundle.getString("msg");
                                  if (sohbetList == null || sohbetRV == null || sohbetRV.getAdapter() == null)
                                      return;
                                  if (sohbetList.get(0).mesaj.equals(mesaj) && sohbetList.get(0).nick.equals(nick))
                                      sohbetList.set(0, new Sohbet_Objects(id, nick, mesaj, getFormattedDate(System.currentTimeMillis(), RadyoMenemenPro.CHAT_DATE_FORMAT)));
                                  else {
                                      //Fallback and search for it
                                      for (int i = 0; i < sohbetList.size(); i++) {
                                          if (sohbetList.get(i).mesaj.equals(mesaj) || (sohbetList.get(i).zaman != null && sohbetList.get(i).zaman.equals(Menemen.PENDING))) {
                                              sohbetList.remove(i);
                                              sohbetRV.getAdapter().notifyItemRemoved(i);
                                          }
                                      }
                                      sohbetList.add(0, new Sohbet_Objects(id, nick, mesaj, getFormattedDate(System.currentTimeMillis(), RadyoMenemenPro.CHAT_DATE_FORMAT)));
                                  }
                                  sohbetRV.getAdapter().notifyDataSetChanged();
                                  //Scroll to top if new message added
                                  if (((LinearLayoutManager) sohbetRV.getLayoutManager()).findLastCompletelyVisibleItemPosition() < 30)
                                      sohbetRV.smoothScrollToPosition(0);
                                  else sohbetRV.scrollToPosition(0);
                                  m.kaydet(RadyoMenemenPro.LAST_ID_SEEN_ON_CHAT, id);
                              } else if (action.equals(FIREBASE_CM_SERVICE.DELETE)) {
                                  if (TOPIC_MODE)
                                      m.getTopicDB().deleteMSG(id);
                                  else
                                      m.getChatDB().deleteMSG(id);
                                  for (int i = 0; i < sohbetList.size(); i++) {
                                      if (id == null) break;
                                      if (sohbetList.get(i).id.equals(id)) {
                                          sohbetList.remove(i);
                                          sohbetRV.getAdapter().notifyItemRemoved(i);
                                      }
                                  }
                              }
                          } catch (Exception e) {
                              if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("show_full_error", false))
                                  Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                              e.printStackTrace();
                          }
                          break;
                      case FIREBASE_CM_SERVICE.USERS_ONLINE_BROADCAST_FILTER:
                          int count = intent.getExtras().getInt("count", 0);
                          if (count > 0 && toolbar != null) {
                              m.setToolbarSubtitleMarquee(toolbar, count == 1 ? getString(R.string.toolbar_online_subtitle_one) : String.format(context.getString(R.string.toolbar_online_subtitle), count));
                          }
                          break;
                  }

              }
            }
        };
        if(m.isFirstTime("downloadmessages")) forceSyncMSGs();
        mHandler = new Handler();
        startRepeatingTask();
        setRetainInstance(true);
        setHasOptionsMenu(true);
        return sohbetView;
    }





    @Override
    public void onStart() {
        if(getActivity()!=null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(FIREBASE_CM_SERVICE.CHAT_BROADCAST_FILTER);
            filter.addAction(FIREBASE_CM_SERVICE.USERS_ONLINE_BROADCAST_FILTER);
            LocalBroadcastManager.getInstance(context).registerReceiver((Chatreceiver),filter);
        }
        super.onStart();
    }

    @Override
    public void onResume() {
        if(TOPIC_MODE)
            m.bool_kaydet(RadyoMenemenPro.IS_CHAT_FOREGROUND+"tid"+TOPIC_ID,true);
        else
            m.bool_kaydet(RadyoMenemenPro.IS_CHAT_FOREGROUND,true);
        //Eğer önceden liste oluşturuldu ise yeniden yükleme
       if((sohbetList == null || sohbetList.size() < 1))
           new initsohbet(30,0).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else if(sohbetList != null && sohbetList.size() > 1){
           try {
               if(Integer.parseInt(sohbetList.get(0).id) < Integer.parseInt(
                       (TOPIC_MODE) ? m.getTopicDB().lastMsgId() : m.getChatDB().lastMsgId())){
                   new initsohbet(20,0).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
               }else{
                   updateListSilently();
               }
           }catch (Exception e){e.printStackTrace();}
       }
        NotificationManagerCompat.from(context).cancel(FIREBASE_CM_SERVICE.CHAT_NOTIFICATION);
        m.runEnterAnimation(resimekle,250);
        iAmOnline();
        super.onResume();
    }

    void updateListSilently() {
        //Prevent glitches: if user is isScrolling do not try to update UI
        if(isScrolling) return;
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected void onPreExecute() {
                int firstitempos = linearLayoutManager.findFirstVisibleItemPosition();
                if(sohbetRV.getAdapter() != null) {
                    //Disable animations before to flickering happens
                    ((SimpleItemAnimator) sohbetRV.getItemAnimator()).setSupportsChangeAnimations(false);
                    sohbetRV.getAdapter().notifyItemRangeChanged(firstitempos, 10);
                }
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                //Re-Enable animations
                ((SimpleItemAnimator) sohbetRV.getItemAnimator()).setSupportsChangeAnimations(true);
                super.onPostExecute(aVoid);
            }

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    //Insure flickering not happening
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void iAmOnline() {
        if(m.getSavedTime("online_push") > System.currentTimeMillis()) return;
        m.saveTime("online_push",(1000 * 60 * 2));
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest post = new StringRequest(Request.Method.POST, RadyoMenemenPro.PUSH_ONLINE_SIGNAL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    }
                },null){
            @Override
            protected Map<String, String> getParams() {
                return m.getAuthMap();
            }

            @Override
            public Priority getPriority() {
                return Priority.IMMEDIATE;
            }
        };
        queue.add(post);
    }

    @Override
    public void onStop() {
        if(TOPIC_MODE)
            m.bool_kaydet(RadyoMenemenPro.IS_CHAT_FOREGROUND+"tid"+TOPIC_ID,false);
        else
            m.bool_kaydet(RadyoMenemenPro.IS_CHAT_FOREGROUND,false);
        if(getActivity()!=null)  LocalBroadcastManager.getInstance(context).unregisterReceiver(Chatreceiver);
        if(toolbar != null) toolbar.setSubtitle("");
        super.onStop();
    }



    @Override
    public void onDestroy() {
        stopRepeatingTask();
        super.onDestroy();
    }

    int first_visible_view;
    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(linearLayoutManager != null){
            first_visible_view = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
            if(first_visible_view > 20) {
                outState.putInt("first_visible_view", first_visible_view);
            }
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if(savedInstanceState != null && sohbetRV != null && sohbetList != null){
            first_visible_view = savedInstanceState.getInt("first_visible_view");
            new initsohbet(first_visible_view + 20, first_visible_view).execute();
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
            case R.id.go_to_online_users:
                getFragmentManager().beginTransaction().replace(R.id.Fcontent, new online(), "online").addToBackStack("online").commit();
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
                    if(sohbetList.size()> 200)
                        sohbetRV.scrollToPosition(0);
                    else sohbetRV.smoothScrollToPosition(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private void takePhoto() {
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M &&context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //Dosya okuma izni yok izin iste
            AskReadPerm();
            return;
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final Uri uri = tempUri();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        m.kaydet(RadyoMenemenPro.LASTURI, uri.toString());
        startActivityForResult(intent,RESULT_LOAD_IMAGE_CAM);
    }

    private void selectFromGallery() {
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M &&context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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

        private void postToMenemen(final String msg) {
            if(!m.isInternetAvailable()) {
                Toast.makeText(context, R.string.toast_internet_warn, Toast.LENGTH_SHORT).show();
                return;
            }
            mesaj.setText("");
            if(sohbetList != null && sohbetRV != null){
                sohbetList.add(0,new Sohbet_Objects(null,m.getUsername(),msg, PENDING));
                if(sohbetRV.getAdapter() != null)
                    sohbetRV.getAdapter().notifyDataSetChanged();
            }
            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest post = new StringRequest(Request.Method.POST, RadyoMenemenPro.MESAJ_GONDER,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                Log.d(TAG, "Volley Response " + response);
                                JSONObject j = new JSONObject(response).getJSONArray("post").getJSONObject(0);
                                if(j.get("status").equals("ok")) {
                                    //Başarılı
                                    if(sohbetList.get(0).zaman != null)
                                        if(sohbetList.get(0).nick.equals(m.getUsername()) && sohbetList.get(0).mesaj.equals(msg) && sohbetList.get(0).zaman.equals(PENDING))
                                            sohbetList.set(0,new Sohbet_Objects(null,m.getUsername(),msg, DELIVERED));
                                        else {
                                            for (int i = 0; i < sohbetList.size(); i++)
                                                if(sohbetList.get(i).zaman != null)
                                                    if (sohbetList.get(i).mesaj.equals(msg) && sohbetList.get(i).zaman.equals(PENDING))
                                                        sohbetList.set(i, new Sohbet_Objects(null, m.getUsername(), msg, DELIVERED));
                                        }
                                }else{
                                    if(sohbetList.get(0).nick.equals(m.getUsername()) && sohbetList.get(0).mesaj.equals(msg))
                                        sohbetList.set(0,new Sohbet_Objects(null,m.getUsername(),msg, NOT_DELIVERED));
                                    else {
                                        for (int i = 0; i < sohbetList.size(); i++)
                                            if (sohbetList.get(i).mesaj.equals(msg)) {
                                                sohbetList.set(i, new Sohbet_Objects(null, m.getUsername(), msg, NOT_DELIVERED));
                                                break;
                                            }
                                    }

                                    if(m.isFirstTime(NOT_DELIVERED))
                                        Toast.makeText(context, R.string.toast_msg_not_sent, Toast.LENGTH_LONG).show();
                                }
                                //notify adapter
                                if(sohbetRV.getAdapter() != null && sohbetList != null)
                                    sohbetRV.getAdapter().notifyDataSetChanged();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG,"Volley Error " + error.toString());
                    if(m.isFirstTime(NOT_DELIVERED))
                        Toast.makeText(context, R.string.toast_msg_not_sent, Toast.LENGTH_LONG).show();
                }
            }){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> dataToSend = m.getAuthMap();
                    dataToSend.put("mesaj", msg);
                    return dataToSend;
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
            queue.add(post);
    }



    /**
     * Creates a uri before picking image from camera
     * @return temporary image uri
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
        Sohbet_Objects(String id, String nick, String mesaj, String zaman) {
            this.id = id;
            this.nick = nick;
            this.mesaj = mesaj;
            this.zaman = zaman;
        }
    }
    public class SohbetAdapter extends RecyclerView.Adapter<sohbet.SohbetAdapter.chatViewHolder> {
        Context context;
        List<Sohbet_Objects> sohbetList;

        class chatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
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
                        if (zaman_val.equals(PENDING) || zaman_val.equals(DELIVERED)) return;
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
                    loadCapsinChat(chatViewHolder, getThumbnail(getCapsUrl(fromHtmlCompat(chatViewHolder.mesaj.getText().toString()))) );
                    //Preload if wifi enabled
                   if(m.isConnectedWifi() && PreferenceManager.getDefaultSharedPreferences(context).getBoolean("preload_if_wifi",true)) {
                       Glide.with(context).load(getCapsUrl(fromHtmlCompat(chatViewHolder.mesaj.getText().toString()))).diskCacheStrategy(DiskCacheStrategy.SOURCE).preload();
                   }
                }else if(chatViewHolder.mesaj.getText().toString().contains("youtube.com/watch") || chatViewHolder.mesaj.getText().toString().contains("youtu.be/")){
                    loadCapsinChat(chatViewHolder, getYoutubeThumbnail(getYoutubeId(fromHtmlCompat(chatViewHolder.mesaj.getText().toString()))));
                }else chatViewHolder.caps.setImageDrawable(null);
            }
            chatViewHolder.zaman.invalidate();
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
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), selectedimage);
                   new CapsYukle(bitmap,context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                Toast.makeText(context, R.string.caps_uploading, Toast.LENGTH_SHORT).show();
            }catch (Exception e){e.printStackTrace();}
        }else if(requestCode == RESULT_LOAD_IMAGE_CAM && resultCode!=0){ //resultCode 0: kameradan seçim iptal edildi
            try {
                final Uri saveduri = Uri.parse(m.oku(RadyoMenemenPro.LASTURI));
                Bitmap bitmap;
                InputStream image_stream = getActivity().getContentResolver().openInputStream(saveduri);
                bitmap= BitmapFactory.decodeStream(image_stream);
                new CapsYukle(bitmap,context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                Toast.makeText(context, R.string.caps_uploading, Toast.LENGTH_SHORT).show();
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
        Satbax_Smiley_Objects(String smile, String smileid) {
            this.smile = smile;
            this.smileid = smileid;
        }
    }
    public class SatbaxSmileAdapter extends RecyclerView.Adapter<SatbaxSmileAdapter.PersonViewHolder> {
        Context context;
        List<Satbax_Smiley_Objects> satbaxSmileList;
        class PersonViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
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

    private void forceSyncMSGs() {
        if(TOPIC_MODE) {
            //In topic mode messages aren't stored
            // in the server like general chat messages
            new initsohbet(20, 0).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            return;
        }
        RequestQueue q = Volley.newRequestQueue(context);
        StringRequest r = new StringRequest(Request.Method.POST, RadyoMenemenPro.MESAJLAR + "&sonmsg=1",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String response) {
                    new AsyncTask<Void,Void,Void>(){
                        @Override
                        protected Void doInBackground(Void... voids) {
                            try {
                                JSONArray arr = new JSONObject(response).getJSONArray("mesajlar");
                                JSONObject c;
                                for(int i = 0;i<arr.getJSONArray(0).length();i++){
                                    String tid,id,nick,mesaj,zaman;
                                    JSONArray innerJarr = arr.getJSONArray(0);
                                    c = innerJarr.getJSONObject(i);
                                    id = c.getString("id");
                                    nick = c.getString("nick");
                                    mesaj = c.getString("post");
                                    zaman = c.getString("time");
                                    //db ye ekle
                                    m.getChatDB().addtoHistory(new chatDB.CHAT(id,nick,mesaj,zaman));
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            new initsohbet(20,0).execute();
                            super.onPostExecute(aVoid);
                        }
                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                }, null){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return m.getAuthMap();
            }
        };
        q.add(r);
    }

    class initsohbet extends AsyncTask<Void,Void,Void>{
        int limit;
        int scroll;

        initsohbet(int limit, int scroll) {
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
                Cursor cursor = (TOPIC_MODE) ? m.getTopicDB().getHistory(limit,TOPIC_ID) : m.getChatDB().getHistory(limit);
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
            if(sohbetRV != null && scroll != 0) sohbetRV.scrollToPosition(scroll);
            super.onPostExecute(aVoid);
        }
    }

    class loadMore extends AsyncTask<Void,Void,Void>{
        String lastid;
        int listSize;

        loadMore(String lastid, int listSize) {
            this.lastid = lastid;
            this.listSize = listSize;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Cursor cursor =
                        (TOPIC_MODE) ? m.getTopicDB().getTopicMessagesOnScroll(lastid, TOPIC_ID) : m.getChatDB().getHistoryOnScroll(lastid);
                if(cursor == null) return null;
                cursor.moveToFirst();
                while(!cursor.isAfterLast()){
                    String id,nick,post,time;
                    id = cursor.getString(cursor.getColumnIndex(chatDB._MSGID));
                    nick = cursor.getString(cursor.getColumnIndex(chatDB._NICK));
                    post = cursor.getString(cursor.getColumnIndex(chatDB._POST));
                    time = cursor.getString(cursor.getColumnIndex(chatDB._TIME));
                    int exist = 0;
                    //Prevent duplicate values being added to the array list
                    for (int i = 0; i < sohbetList.size(); i++) {
                        if (sohbetList.get(i).mesaj.equals(post)) {
                           exist++;
                            break;
                        }
                    }
                    if(exist < 1) sohbetList.add(new Sohbet_Objects(id,nick,post,time));
                    cursor.moveToNext();
                }
                cursor.close();
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
                sohbetRV.getAdapter().notifyItemRangeInserted(listSize,40);
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

            if(sohbetList != null && sohbetList.size() > 0 && sohbetList.get(viewHolder.getAdapterPosition()).nick.equals(m.oku("username"))) { //Kendi mesajı, silebilir
                    Snackbar sn = Snackbar.make(smilegoster, R.string.message_deleted,Snackbar.LENGTH_SHORT).setCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar snackbar, int event) {
                    if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT || event == Snackbar.Callback.DISMISS_EVENT_SWIPE || event == Snackbar.Callback.DISMISS_EVENT_CONSECUTIVE) {
                        try {
                            //dbden sil
                            if(TOPIC_MODE)
                                m.getTopicDB().deleteMSG(sohbetList.get(position).id);
                            else
                                m.getChatDB().deleteMSG(sohbetList.get(position).id);
                            //siteyi güncelle
                            if(getActivity()!=null)  new deletePost(context,sohbetList.get(position).id).execute();
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
        if(getActivity()!=null)    Toast.makeText(context, R.string.toast_only_your_message_deleted,Toast.LENGTH_SHORT).show();
        }

        //Remove swiped item from list and notify the RecyclerView

    }
};

ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                updateListSilently();
            } finally {
                mHandler.postDelayed(mStatusChecker, RadyoMenemenPro.MENEMEN_TIMEOUT);
            }
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        try {
            mHandler.removeCallbacks(mStatusChecker);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
