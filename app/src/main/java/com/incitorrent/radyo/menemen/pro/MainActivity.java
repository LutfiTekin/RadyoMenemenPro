package com.incitorrent.radyo.menemen.pro;

import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.incitorrent.radyo.menemen.pro.fragments.haykir;
import com.incitorrent.radyo.menemen.pro.fragments.login;
import com.incitorrent.radyo.menemen.pro.fragments.olan_biten;
import com.incitorrent.radyo.menemen.pro.fragments.podcast;
import com.incitorrent.radyo.menemen.pro.fragments.radio;
import com.incitorrent.radyo.menemen.pro.fragments.sohbet;
import com.incitorrent.radyo.menemen.pro.services.MUSIC_PLAY_SERVICE;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;
import com.incitorrent.radyo.menemen.pro.utils.syncChannels;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,login.OnFragmentInteractionListener,radio.OnFragmentInteractionListener,podcast.OnFragmentInteractionListener {
    public static String TAG = "RMPRO";
    FloatingActionButton fab;
    private Menemen m;
    Fragment fragment;
    FragmentManager fragmentManager;
    BroadcastReceiver receiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //Menemen yardımcı sınıfı
        m = new Menemen(this);
        //İlk açılışta intro göster
        if(m.isFirstTime("intro")) startActivity(new Intent(this, Intro.class));

        fragmentManager = getFragmentManager();

        //Radyo durumunu buton ile senkronize tut
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Boolean play;
              if(intent!=null) {
                play = intent.getBooleanExtra(RadyoMenemenPro.PLAY, true);
                fab.setImageResource((play) ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
              }
            }
        };

        fab = (FloatingActionButton) findViewById(R.id.fab);
        if(fab!=null) fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!m.isInternetAvailable()){
                    Toast.makeText(MainActivity.this, R.string.toast_internet_warn, Toast.LENGTH_SHORT).show();
                    return;
                }
                //Podcast çalmıyor
                m.kaydet(RadyoMenemenPro.IS_PODCAST,"hayır");
                 Intent  radyoservis = new Intent(MainActivity.this, MUSIC_PLAY_SERVICE.class);
                //Ayarlardan seçilmiş kanalı bul
                String selected_channel = m.oku(PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("radio_channel",RadyoMenemenPro.HIGH_CHANNEL));
                String dataSource = "http://" + m.oku(RadyoMenemenPro.RADIO_SERVER) + ":" + selected_channel +  "/";
                //Oluşturulan servis intentine datasource ekle
                radyoservis.putExtra("dataSource",dataSource);
                //data source ile servisi başlat
                startService(radyoservis);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
       if(drawer!=null) drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
            if(m.oku("logged").equals("yok")) {
                navigationView.getMenu().findItem(R.id.nav_chat).setEnabled(false);//TODO giriş yapınca aktif
                navigationView.getMenu().findItem(R.id.nav_shout).setEnabled(false);
            }

            final TextView badge =(TextView) MenuItemCompat.getActionView(navigationView.getMenu().
                    findItem(R.id.nav_olanbiten));
           if(!m.oku(RadyoMenemenPro.LASTOB).equals(m.oku(RadyoMenemenPro.SAVEDOB)))
            m.setBadge(badge,getString(R.string.fresh));
        }

Log.v(TAG,"FRA"+ " "+ m.oku("logged"));
        if(savedInstanceState == null) {
            if(m.oku("logged").equals("yok") && PreferenceManager.getDefaultSharedPreferences(this).getBoolean("music_only",false)) {
            //Sadece müzik modu açık ve giriş yapılmamış
                fragmentManager.beginTransaction().replace(R.id.Fcontent, new radio()).commit();
                //sohbet haykır giriş çıkış butonlarını gizle
                if (navigationView != null){
                    navigationView.getMenu().findItem(R.id.nav_chat).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_login).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_shout).setVisible(false);
                }
            }else if (m.oku("logged").equals("yok")) {
                fragmentManager.beginTransaction().replace(R.id.Fcontent, new login()).commit();
            } else {
               if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("onstart_chat",true))
                   fragmentManager.beginTransaction().replace(R.id.Fcontent, new sohbet()).commit();
                else
                fragmentManager.beginTransaction().replace(R.id.Fcontent, new radio()).commit();
            }
        }
        //Yeni mesaj Bildirimnden gelenleri chate gönder
       if(getIntent().getAction()!=null){
           Log.v("ACTION",getIntent().getAction());
           if(getIntent().getAction().equals("radyo.menemen.chat")) fragmentManager.beginTransaction().replace(R.id.Fcontent, new sohbet()).commit();
       }


        if (m.oku("logged").equals("evet") && navigationView != null) {
            navigationView.getMenu().findItem(R.id.nav_login).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_logout).setVisible(true);
        }
        new syncChannels(this).execute();
    }




    @Override
    protected void onStart() {
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(MUSIC_PLAY_SERVICE.MUSIC_PLAY_FILTER)
        );
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
    protected void onResume() {
        fab.setImageResource(m.oku("caliyor").equals("evet") ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
        super.onResume();
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
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if(id== R.id.nav_login){
            fragmentManager.beginTransaction().replace(R.id.Fcontent, new login()).commit();
        }else if (id == R.id.nav_radio)
            fragmentManager.beginTransaction().replace(R.id.Fcontent, new radio()).commit();
        else if (id == R.id.nav_chat)
            fragmentManager.beginTransaction().addToBackStack("sohbet").replace(R.id.Fcontent,new sohbet()).commit();
        else if (id == R.id.nav_shout) {
            fragmentManager.beginTransaction().addToBackStack("haykir").replace(R.id.Fcontent,new haykir()).commit();
        }else if(id == R.id.nav_olanbiten){
            fragmentManager.beginTransaction().addToBackStack("ob").replace(R.id.Fcontent,new olan_biten()).commit();
        }else if (id == R.id.nav_podcast) {
            fragmentManager.beginTransaction().addToBackStack("podcast").replace(R.id.Fcontent,new podcast()).commit();
        } else if (id == R.id.nav_share) {
            String googleplaylink = "https://play.google.com/store/apps/details?id=com.incitorrent.radyo.menemen.pro";
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    getString(R.string.app_share_text) + googleplaylink);
            shareIntent.setType("text/plain");
            startActivity(shareIntent);

        } else if (id == R.id.nav_about) {
        startActivity(new Intent(this,About.class));
        } else if(id == R.id.nav_logout){

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
                            m.kaydet("logged", "yok");
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

    @Override
    public void onFragmentInteraction(Uri uri) {

    }


}
