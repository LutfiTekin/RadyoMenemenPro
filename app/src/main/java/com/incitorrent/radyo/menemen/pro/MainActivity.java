package com.incitorrent.radyo.menemen.pro;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.iid.FirebaseInstanceId;
import com.incitorrent.radyo.menemen.pro.fragments.galeri;
import com.incitorrent.radyo.menemen.pro.fragments.haykir;
import com.incitorrent.radyo.menemen.pro.fragments.login;
import com.incitorrent.radyo.menemen.pro.fragments.olan_biten;
import com.incitorrent.radyo.menemen.pro.fragments.podcast;
import com.incitorrent.radyo.menemen.pro.fragments.podcast_now_playing;
import com.incitorrent.radyo.menemen.pro.fragments.radio;
import com.incitorrent.radyo.menemen.pro.fragments.sohbet;
import com.incitorrent.radyo.menemen.pro.fragments.topics;
import com.incitorrent.radyo.menemen.pro.fragments.track_info;
import com.incitorrent.radyo.menemen.pro.services.FIREBASE_CM_SERVICE;
import com.incitorrent.radyo.menemen.pro.services.MUSIC_INFO_SERVICE;
import com.incitorrent.radyo.menemen.pro.services.MUSIC_PLAY_SERVICE;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;
import com.incitorrent.radyo.menemen.pro.utils.syncChannels;
import com.incitorrent.radyo.menemen.pro.utils.trackonlineusersDB;

