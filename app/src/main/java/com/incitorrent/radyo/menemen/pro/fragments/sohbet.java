package com.incitorrent.radyo.menemen.pro.fragments;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.SearchManager;
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
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.transition.Slide;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
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
import com.google.firebase.messaging.FirebaseMessaging;
import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;
import com.incitorrent.radyo.menemen.pro.services.FIREBASE_CM_SERVICE;
import com.incitorrent.radyo.menemen.pro.utils.CapsYukle;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;
import com.incitorrent.radyo.menemen.pro.utils.NavHeader;
import com.incitorrent.radyo.menemen.pro.utils.WrapContentLinearLayoutManager;
import com.incitorrent.radyo.menemen.pro.utils.chatDB;
import com.incitorrent.radyo.menemen.pro.utils.deletePost;
import com.incitorrent.radyo.menemen.pro.utils.topicDB;
import com.incitorrent.radyo.menemen.pro.utils.trackonlineusersDB;

import org.json.JSONArray;
import org.json.JSONException;
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
    private EditText ETmesaj;
    private ImageView smilegoster;
    private TextView emptyview;
    FloatingActionButton resimekle,scrollTop;
    private RecyclerView smileRV,sohbetRV;
    Menemen m;
    Context context;
    List<Satbax_Smiley_Objects> satbaxSmileList;
    List<Sohbet_Objects> sohbetList;
    ArrayList<String> userlist;
    SatbaxSmileAdapter Smileadapter;
    SohbetAdapter SohbetAdapter;
    BroadcastReceiver Chatreceiver;
    SwipeRefreshLayout swipeRV;
    LinearLayoutManager linearLayoutManager;
    CardView image_pick;
    ImageView take_photo,pick_gallery,cancel_image_pick;
    Toolbar toolbar;
    private Handler mHandler;
    private NavHeader navHeader;
    private boolean isScrolling = false;
    /**
     * A boolean value for differentiating Topic messages by given id or General Chat
     * Since they are both using same template and fragment
     */
    private boolean TOPIC_MODE = false;
    /**
     * A boolean for checking whether it is normal topic or pm topic
     */
    private boolean IS_PM = false;
    private String TOPIC_ID = "0";

    MenuItem searchItem;
    boolean isUserSearching = false;
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
            if(TOPIC_MODE) {
                IS_PM = m.getTopicDB().getTopicInfo(TOPIC_ID,topicDB._DESCR).equals(RadyoMenemenPro.PM);
                final String topictitle = m.getTopicDB().getTopicInfo(TOPIC_ID, topicDB._TITLE);
                if(!topictitle.startsWith(RadyoMenemenPro.PM))
                    getActivity().setTitle(topictitle);
                emptyview = (TextView) sohbetView.findViewById(R.id.emptytextview);
                emptyview.setVisibility(View.VISIBLE);
                if(!m.getTopicDB().isJoined(TOPIC_ID))
                    promptJoinDialog();
            }
            else
                getActivity().setTitle(getString(R.string.nav_sohbet));
            toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
            if(toolbar!=null && TOPIC_MODE && !IS_PM)
                m.setToolbarSubtitleMarquee(toolbar,m.getTopicDB().getTopicInfo(TOPIC_ID,topicDB._DESCR));
        }

        resimekle = (FloatingActionButton) sohbetView.findViewById(R.id.resim_ekle);
        smilegoster = (ImageView) sohbetView.findViewById(R.id.smile_goster_button);
        ETmesaj = (EditText) sohbetView.findViewById(R.id.ETmesaj);
        ETmesaj.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if(ETmesaj.getText().toString().trim().length()>0) {
                        postToMenemen(ETmesaj.getText().toString());
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
                if(sohbetList == null || isUserSearching) return;
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
                            if (!IS_PM) {
                                if(count > 0 && toolbar !=null) {
                                    if(count == 1)
                                        toolbar.setSubtitle(R.string.toolbar_online_subtitle_one);
                                    else
                                        toolbar.setSubtitle(String.format(context.getString(R.string.toolbar_online_subtitle), count));
                                }else toolbar.setSubtitle("");
                            }
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
                                  emptyview.setVisibility(View.GONE);
                              }
                              String action = bundle.getString("action");
                              if (action == null) return;
                              String id = bundle.getString("msgid");
                              if (action.equals(FIREBASE_CM_SERVICE.ADD)) {
                                  String nick = bundle.getString("nick");
                                  String mesaj = bundle.getString("msg");
                                  if (sohbetList == null || sohbetRV == null || sohbetRV.getAdapter() == null)
                                      return;
                                  if(sohbetList.size()<1)
                                      sohbetList.add(0, new Sohbet_Objects(id, nick, mesaj, getFormattedDate(System.currentTimeMillis(), RadyoMenemenPro.CHAT_DATE_FORMAT)));
                                  else if (sohbetList.get(0).mesaj.equals(mesaj) && sohbetList.get(0).nick.equals(nick))
                                      sohbetList.set(0, new Sohbet_Objects(id, nick, mesaj, getFormattedDate(System.currentTimeMillis(), RadyoMenemenPro.CHAT_DATE_FORMAT)));
                                  else {
                                      //Fallback and search for it
                                      int searchmargin = (sohbetList.size()> 10) ? 10 : sohbetList.size();
                                      for (int i = 0; i < searchmargin; i++) {
                                          if (sohbetList.get(i).nick.equals(nick) && (sohbetList.get(i).mesaj.equals(mesaj) || sohbetList.get(i).mesaj.contains("http")) && (sohbetList.get(i).zaman != null && sohbetList.get(i).zaman.equals(Menemen.PENDING))) {
                                              sohbetList.remove(i);
                                              sohbetRV.getAdapter().notifyItemRemoved(i);
                                          }
                                      }
                                      sohbetList.add(0, new Sohbet_Objects(id, nick, mesaj, getFormattedDate(System.currentTimeMillis(), RadyoMenemenPro.CHAT_DATE_FORMAT)));
                                  }
                                  sohbetRV.getAdapter().notifyDataSetChanged();
                                  //Scroll to top if new message added
                                  if (((LinearLayoutManager) sohbetRV.getLayoutManager()).findLastCompletelyVisibleItemPosition() > 15)
                                      scrollTop.show();
                                  m.kaydet((TOPIC_MODE) ? RadyoMenemenPro.LAST_ID_SEEN_ON_TOPIC + TOPIC_ID : RadyoMenemenPro.LAST_ID_SEEN_ON_CHAT, id);
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
                              if (isAdded()) {
                                  if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("show_full_error", false))
                                      Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                              }
                              e.printStackTrace();
                          }
                          break;
                      case FIREBASE_CM_SERVICE.USERS_ONLINE_BROADCAST_FILTER:
                          if(ETmesaj.getText().toString().trim().length()<1)
                          if (!IS_PM) {
                              int count = intent.getExtras().getInt("count", 0);
                              if (count > 0 && toolbar != null) {
                                  m.setToolbarSubtitleMarquee(toolbar, count == 1 ? getString(R.string.toolbar_online_subtitle_one) : String.format(context.getString(R.string.toolbar_online_subtitle), count));
                              ETmesaj.requestFocus();
                              }
                          }else{
                              trackonlineusersDB sql = new trackonlineusersDB(context,null,null,1);
                              //extract username from title
                              String pm_user = m.getTopicDB().getTopicInfo(TOPIC_ID, topicDB._TITLE)
                                      .substring(2)
                                      .replaceAll(m.getUsername(), "")
                                      .replaceAll("\\+", "");
                              m.setToolbarSubtitleMarquee(toolbar, sql.isUserOnline(pm_user) ?
                                      String.format(getString(R.string.user_is_online), pm_user) :
                                      String.format(getString(R.string.user_is_offline), pm_user));
                          }
                          break;
                  }

              }
            }
        };
        if(m.isFirstTime("downloadmessages")) forceSyncMSGs();
        mHandler = new Handler();
        startUpdatingUISilently();
        setRetainInstance(!IS_PM);
        setHasOptionsMenu(!IS_PM);
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
        if(TOPIC_MODE) {
            m.bool_kaydet(RadyoMenemenPro.IS_CHAT_FOREGROUND + "tid" + TOPIC_ID, true);
            if(!m.getTopicDB().isTopicExists(TOPIC_ID)){
                if(isAdded())
                    Toast.makeText(context, R.string.topic_not_found, Toast.LENGTH_SHORT).show();
                getFragmentManager().beginTransaction().replace(R.id.Fcontent, new topics()).commit();
            }
            if (!IS_PM) {
                navHeader = new NavHeader().setHeaderView(((NavigationView) getActivity().findViewById(R.id.nav_view)).getHeaderView(0))
                        .setTitle(m.getTopicDB().getTopicInfo(TOPIC_ID, topicDB._TITLE))
                        .setSubTitle(m.getTopicDB().getTopicInfo(TOPIC_ID, topicDB._DESCR))
                        .setImage(RadyoMenemenPro.CAPS_IMAGES_PATH + m.getTopicDB().getTopicInfo(TOPIC_ID, topicDB._IMAGEURL));
                navHeader.build();
                navHeader.getSubHeaderTextView().setEllipsize(TextUtils.TruncateAt.END);
            }
        }
        else
            m.bool_kaydet(RadyoMenemenPro.IS_CHAT_FOREGROUND,true);
        //Eğer önceden liste oluşturuldu ise yeniden yükleme
       if((sohbetList == null || sohbetList.size() < 1))
           new initsohbet(30,0).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else if(sohbetList != null && sohbetList.size() > 1){
           try {
               String lastid = (TOPIC_MODE) ? m.getTopicDB().lastMsgId() : m.getChatDB().lastMsgId();
               if(!lastid.equals(sohbetList.get(0).id))
                   new initsohbet(30,0).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
           }catch (Exception e){
               e.printStackTrace();
               updateListSilently();
           }
       }
        int tid = 0;
        if(TOPIC_MODE) tid = Integer.parseInt(TOPIC_ID);
        NotificationManagerCompat.from(context).cancel(FIREBASE_CM_SERVICE.CHAT_NOTIFICATION + tid);
        m.runEnterAnimation(resimekle,250);
        iAmOnline();
        super.onResume();
    }

    private void promptJoinDialog() {
        if(isAdded())
            new AlertDialog.Builder(new ContextThemeWrapper(getActivity(),R.style.alertDialogTheme))
                    .setTitle(R.string.topic_join_prompt_title)
                    .setIcon(R.drawable.ic_topic_discussion)
                    .setMessage(String.format(getString(R.string.topic_join_prompt_message), m.getTopicDB().getTopicInfo(TOPIC_ID, topicDB._TITLE)))
                    .setPositiveButton(R.string.topics_join, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            RequestQueue queue = Volley.newRequestQueue(context);
                            Toast.makeText(context, R.string.topic_joining, Toast.LENGTH_SHORT).show();
                            queue.add(jointopic);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            getFragmentManager().beginTransaction().replace(R.id.Fcontent, new topics()).commit();
                            dialogInterface.cancel();
                        }
                    })
                    .setCancelable(false)
                    .show();
    }

    void updateListSilently() {
        //Prevent glitches: if user is isScrolling or searching do not try to update UI
        if(isScrolling || isUserSearching) return;
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
            m.bool_kaydet(RadyoMenemenPro.IS_CHAT_FOREGROUND + "tid" + TOPIC_ID, false);
        else
            m.bool_kaydet(RadyoMenemenPro.IS_CHAT_FOREGROUND, false);
        if(getActivity()!=null)  LocalBroadcastManager.getInstance(context).unregisterReceiver(Chatreceiver);
        if(toolbar != null) toolbar.setSubtitle("");
        if(TOPIC_MODE && navHeader != null)
            navHeader.clear();
        super.onStop();
    }



    @Override
    public void onDestroy() {
        stopUpdatingUI();
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
        if(IS_PM){
            super.onCreateOptionsMenu(menu, inflater);
            return;
        }
        menu.clear();
        inflater.inflate((TOPIC_MODE) ? R.menu.topic_messages_menu : R.menu.sohbet_menu,menu);
        if(m.getSavedTime(RadyoMenemenPro.MUTE_NOTIFICATION) > System.currentTimeMillis())
            menu.findItem(R.id.action_silent_notification).setVisible(false);
        try {
            if (TOPIC_MODE && m.getTopicDB().getTopicInfo(TOPIC_ID, topicDB._CREATOR).equals(m.getUsername())) {
                menu.findItem(R.id.action_close_topic).setVisible(true);
                menu.findItem(R.id.action_leave_topic).setVisible(false);
                menu.findItem(R.id.action_edit_topic).setVisible(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Search user and add to topic (If current user is creator of topic)
        if(TOPIC_MODE && m.getTopicDB().getTopicInfo(TOPIC_ID,topicDB._CREATOR).equals(m.getUsername())){
            menu.findItem(R.id.action_add_user).setVisible(true);
            SearchManager manager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
            searchItem = menu.findItem(R.id.action_add_user);
            MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    isUserSearching = true;
                    if(getActivity()!=null)(getActivity().findViewById(R.id.input_section)).setVisibility(View.GONE);
                    emptyview.setVisibility(View.GONE);
                    resimekle.hide();
                    sohbetList.clear();
                    userlist = new ArrayList<>();
                    sohbetRV.setAdapter(new UserAdapter(userlist));
                    swipeRV.setEnabled(false);
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    isUserSearching = false;
                    swipeRV.setEnabled(true);
                    if(getActivity()!=null)(getActivity().findViewById(R.id.input_section)).setVisibility(View.VISIBLE);
                    new initsohbet(20,0).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    return true;
                }
            });
            SearchView search = (SearchView) searchItem.getActionView();
            search.setSearchableInfo(manager.getSearchableInfo(getActivity().getComponentName()));
            search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    //Search user
                    if (newText.trim().length()>2) {
                        Volley.newRequestQueue(context).add(search_user(newText.trim()));
                    }
                    return true;
                }
            });
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(IS_PM)
            return super.onOptionsItemSelected(item);
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
            case R.id.action_leave_topic:
                Toast.makeText(context, R.string.toast_leaving_topic, Toast.LENGTH_SHORT).show();
                RequestQueue queue = Volley.newRequestQueue(context);
                queue.add(leavetopic);
                break;
            case R.id.action_close_topic:
                if(isAdded())
                    new AlertDialog.Builder(new ContextThemeWrapper(getActivity(),R.style.alertDialogTheme))
                            .setTitle(R.string.action_close_topic)
                            .setIcon(R.mipmap.ic_logout)
                            .setMessage(R.string.dialog_topic_close)
                            .setPositiveButton(R.string.action_close_topic, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    RequestQueue queue = Volley.newRequestQueue(context);
                                    Toast.makeText(context, R.string.toast_closing_topic, Toast.LENGTH_SHORT).show();
                                    queue.add(closetopic);
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            })
                            .setCancelable(true)
                            .show();
                break;
            case R.id.action_edit_topic:
                Fragment topic_edit = new topics_create();
                Bundle tbundle = new Bundle();
                tbundle.putString(topicDB._TOPICID, TOPIC_ID);
                topic_edit.setArguments(tbundle);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    topic_edit.setEnterTransition(new Slide(Gravity.TOP));
                getFragmentManager().beginTransaction()
                        .replace(R.id.Fcontent, topic_edit)
                        .addToBackStack(topicDB.TOPICS_TABLE + "edit" + TOPIC_ID)
                        .commit();
                break;
            case R.id.action_share_topic:
                shareTopic();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareTopic() {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("radyomenemen.com")
                .appendQueryParameter(topicDB._TOPICID, m.getTopicDB().getTopicSTR(TOPIC_ID));
        String topic_link = builder.build().toString();
        Intent share_intent = new Intent(Intent.ACTION_SEND);
        share_intent.setType("text/plain");
        share_intent.putExtra(android.content.Intent.EXTRA_TEXT, String.format(getString(R.string.topic_share_text), topic_link));
        startActivity(Intent.createChooser(share_intent,getString(R.string.topic_share_link)));
    }

    StringRequest leavetopic = new StringRequest(Request.Method.POST, RadyoMenemenPro.MENEMEN_TOPICS_LEAVE,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(TAG,"TOPIC response" + response);
                    switch (response) {
                        case "1":
                            if(context!=null)
                                Toast.makeText(context, R.string.toast_auth_error, Toast.LENGTH_SHORT).show();
                            break;
                        case "2":
                            try {
                                String topicstr = m.getTopicDB().getTopicSTR(TOPIC_ID);
                                if(topicstr!=null)
                                    FirebaseMessaging.getInstance().unsubscribeFromTopic(topicstr);
                                m.getTopicDB().leave(TOPIC_ID);
                                Toast.makeText(context, R.string.topic_leave_topic_success, Toast.LENGTH_SHORT).show();
                                getFragmentManager().beginTransaction().replace(R.id.Fcontent,new topics()).commit();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                }
            },null){
        @Override
        protected Map<String, String> getParams() throws AuthFailureError {
            Map<String,String> dataToSend = m.getAuthMap();
            dataToSend.put(topicDB._TOPICID,TOPIC_ID);
            return dataToSend;
        }

        @Override
        public RetryPolicy getRetryPolicy() {
            return new RetryPolicy() {
                @Override
                public int getCurrentTimeout() {
                    return 5000;
                }

                @Override
                public int getCurrentRetryCount() {
                    return 5000;
                }

                @Override
                public void retry(VolleyError error) throws VolleyError {
                    error.printStackTrace();
                }
            };
        }
    };

    StringRequest closetopic = new StringRequest(Request.Method.POST, RadyoMenemenPro.MENEMEN_TOPICS_CLOSE,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    switch (response) {
                        case "1":
                            if(context!=null)
                                Toast.makeText(context, R.string.toast_auth_error, Toast.LENGTH_SHORT).show();
                            break;
                        case "2":
                            try {
                                String topicstr = m.getTopicDB().getTopicSTR(TOPIC_ID);
                                if(topicstr!=null)
                                    FirebaseMessaging.getInstance().unsubscribeFromTopic(topicstr);
                                m.getTopicDB().closeTopic(TOPIC_ID);
                                Toast.makeText(context, R.string.toast_topic_close_success, Toast.LENGTH_LONG).show();
                                getFragmentManager().beginTransaction().replace(R.id.Fcontent,new topics()).commit();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                }
            },null){
        @Override
        protected Map<String, String> getParams() throws AuthFailureError {
            Map<String,String> dataToSend = m.getAuthMap();
            dataToSend.put(topicDB._TOPICID,TOPIC_ID);
            return dataToSend;
        }

        @Override
        public RetryPolicy getRetryPolicy() {
            return new RetryPolicy() {
                @Override
                public int getCurrentTimeout() {
                    return 5000;
                }

                @Override
                public int getCurrentRetryCount() {
                    return 5000;
                }

                @Override
                public void retry(VolleyError error) throws VolleyError {
                    error.printStackTrace();
                }
            };
        }
    };


    StringRequest jointopic = new StringRequest(Request.Method.POST, RadyoMenemenPro.MENEMEN_TOPICS_JOIN,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    switch (response) {
                        case "1":
                            if(context!=null)
                                Toast.makeText(context, R.string.toast_auth_error, Toast.LENGTH_SHORT).show();
                            break;
                        case "2":
                            m.getTopicDB().join(TOPIC_ID);
                            break;
                    }
                }
            },null){
        @Override
        protected Map<String, String> getParams() throws AuthFailureError {
            Map<String, String> dataToSend = m.getAuthMap();
            dataToSend.put(topicDB._TOPICID,TOPIC_ID);
            return dataToSend;
        }

        @Override
        public Priority getPriority() {
            return Priority.IMMEDIATE;
        }

        @Override
        public RetryPolicy getRetryPolicy() {
            return new RetryPolicy() {
                @Override
                public int getCurrentTimeout() {
                    return 5000;
                }

                @Override
                public int getCurrentRetryCount() {
                    return 5000;
                }

                @Override
                public void retry(VolleyError error) throws VolleyError {
                    error.printStackTrace();
                }
            };
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.smile_goster_button:
                smileRV.setVisibility(smileRV.getVisibility() != View.VISIBLE ? View.VISIBLE : View.GONE);
                break;
            case R.id.mesaj_gonder_button:
                if(ETmesaj.getText().toString().trim().length()>0)postToMenemen(ETmesaj.getText().toString());
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
            ETmesaj.setText("");
            if(sohbetList != null && sohbetRV != null){
                sohbetList.add(0,new Sohbet_Objects(null,m.getUsername(),msg, PENDING));
                if(sohbetRV.getAdapter() != null)
                    sohbetRV.getAdapter().notifyDataSetChanged();
            }
            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest post = new StringRequest(Request.Method.POST,
                    (TOPIC_MODE) ? RadyoMenemenPro.MENEMEN_TOPICS_POST : RadyoMenemenPro.MESAJ_GONDER,
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
                    if(TOPIC_MODE)
                        dataToSend.put(topicDB._TOPICID,TOPIC_ID);
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
                if(!IS_PM)
                    nick.setOnClickListener(this);
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
                }else if(view == nick){
                    Fragment userinfo = new user_pm();
                    Bundle bundle = new Bundle();
                    bundle.putString(RadyoMenemenPro.NICK,sohbetList.get(getAdapterPosition()).nick);
                    userinfo.setArguments(bundle);
                    getFragmentManager().beginTransaction().replace(R.id.Fcontent,userinfo).commit();
                }else {
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
            if(IS_PM){
                final int backgroundcolor = (chatViewHolder.nick.getText().toString().equals(m.getUsername())) ? ContextCompat.getColor(context,R.color.cardviewBG) : ContextCompat.getColor(context,R.color.colorBackgroundsoft);
                chatViewHolder.card.setCardBackgroundColor(backgroundcolor);
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
                   new CapsYukle(bitmap,context,(TOPIC_MODE) ? TOPIC_ID : null).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                Toast.makeText(context, R.string.caps_uploading, Toast.LENGTH_SHORT).show();
            }catch (Exception e){e.printStackTrace();}
        }else if(requestCode == RESULT_LOAD_IMAGE_CAM && resultCode!=0){ //resultCode 0: kameradan seçim iptal edildi
            try {
                final Uri saveduri = Uri.parse(m.oku(RadyoMenemenPro.LASTURI));
                Bitmap bitmap;
                InputStream image_stream = getActivity().getContentResolver().openInputStream(saveduri);
                bitmap= BitmapFactory.decodeStream(image_stream);
                new CapsYukle(bitmap,context,(TOPIC_MODE) ? TOPIC_ID : null).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
                ETmesaj.setText(ETmesaj.getText().toString() + " " + satbaxSmileList.get(getAdapterPosition()).smile);
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
    //Smiley END

    class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserVH>{
        Context context;
        ArrayList<String> userlist;
        class UserVH extends RecyclerView.ViewHolder implements View.OnClickListener{
            ImageButton add_user;
            TextView username;
            UserVH(View itemView) {
                super(itemView);
                add_user = (ImageButton) itemView.findViewById(R.id.add_user);
                username = (TextView) itemView.findViewById(R.id.username);
                add_user.setOnClickListener(this);
            }
            @Override
            public void onClick(View v) {
                ImageButton i = (ImageButton) v;
                Volley.newRequestQueue(context).add(add_user(userlist.get(getAdapterPosition()),i));
            }
        }
        UserAdapter(ArrayList<String> userlist){
            this.userlist = userlist;
        }

        @Override
        public UserVH onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.topic_add_user_item, viewGroup,false);
            UserVH pvh = new UserVH(v);
            context = viewGroup.getContext();
            return pvh;
        }
        @Override
        public int getItemCount() {
            return userlist.size();
        }

        @Override
        public void onBindViewHolder(UserVH user, final int i) {
            user.username.setText(userlist.get(i));
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }
    }

    StringRequest search_user(final String query) {
        StringRequest sr =  new StringRequest(Request.Method.POST, RadyoMenemenPro.SEARCH_USER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String response) {
                        Log.d("SEARCH",response);
                        if (response.equals("1")) {
                            Toast.makeText(context, R.string.toast_auth_error, Toast.LENGTH_SHORT).show();
                        } else {
                            new AsyncTask<Void,Void,Void>(){
                                @Override
                                protected Void doInBackground(Void... voids) {
                                    try {
                                        JSONArray arr = new JSONObject(response).getJSONArray("req");
                                        JSONObject c;
                                        if(arr.getJSONArray(0).length()>0) userlist.clear();
                                        for(int i = 0;i<arr.getJSONArray(0).length();i++) {
                                            JSONArray innerJarr = arr.getJSONArray(0);
                                            c = innerJarr.getJSONObject(i);
                                            userlist.add(c.getString("nick"));
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    return null;}

                                @Override
                                protected void onPostExecute(Void aVoid) {
                                    if(userlist.size()>0)
                                        sohbetRV.getAdapter().notifyDataSetChanged();
                                    super.onPostExecute(aVoid);
                                }
                            }.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                        }
                    }
                }, null) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> dataToSend = m.getAuthMap();
                dataToSend.put(SearchManager.QUERY, query);
                return dataToSend;
            }

        };
        sr.setShouldCache(true);
        return sr;
    }

    StringRequest add_user(final String user, final ImageButton ib){
        Toast.makeText(context, String.format(getString(R.string.topic_user_adding), user), Toast.LENGTH_SHORT).show();
        return new StringRequest(Request.Method.POST, RadyoMenemenPro.MENEMEN_TOPICS_ADD_USER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, response);
                        switch (response) {
                            case "1":
                                Toast.makeText(context, R.string.toast_auth_error, Toast.LENGTH_SHORT).show();
//                                if (ib != null)
                                    ib.setImageResource(R.drawable.ic_cancel_black_24dp);
                                break;
                            case "2":
                                //Duplicate
                                Toast.makeText(context, R.string.topics_member_duplicate, Toast.LENGTH_SHORT).show();
//                                if (ib != null)
                                    ib.setImageResource(R.drawable.ic_cancel_black_24dp);
                                break;
                            case "3":
                                //Success
                                Toast.makeText(context, R.string.topics_add_member_success, Toast.LENGTH_SHORT).show();
                                if (ib != null) ib.setImageResource(R.drawable.ic_done_black_24dp);
                                break;
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG,error.toString());
            }
        }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> dataToSend = m.getAuthMap();
                dataToSend.put(topicDB._TOPICID, TOPIC_ID);
                dataToSend.put("user", user);
                return dataToSend;
            }
        };
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
                                    String id,nick,mesaj,zaman;
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
        Cursor cursor;
        topicDB tsql;
        chatDB csql;

        initsohbet(int limit, int scroll) {
            this.limit = limit;
            this.scroll = scroll;
        }

        @Override
        protected void onPreExecute() {
            sohbetList = new ArrayList<>();
            if(TOPIC_MODE){
                tsql = m.getTopicDB();
                cursor = tsql.getHistory(limit,TOPIC_ID);
            }else {
                csql = m.getChatDB();
                cursor = csql.getHistory(limit);
            }
            try {
                if(cursor.getCount()>0 && TOPIC_MODE && emptyview != null)
                    emptyview.setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            //Getfrom db
            try {
                if(cursor == null) return null;
                cursor.moveToFirst();
                while(!cursor.isAfterLast()){
                    String id,nick,post,time;
                    id = cursor.getString(cursor.getColumnIndex((TOPIC_MODE) ? topicDB._TOPIC_MSG_ID : chatDB._MSGID));
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
            if(sohbetList != null && sohbetList.size()>1 && sohbetList.get(0).id != null)
                m.kaydet((TOPIC_MODE) ? RadyoMenemenPro.LAST_ID_SEEN_ON_TOPIC + TOPIC_ID : RadyoMenemenPro.LAST_ID_SEEN_ON_CHAT ,sohbetList.get(0).id);
            if(sohbetRV != null && scroll != 0) sohbetRV.scrollToPosition(scroll);
            try {
                if(TOPIC_MODE) tsql.close(); else csql.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
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
                if(cursor.getCount()<1) return null;
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
                    if(exist < 1)
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

    void startUpdatingUISilently() {
        mStatusChecker.run();
    }

    void stopUpdatingUI() {
        try {
            mHandler.removeCallbacks(mStatusChecker);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
