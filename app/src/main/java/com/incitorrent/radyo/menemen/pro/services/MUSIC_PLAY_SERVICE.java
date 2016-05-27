package com.incitorrent.radyo.menemen.pro.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;


import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.incitorrent.radyo.menemen.pro.MainActivity;
import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;
import com.incitorrent.radyo.menemen.pro.utils.NotificationControls;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class MUSIC_PLAY_SERVICE extends Service {
    private static final String TAG = "MUSIC_PLAY_SERVICE";
    Menemen m;
    AudioManager audioManager;
    int amgr_result;
    AudioManager.OnAudioFocusChangeListener focusChangeListener;
    MediaPlayer mediaPlayer;
    IntentFilter filter;
    MediaSessionCompat mediaSessionCompat;
    MediaMetadataCompat.Builder mdBuilder;
    PlaybackStateCompat.Builder stateBuilder;
    ScheduledThreadPoolExecutor exec;
    NotificationCompat.Builder notification;
    NotificationManager nm;
    public MUSIC_PLAY_SERVICE() {
    }

    @Override
    public void onCreate() {

        exec = new ScheduledThreadPoolExecutor(1);
        mdBuilder = new MediaMetadataCompat.Builder();
        mediaSessionCompat = new MediaSessionCompat(this,TAG);
        m = new Menemen(this);
        filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        mediaPlayer = new MediaPlayer();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        focusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_LOSS:
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        pause();
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN:
                        resume();
                        break;
                }
            }
        };
        amgr_result = audioManager.requestAudioFocus(focusChangeListener,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
        mediaSessionCompat.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
                Log.v("TAG","MediaButtonEvent " + mediaButtonEvent.getAction());
                return super.onMediaButtonEvent(mediaButtonEvent);
            }

            @Override
            public void onPlay() {
                resume();
                Log.v(TAG,"MediaSession callback onPlay");
                super.onPlay();
            }

            @Override
            public void onPause() {
                pause();
                Log.v(TAG,"MediaSession callback onPause");
                super.onPause();
            }

            @Override
            public void onStop() {
                pause();
                Log.v(TAG,"MediaSession callback onStop");
                super.onStop();
            }
        });
        mediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        stateBuilder = new PlaybackStateCompat.Builder();
        stateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PAUSE);


        //Çalan şarkıyı kontrol et
        exec.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                setMusicMeta();
                nowPlayingNotification();
                Log.v(TAG, "COMPLETED TASK COUNT " + exec.getCompletedTaskCount());
            }
        },1,RadyoMenemenPro.MUSIC_SERVICE_INFO_INTERVAL/2, TimeUnit.SECONDS);

        super.onCreate();
    }

    private void pause() {
        try {
            mediaPlayer.pause();
            m.kaydet("caliyor","hayır");
            nowPlayingNotification();
            stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,1.0f);
            mediaSessionCompat.setPlaybackState(stateBuilder.build());
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void resume() {
        try {
            mediaPlayer.start();
            m.kaydet("caliyor","evet");
            nowPlayingNotification();
            mediaSessionCompat.setActive(true);
            stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,1.0f);
            mediaSessionCompat.setPlaybackState(stateBuilder.build());
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }catch (Exception e){
            Log.v(TAG, "ERROR  " + e.getMessage());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String dataSource =null;
       if(intent.getExtras()!=null)dataSource = intent.getExtras().getString("dataSource");
        if(dataSource!=null && dataSource.equals("stop")) {
            stopService(new Intent(this, MUSIC_PLAY_SERVICE.class)); //DURDUR
            return START_NOT_STICKY;
        }
        if(!m.oku("caliyor").equals("evet")){
           if(dataSource!=null)  play(dataSource);
            else resume();
            Log.v(TAG,"DATA SOURCE " +dataSource);
        }else if(m.oku("caliyor").equals("evet")) pause();
        return START_NOT_STICKY;
    }

    private void setMusicMeta() {
        Boolean showartwork = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("download_artwork",true);
        Bitmap defaultbitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_header_background);
        mdBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE,m.oku("calan"));
        mdBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST,m.oku("dj"));
       if(showartwork) mdBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART,m.getMenemenArt(m.oku("LASTsongid"),true));
           else mdBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART,defaultbitmap);
        mediaSessionCompat.setMetadata(mdBuilder.build());
    }

    private void play(final String dataSource) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    mediaPlayer.setDataSource(dataSource);
                    mediaPlayer.prepare();
                    registerReceiver(PlugReceiver,filter);
                    setMusicMeta();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e){
                    //İllegal durum
                    e.printStackTrace();
                }catch (NullPointerException e){
                    //Houston we have a problem
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                try {
                    mediaPlayer.start();
                    m.kaydet("caliyor","evet");
                    mediaSessionCompat.setActive(true);
                    stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,1.0f);
                    mediaSessionCompat.setPlaybackState(stateBuilder.build());
                    nowPlayingNotification();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
                super.onPostExecute(aVoid);
            }
        }.execute();

    }

    @Override
    public void onDestroy() {
        Log.v(TAG,"onDestroy");
        audioManager.abandonAudioFocus(focusChangeListener);
        try {
            unregisterReceiver(PlugReceiver);
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaSessionCompat.release();
            exec.shutdown();
            nm.cancel(RadyoMenemenPro.NOW_PLAYING_NOTIFICATION);
        } catch (Exception e) {
            e.printStackTrace();
        }
        m.kaydet("caliyor","hayır");
        super.onDestroy();
    }

    void nowPlayingNotification(){
        //Şarkı albüm kapağı
        if(m.oku("calan").equals("yok")) return; //ilk açılışta boş bildirim atma
        Boolean showartwork = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("download_artwork",true);
        String contentTitle = (m.oku("dj").equals(RadyoMenemenPro.OTO_DJ)) ? "Radyo Menemen" : m.oku("dj");
        notification = new NotificationCompat.Builder(this);
        notification
        .setContentTitle(contentTitle)
        .setContentText(m.oku("calan"))
        .setSmallIcon(R.mipmap.ic_equlizer)
        .setLargeIcon(m.getMenemenArt(m.oku("LASTsongid"),false))
        .setOngoing(m.oku("caliyor").equals("evet"))
        .setContentIntent(PendingIntent.getActivity(this, new Random().nextInt(200), new Intent(this, MainActivity.class), PendingIntent.FLAG_CANCEL_CURRENT))
        .setStyle(new android.support.v7.app.NotificationCompat.MediaStyle().setMediaSession(mediaSessionCompat.getSessionToken()));
        Intent playpause = new Intent(this,NotificationControls.class);
        Intent stop = new Intent(this,NotificationControls.class);
        stop.putExtra("stop",true);
        PendingIntent ppIntent = PendingIntent.getBroadcast(this, new Random().nextInt(102), playpause, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent stopIntent = PendingIntent.getBroadcast(this, new Random().nextInt(102), stop, PendingIntent.FLAG_CANCEL_CURRENT);
       if(m.oku("caliyor").equals("evet")) notification.addAction(android.R.drawable.ic_media_pause,getString(R.string.media_pause),ppIntent);
        else notification.addAction(android.R.drawable.ic_media_play,getString(R.string.media_resume),ppIntent);
        notification.addAction(R.mipmap.ic_media_stop,getString(R.string.media_stop),stopIntent);
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
     nm.notify(RadyoMenemenPro.NOW_PLAYING_NOTIFICATION, notification.build());
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    private BroadcastReceiver PlugReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG,intent.getAction());
        stopSelf(); //kulaklık çıkınca durdur
        }
    };



}