import static com.incitorrent.radyo.menemen.pro.RadyoMenemenPro.broadcastinfo.CALAN;
import static com.incitorrent.radyo.menemen.pro.RadyoMenemenPro.broadcastinfo.DJ;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static String TAG = "RMPRO";
    private Menemen m;
    FragmentManager fragmentManager;
    BroadcastReceiver receiver;
    NavigationView navigationView;
    View hview;
    TextView header_txt,header_sub_txt;
    ImageView header_img;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        setTheme(R.style.AppTheme_NoActionBar);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //Menemen yardımcı sınıfı
        m = new Menemen(getApplicationContext());
        //İlk açılışta intro göster
        if(m.isFirstTime("intro")) startActivity(new Intent(this, Intro.class));
        fragmentManager = getFragmentManager();
        //Radyo durumunu buton ile senkronize tut
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Boolean play;
              if(intent!=null) {
                  String action = intent.getAction();
                  if (action.equals(MUSIC_INFO_SERVICE.NP_FILTER) || action.equals(MUSIC_PLAY_SERVICE.MUSIC_PLAY_FILTER)) {
                  play = intent.getBooleanExtra(RadyoMenemenPro.PLAY, true);
                      setNP(m.isPlaying() || play);
              }else if(action.equals(FIREBASE_CM_SERVICE.USERS_ONLINE_BROADCAST_FILTER)){
                      int count = intent.getExtras().getInt("count",0);
                      if(count > 0){
                          final TextView badge =(TextView) MenuItemCompat.getActionView(navigationView.getMenu().
                                  findItem(R.id.nav_chat));
                          m.setBadge(badge,String.valueOf(count));
                      }
                  }
              }
            }
        };
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
       if(drawer!=null) drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        hview = navigationView.getHeaderView(0);
        header_img = (ImageView) hview.findViewById(R.id.header_img);
        header_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(m.isPlaying() && !m.bool_oku(RadyoMenemenPro.IS_PODCAST))
                    fragmentManager.beginTransaction().replace(R.id.Fcontent, new radio()).commit();
                if(drawer == null) return;
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                }
            }
        });
        header_txt = (TextView) hview.findViewById(R.id.header_txt);
        header_sub_txt = (TextView) hview.findViewById(R.id.header_sub_txt);
        header_sub_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!m.isPlaying() || m.bool_oku(RadyoMenemenPro.IS_PODCAST))
                    return;
                Fragment track_info = new track_info();
                Bundle bundle = new Bundle();
                bundle.putString("trackname", Menemen.fromHtmlCompat(m.oku(CALAN)));
                bundle.putString("arturl",m.oku(MUSIC_INFO_SERVICE.LAST_ARTWORK_URL));
                track_info.setArguments(bundle);
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.Fcontent, track_info)
                        .addToBackStack("track_info")
                        .commit();
            }
        });
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
            final TextView badge =(TextView) MenuItemCompat.getActionView(navigationView.getMenu().
                    findItem(R.id.nav_olanbiten));
           if(!m.oku(RadyoMenemenPro.LASTOB).equals(m.oku(RadyoMenemenPro.SAVEDOB))) {
               m.setBadge(badge, getString(R.string.fresh)); //Olan bitende yeni ibaresini göster
               m.kaydet(RadyoMenemenPro.SAVEDOB,m.oku(RadyoMenemenPro.LASTOB));//Yeni gelen veriyi kaydet
           }
        }
        if(savedInstanceState == null) {
            defaultAction();
        }
         goToFragmentByIntentAction(getIntent());
        if (navigationView != null) {
            if (m.isLoggedIn()) {
                navigationView.getMenu().findItem(R.id.nav_login).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_logout).setVisible(true);
            } else {
                navigationView.getMenu().findItem(R.id.nav_chat).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_login).setVisible(true);
                navigationView.getMenu().findItem(R.id.nav_shout).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_logout).setVisible(false);
            }
        }
      if(m.isFirstTime("channelsync"))  new syncChannels(this).execute();
    }



    private void setNP(boolean isPlaying){
        if(isPlaying){
            if(!m.bool_oku(RadyoMenemenPro.IS_PODCAST)) {
                //Podcast çalmıyorsa sol headerı düzenle
                String title = Menemen.fromHtmlCompat(m.oku(CALAN));
                if (header_txt != null) header_txt.setText(m.oku(DJ));
                if (header_sub_txt != null) header_sub_txt.setText(title);
                if(PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getBoolean("download_artwork",true))
                    Glide.with(MainActivity.this)
                        .load(m.oku(MUSIC_INFO_SERVICE.LAST_ARTWORK_URL))
                        .dontAnimate()
                        .override(RadyoMenemenPro.ARTWORK_IMAGE_OVERRIDE_DIM, RadyoMenemenPro.ARTWORK_IMAGE_OVERRIDE_DIM)
                        .into(header_img);
            }
        }else{
            header_img.setImageDrawable(null);
            if(m.isLoggedIn())
                header_txt.setText(m.getUsername().toUpperCase());
            else header_txt.setText(getString(R.string.app_name));
            header_sub_txt.setText(R.string.site_adress);
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        goToFragmentByIntentAction(intent);

    }

    /**
     * Bildirimden gelen aksiyonları doğru fragmente yolla
     * @param intent
     */
    private void goToFragmentByIntentAction(Intent intent) {
        final String main = "android.intent.action.MAIN";
        if(intent == null || intent.getAction() == null) {
            defaultAction();
            return;
        }
        if(!intent.getAction().equals(main)) {
            try {
                switch (intent.getAction()) {
                    case RadyoMenemenPro.Action.RADIO:
                        fragmentManager.beginTransaction()
                                .replace(R.id.Fcontent, new radio()).commit();
                        break;
                    case RadyoMenemenPro.Action.CHAT:
                        fragmentManager.beginTransaction()
                                .replace(R.id.Fcontent, new sohbet()).commit();
                        break;
                    case RadyoMenemenPro.Action.PODCAST:
                        fragmentManager.beginTransaction()
                                .replace(R.id.Fcontent, new podcast()).commit();
                        break;
                    case RadyoMenemenPro.Action.PODCAST_PLAY:
                        podcastPlay(intent);
                        break;
                    case RadyoMenemenPro.Action.OLAN_BITEN:
                        fragmentManager.beginTransaction()
                                .replace(R.id.Fcontent, new olan_biten()).commit();
                        break;
                    case RadyoMenemenPro.Action.HAYKIR:
                        fragmentManager.beginTransaction()
                                .replace(R.id.Fcontent, new haykir()).commit();
                        break;
                    case RadyoMenemenPro.Action.TRACK_INFO_LAST:
                        fragmentManager.beginTransaction()
                                .replace(R.id.Fcontent, new radio()).commit();
                        Fragment track_info = new track_info();
                        Bundle bundle = new Bundle();
                        bundle.putString("trackname", Menemen.fromHtmlCompat(m.oku(CALAN)));
                        bundle.putString("arturl",m.oku(MUSIC_INFO_SERVICE.LAST_ARTWORK_URL));
                        track_info.setArguments(bundle);
                        fragmentManager
                                .beginTransaction()
                                .replace(R.id.Fcontent, track_info)
                                .addToBackStack("track_info")
                                .commit();
                        break;
                    default:
                        defaultAction();
                        break;
                }
            } catch (Exception e) {
                Log.v("ACTION", e.toString());
            }
        }
    }

    private void podcastPlay(Intent intent) {
        fragmentManager.beginTransaction()
                .replace(R.id.Fcontent, new podcast()).addToBackStack("podcast").commit();
        Fragment podcast_now_playing = new podcast_now_playing();
        Bundle bundle = new Bundle();
        bundle.putString("title", intent.getExtras().getString("title"));
        bundle.putString("descr", intent.getExtras().getString("descr"));
        bundle.putLong("duration", intent.getExtras().getLong("duration"));
        bundle.putLong("current", intent.getExtras().getLong("current"));
        podcast_now_playing.setArguments(bundle);
        fragmentManager.beginTransaction()
                .replace(R.id.Fcontent, podcast_now_playing).addToBackStack("podcast_play").commit();
    }

    private void defaultAction() {
        if(!m.isLoggedIn() && PreferenceManager.getDefaultSharedPreferences(this).getBoolean("music_only",false)) {
            //Sadece müzik modu açık ve giriş yapılmamış
            fragmentManager.beginTransaction().replace(R.id.Fcontent, new radio()).commit();
        }else if (!m.isLoggedIn()) {
            fragmentManager.beginTransaction().replace(R.id.Fcontent, new login()).commit();
        } else {
            if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("onstart_chat",true))
                fragmentManager.beginTransaction().replace(R.id.Fcontent, new sohbet()).commit();
            else
                fragmentManager.beginTransaction().replace(R.id.Fcontent, new radio()).commit();
        }
    }

    @Override
    protected void onStart() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(MUSIC_PLAY_SERVICE.MUSIC_PLAY_FILTER);
        filter.addAction(MUSIC_INFO_SERVICE.NP_FILTER);
        filter.addAction(FIREBASE_CM_SERVICE.USERS_ONLINE_BROADCAST_FILTER);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver((receiver),filter);
        super.onStart();
    }


    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(drawer==null) return;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        if(!m.isServiceRunning(MUSIC_PLAY_SERVICE.class)) m.setPlaying(false);
        setNP(m.isPlaying());
        initOnlineUsersCountBadge();
        super.onResume();
    }

    private void initOnlineUsersCountBadge() {
        trackonlineusersDB sql = new trackonlineusersDB(MainActivity.this,null,null,1);
        if(sql.getOnlineUserCount() > 0) {
            final TextView badge = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().
                    findItem(R.id.nav_chat));
            m.setBadge(badge, String.valueOf(sql.getOnlineUserCount()));
        }
        sql.close();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
           startActivity(new Intent(this,Ayarlar.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if(id== R.id.nav_login){
            fragmentManager.beginTransaction().replace(R.id.Fcontent, new login()).commit();
        }else if (id == R.id.nav_radio)
            fragmentManager.beginTransaction().replace(R.id.Fcontent, new radio()).commit();
        else if (id == R.id.nav_chat)
            fragmentManager.beginTransaction().addToBackStack("sohbet").replace(R.id.Fcontent,new sohbet()).commit();
        else if (id == R.id.nav_topic)
            fragmentManager.beginTransaction().addToBackStack("topics").replace(R.id.Fcontent, new topics()).commit();
        else if (id == R.id.nav_galeri)
            fragmentManager.beginTransaction().addToBackStack("galeri").replace(R.id.Fcontent, new galeri()).commit();
        else if (id == R.id.nav_shout) {
            fragmentManager.beginTransaction().addToBackStack("haykir").replace(R.id.Fcontent,new haykir()).commit();
        }else if(id == R.id.nav_olanbiten){
            fragmentManager.beginTransaction().addToBackStack("ob").replace(R.id.Fcontent,new olan_biten()).commit();
        }else if (id == R.id.nav_podcast) {
            fragmentManager.beginTransaction().addToBackStack("podcast").replace(R.id.Fcontent,new podcast()).commit();
        }  else if(id == R.id.nav_logout){

            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.dialog_logout_title))
                    .setMessage(getString(R.string.dialog_logout_descr))
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //REFRESH FCM TOKEN
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        FirebaseInstanceId.getInstance().deleteInstanceId();
                                        FirebaseInstanceId.getInstance().getToken();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                            //DELETE username and key
                            m.kaydet("username","yok");
                            m.kaydet("mkey","yok");
                            m.bool_kaydet("loggedin",false);
                            m.kaydet(RadyoMenemenPro.HAYKIRCACHE,"yok");
                            Toast.makeText(MainActivity.this, R.string.toast_logged_out, Toast.LENGTH_SHORT).show();
                            //REOPEN Activity
                            fragmentManager.beginTransaction().replace(R.id.Fcontent,new login()).commit();
                            MainActivity.this.recreate();
                        }
                    }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            })
            .setIcon(R.mipmap.ic_launcher)
            .show();

        } else if(id == R.id.nav_settings){
            startActivity(new Intent(this,Ayarlar.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
       if(drawer!=null) drawer.closeDrawer(GravityCompat.START);
        return true;
    }




}
