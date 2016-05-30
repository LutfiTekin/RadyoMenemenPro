package com.incitorrent.radyo.menemen.pro;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


import com.incitorrent.radyo.menemen.pro.fragments.login;
import com.incitorrent.radyo.menemen.pro.fragments.olan_biten;
import com.incitorrent.radyo.menemen.pro.fragments.podcast;
import com.incitorrent.radyo.menemen.pro.fragments.radio;
import com.incitorrent.radyo.menemen.pro.fragments.sohbet;
import com.incitorrent.radyo.menemen.pro.services.MUSIC_INFO_SERVICE;
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
        m= new Menemen(this);
        fragmentManager = getFragmentManager();
        //Music info servisini başlat
        startService(new Intent(MainActivity.this,MUSIC_INFO_SERVICE.class));
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
                FloatingActionButton b = (FloatingActionButton) view;
//                b.setImageResource(!m.oku("caliyor").equals("evet") ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
            if(m.oku("logged").equals("yok")) {
                navigationView.getMenu().findItem(R.id.nav_chat).setEnabled(false);//TODO giriş yapınca aktif
                navigationView.getMenu().findItem(R.id.nav_shout).setEnabled(false);
            }
        }

Log.v(TAG,"FRA"+ " "+ m.oku("logged"));
        if(savedInstanceState == null) {
            if (m.oku("logged").equals("yok")) {
                //TODO giriş fragmentini göster
                fragmentManager.beginTransaction().replace(R.id.Fcontent, new login()).commit();
            } else {
                fragmentManager.beginTransaction().replace(R.id.Fcontent, new radio()).commit();

            }
        }
        if (m.oku("logged").equals("evet") && navigationView != null) {
            navigationView.getMenu().findItem(R.id.nav_login).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_logout).setVisible(true);
        }


        new syncChannels(this).execute();
        askperms();
    }


    private void askperms() {
        //Android M için izinler
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) { //SDK versiyonu android M ise
            int hasWriteContactsPermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);

            if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED ) {
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.permissions))
                        .setMessage(getString(R.string.askperm))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @TargetApi(Build.VERSION_CODES.M)
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                        123);

                            }
                        })
                        .setIcon(R.mipmap.album_placeholder)
                        .show();

            }
        }
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

        if (id == R.id.nav_radio)
            fragmentManager.beginTransaction().replace(R.id.Fcontent, new radio()).commit();
        else if (id == R.id.nav_chat)
            fragmentManager.beginTransaction().addToBackStack("sohbet").replace(R.id.Fcontent,new sohbet()).commit();
        else if (id == R.id.nav_shout) {

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
            //TODO dialog ekranı göster
            m.kaydet("username","yok");
            m.kaydet("mkey","yok");
            m.kaydet("logged", "yok");
            Toast.makeText(MainActivity.this, R.string.toast_logged_out, Toast.LENGTH_SHORT).show();
            fragmentManager.beginTransaction().replace(R.id.Fcontent,new login()).commit();
           this.recreate();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }


}
