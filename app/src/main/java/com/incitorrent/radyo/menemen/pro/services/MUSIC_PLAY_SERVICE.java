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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.graphics.Palette;
import android.util.Log;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.firebase.messaging.FirebaseMessaging;
import com.incitorrent.radyo.menemen.pro.MainActivity;
import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;
import com.incitorrent.radyo.menemen.pro.utils.NotificationControls;
import com.incitorrent.radyo.menemen.pro.utils.TriggerSongChange;

import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.incitorrent.radyo.menemen.pro.RadyoMenemenPro.broadcastinfo.CALAN;
import static com.incitorrent.radyo.menemen.pro.RadyoMenemenPro.broadcastinfo.DJ;
import static com.incitorrent.radyo.menemen.pro.RadyoMenemenPro.broadcastinfo.PODCAST_DESCR;


public class MUSIC_PLAY_SERVICE extends Service {
    private static final String TAG = "MUSIC_PLAY_SERVICE";
    final public static String MUSIC_PLAY_FILTER = "com.incitorrent.radyo.menemen.UICHANGE";
    final public static String PODCAST_PLAY_FILTER = "com.incitorrent.radyo.menemen.podcast.UICHANGE";
    final public static String PODCAST_SEEK_FILTER = "com.incitorrent.radyo.menemen.podcast.seek";
    final public static String PODCAST_GET_DURATION = "getpodcastduration";
    final public static String PODCAST_SEEKBAR_BUFFERING_UPDATE = "podcastseekbarupdate";
    final public static String PODCAST_TERMINATE = "podcastterminate";
    BroadcastReceiver receiver;

    Menemen m;
    AudioManager audioManager;
    int amgr_result;
    AudioManager.OnAudioFocusChangeListener focusChangeListener;
    ExoPlayer exoPlayer;
    ExoPlayer.EventListener exolistener;
    IntentFilter filter;
    MediaSessionCompat mediaSessionCompat;
    MediaMetadataCompat.Builder mdBuilder;
    PlaybackStateCompat.Builder stateBuilder;
    ScheduledThreadPoolExecutor exec;
    NotificationCompat.Builder notification;
    NotificationManager nm;
    LocalBroadcastManager broadcasterForUi;
    Boolean isPodcast = false; //Çalan şey radyo mu podcast mi
    public MUSIC_PLAY_SERVICE() {
    }

    @Override
    public void onCreate() {
        broadcasterForUi = LocalBroadcastManager.getInstance(this);
        exec = new ScheduledThreadPoolExecutor(1);
        mdBuilder = new MediaMetadataCompat.Builder();
        mediaSessionCompat = new MediaSessionCompat(this,TAG);
        m = new Menemen(this);
        filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        // 1. Create a default TrackSelector
        Handler handler = new Handler();
        TrackSelector trackSelector = new DefaultTrackSelector(handler);
        LoadControl loadControl = new DefaultLoadControl();
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        focusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_LOSS:
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        pause(false);
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN:
                        resume(false);
                        break;
                }
            }
        };
        amgr_result = audioManager.requestAudioFocus(focusChangeListener,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
        mediaSessionCompat.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
                return super.onMediaButtonEvent(mediaButtonEvent);
            }

            @Override
            public void onPlay() {
                resume(true);
                super.onPlay();
            }

            @Override
            public void onPause() {
                pause(true);
                super.onPause();
            }

            @Override
            public void onStop() {
                pause(true);
                super.onStop();
            }

            @Override
            public void onSkipToNext() {
                sendBroadcast(new Intent(MUSIC_PLAY_SERVICE.this, TriggerSongChange.class));
                super.onSkipToNext();
            }

            @Override
            public void onFastForward() {
                try {
                    exoPlayer.seekTo(exoPlayer.getCurrentPosition() + 5000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                super.onFastForward();
            }

            @Override
            public void onRewind() {
                try {
                    exoPlayer.seekTo(exoPlayer.getCurrentPosition() - 5000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                super.onRewind();
            }
        });
        mediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        stateBuilder = new PlaybackStateCompat.Builder();
        stateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_FAST_FORWARD | PlaybackStateCompat.ACTION_REWIND);
        //Exoplayerlistener
        exolistener = new ExoPlayer.EventListener() {
            @Override
            public void onLoadingChanged(boolean isLoading) {
                Log.d(TAG,"isLoading " + isLoading);
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if(playbackState == ExoPlayer.STATE_READY){
                    broadcastPodcastDuration();
                }else if(playbackState == ExoPlayer.STATE_ENDED){
                    terminatePodcast();
                }
                if(isPodcast)
                    updateProgress();
            }

            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Log.e(TAG, "onPlayerError " + error.toString());
            }

            @Override
            public void onPositionDiscontinuity() {
                Log.d(TAG, "onPositionDiscontinuity");
            }
        };

        //Çalan şarkıyı kontrol et
        exec.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
               updateMediaInfoIfNecessary();
            }
        },1,RadyoMenemenPro.MUSIC_INFO_SERVICE_INTERVAL /2, TimeUnit.SECONDS);
        MusicPlayServiceReceiver();
        registerReceiver();
        super.onCreate();
    }

    private void updateMediaInfoIfNecessary() {
        if(m.isPlaying() && !isPodcast) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        setMusicMeta();
                        nowPlayingNotification();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        }
    }


    private void pause(final Boolean pausedbyUser) {
    new AsyncTask<Void,Void,Void>(){
        @Override
        protected void onPreExecute() {
            if(pausedbyUser) //abandonfocus if paused by user
                audioManager.abandonAudioFocus(focusChangeListener);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                m.kaydet("caliyor","hayır");
                exoPlayer.setPlayWhenReady(false);
                nowPlayingNotification();
                stopForeground(false);
                stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,1.0f);
                mediaSessionCompat.setPlaybackState(stateBuilder.build());
            } catch (Exception e){ e.printStackTrace(); }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            broadcastToUi(false); //Call this on Ui Thread
            super.onPostExecute(aVoid);
        }
    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void resume(final Boolean resumedbyUser) {
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected void onPreExecute() {
                if(resumedbyUser) { //regain focus if resumed by user
                    int res =  audioManager.requestAudioFocus(focusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                    if(res == AudioManager.AUDIOFOCUS_REQUEST_FAILED) return;
                }
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    exoPlayer.setPlayWhenReady(true);
                    m.kaydet("caliyor","evet");
                    nowPlayingNotification();
                    startForeground(RadyoMenemenPro.NOW_PLAYING_NOTIFICATION,notification.build());
                    mediaSessionCompat.setActive(true);
                    stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,1.0f);
                    mediaSessionCompat.setPlaybackState(stateBuilder.build());
                } catch (Exception e){
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                broadcastToUi(true); //Call this on Ui Thread
                super.onPostExecute(aVoid);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String dataSource = null;
        isPodcast = m.oku(RadyoMenemenPro.IS_PODCAST).equals("evet");
       if(intent.getExtras()!=null)dataSource = intent.getExtras().getString("dataSource");
        if(dataSource!=null && dataSource.equals("stop")) {
            stopService(new Intent(this, MUSIC_PLAY_SERVICE.class)); //DURDUR
            return START_NOT_STICKY;
        }
        if(!m.isPlaying()){
            //MUSIC_INFO_SERVICE başlat
            if(!isPodcast)
                startService(new Intent(MUSIC_PLAY_SERVICE.this, MUSIC_INFO_SERVICE.class));
            else {
                try {
                    if(intent.hasExtra("descr"))
                        m.kaydet(PODCAST_DESCR,intent.getExtras().getString("descr"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
           if(dataSource!=null)  play(dataSource);
            else resume(true);
        }else if(m.isPlaying()) pause(true);

        return START_NOT_STICKY;
    }

    private void setMusicMeta() {
        String title,artist;
        if (!isPodcast) {
            //Normal radyo çalıyor
            Boolean showartwork = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("download_artwork",true);
            Bitmap defaultbitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_header_background);
            title = m.oku(CALAN);
            artist = m.oku(DJ);
            if(showartwork) mdBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART,m.getMenemenArt(m.oku(MUSIC_INFO_SERVICE.LAST_ARTWORK_URL),true));
                else mdBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART,defaultbitmap);
        } else {
            // çalan şey podcast
            title = m.oku(RadyoMenemenPro.PLAYING_PODCAST);
            artist = getString(R.string.app_name) + " Podcast";
            mdBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART,BitmapFactory.decodeResource(getResources(),R.mipmap.locksreen_podcast_art));
        }
        mdBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE,Menemen.fromHtmlCompat(title));
        mdBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST,artist);
        mediaSessionCompat.setMetadata(mdBuilder.build());
    }

    private void play(final String dataSource) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPostExecute(Void aVoid) {
                broadcastToUi(true);
                super.onPostExecute(aVoid);
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory("Menemen");
                    ExtractorsFactory extractor = new DefaultExtractorsFactory();
                    MediaSource audioSource = new ExtractorMediaSource(Uri.parse(dataSource), dataSourceFactory, extractor, null, null);
                    exoPlayer.prepare(audioSource);
                    registerReceiver(PlugReceiver,filter);
                    setMusicMeta();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //ONPOST
                try {
                    exoPlayer.setPlayWhenReady(true);
                    exoPlayer.addListener(exolistener);
                    m.kaydet("caliyor","evet");
                    mediaSessionCompat.setActive(true);
                    stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,1.0f);
                    mediaSessionCompat.setPlaybackState(stateBuilder.build());
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                nowPlayingNotification();
                                startForeground(RadyoMenemenPro.NOW_PLAYING_NOTIFICATION,notification.build());
                                FirebaseMessaging.getInstance().subscribeToTopic("songchange");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    audioManager.requestAudioFocus(focusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                } catch (Exception e){
                    e.printStackTrace();
                }
                //ONPOST
                return null;
            }

        }.execute();

    }




    @Override
    public void onDestroy() {
        m.kaydet("caliyor","hayır");
        audioManager.abandonAudioFocus(focusChangeListener);
        try {
            stopForeground(true);
            exoPlayer.stop();
            exoPlayer.removeListener(exolistener);
            exoPlayer.release();
            if(isPodcast) terminatePodcast();
            mediaSessionCompat.release();
            exec.shutdown();
            broadcastToUi(false);
            LocalBroadcastManager.getInstance(MUSIC_PLAY_SERVICE.this).unregisterReceiver(receiver);
            FirebaseMessaging.getInstance().unsubscribeFromTopic("songchange");
            stopService(new Intent(MUSIC_PLAY_SERVICE.this,MUSIC_INFO_SERVICE.class));
            unregisterReceiver(PlugReceiver);
        } catch (Exception e) {
           e.printStackTrace();
        }

        super.onDestroy();
    }



    void nowPlayingNotification(){
        //Şarkı albüm kapağı
        if(m.oku(CALAN).equals("yok")) return; //ilk açılışta boş bildirim atma
        String contentTitle = (m.oku("dj").equals(RadyoMenemenPro.OTO_DJ)) ? "Radyo Menemen" : m.oku("dj");
        String calan = m.oku(CALAN);
        Intent intent = new Intent(this, MainActivity.class);
        if(isPodcast){
            contentTitle = getString(R.string.app_name) + " Podcast";
            calan = m.oku(RadyoMenemenPro.PLAYING_PODCAST);
            intent.setAction(RadyoMenemenPro.Action.PODCAST_PLAY);
            intent.putExtra("title", calan)
                    .putExtra("descr", m.oku(PODCAST_DESCR))
                    .putExtra("duration", exoPlayer.getDuration())
                    .putExtra("current", exoPlayer.getCurrentPosition());
        }else intent.setAction(RadyoMenemenPro.Action.RADIO);
        Bitmap artwork = m.getMenemenArt(m.oku(MUSIC_INFO_SERVICE.LAST_ARTWORK_URL),false);
        int notificationcolor = ContextCompat.getColor(MUSIC_PLAY_SERVICE.this,R.color.colorBackgroundsofter);
        try {
            Palette palette = Palette.from(artwork).generate();
            notificationcolor = palette.getVibrantColor(notificationcolor);
        } catch (Exception e) {
            e.printStackTrace();
        }
        notification = new NotificationCompat.Builder(this);
        notification
        .setContentTitle(contentTitle)
        .setContentText(Menemen.fromHtmlCompat(calan))
        .setColor(notificationcolor)
        .setSmallIcon((isPodcast) ? R.drawable.podcast : R.mipmap.ic_equalizer)
        .setLargeIcon((isPodcast) ? BitmapFactory.decodeResource(this.getResources(), R.drawable.podcast) : artwork)
        .setContentIntent(PendingIntent.getActivity(this, new Random().nextInt(200), intent, PendingIntent.FLAG_UPDATE_CURRENT))
        .setStyle(new android.support.v7.app.NotificationCompat.MediaStyle().setMediaSession(mediaSessionCompat.getSessionToken()));
        Intent playpause = new Intent(this,NotificationControls.class);
        Intent stop = new Intent(this,NotificationControls.class);
        stop.putExtra("stop",true);
        PendingIntent ppIntent = PendingIntent.getBroadcast(this, new Random().nextInt(102), playpause, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent stopIntent = PendingIntent.getBroadcast(this, new Random().nextInt(102), stop, PendingIntent.FLAG_CANCEL_CURRENT);
        if(m.isPlaying()) notification.addAction(R.drawable.ic_pause_black_24dp,getString(R.string.media_pause),ppIntent);
        else notification.addAction(R.drawable.ic_play_arrow_black_24dp,getString(R.string.media_resume),ppIntent);
        if(!isPodcast && m.oku(DJ).equals(RadyoMenemenPro.OTO_DJ)){
            Intent triggersongchange = new Intent(this, TriggerSongChange.class);
            PendingIntent skipIntent = PendingIntent.getBroadcast(this, new Random().nextInt(102),triggersongchange, PendingIntent.FLAG_CANCEL_CURRENT);
            notification.addAction(R.drawable.ic_skip_next_black_24dp, getString(R.string.skip), skipIntent);
        }
        notification.addAction(R.drawable.ic_stop_black_24dp,getString(R.string.media_stop),stopIntent);
        notification.setDeleteIntent(stopIntent);
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(RadyoMenemenPro.NOW_PLAYING_NOTIFICATION, notification.build());
    }
    //Activity de şimdi çalıyor butonunu değiştir
    public void broadcastToUi(Boolean play) {
        Intent intent = new Intent(MUSIC_PLAY_FILTER);
        intent.putExtra("action",RadyoMenemenPro.PLAY);
        intent.putExtra(RadyoMenemenPro.PLAY,play);
        broadcasterForUi.sendBroadcast(intent);
        m.updateRadioWidget();
        m.updateSqrRadioWidget();
    }

    //Podcast Şimdi Çalıyor Fragmentine Bilgileri Gönder
    public void broadcastPodcastDuration(){
        if(!isPodcast) return;
        Intent intent = new Intent(PODCAST_PLAY_FILTER);
        long duration = exoPlayer.getDuration();
        long currentpos = exoPlayer.getCurrentPosition();
        intent.putExtra("action",PODCAST_GET_DURATION);
        intent.putExtra("duration",duration);
        intent.putExtra("current", currentpos);
        broadcasterForUi.sendBroadcast(intent);
    }

    private void broadcastPodcastBuffering(int buffer, int current) {
        if(!isPodcast) return;
        Intent intent = new Intent(PODCAST_PLAY_FILTER);
        intent.putExtra("action",PODCAST_SEEKBAR_BUFFERING_UPDATE);
        intent.putExtra("buffer",buffer);
        intent.putExtra("current", current);
        broadcasterForUi.sendBroadcast(intent);
    }

    private void terminatePodcast() {
        if(!isPodcast) return;
        Intent intent = new Intent(PODCAST_PLAY_FILTER);
        intent.putExtra("action",PODCAST_TERMINATE);
        broadcasterForUi.sendBroadcast(intent);
    }



    private void MusicPlayServiceReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent!=null) {
                    String action = intent.getExtras().getString("action");
                    if(action == null) return;
                    if (action.equals("seek")){
                        int msec = intent.getExtras().getInt("seek");
                        try {
                            exoPlayer.seekTo(msec);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }else if(action.equals("update")){
                        updateMediaInfoIfNecessary();
                    }
                }
            }
        };
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(PODCAST_SEEK_FILTER);
        filter.addAction(MUSIC_INFO_SERVICE.NP_FILTER);
        LocalBroadcastManager.getInstance(MUSIC_PLAY_SERVICE.this).registerReceiver((receiver), filter);
    }
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    private BroadcastReceiver PlugReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            pause(true);
        }
    };
    final Handler handler = new Handler();
    private void updateProgress() {
        final long position = exoPlayer == null ? 0 : exoPlayer.getCurrentPosition();
        final long bufferedPosition = exoPlayer == null ? 0 : exoPlayer.getBufferedPosition();
        broadcastPodcastBuffering((int)bufferedPosition,(int)position);
        // Remove scheduled updates.
        handler.removeCallbacks(updateProgressAction);
        // Schedule an update if necessary.
        int playbackState = exoPlayer == null ? ExoPlayer.STATE_IDLE : exoPlayer.getPlaybackState();
        if (playbackState != ExoPlayer.STATE_IDLE && playbackState != ExoPlayer.STATE_ENDED) {
            long delayMs;
            if (exoPlayer.getPlayWhenReady() && playbackState == ExoPlayer.STATE_READY) {
                delayMs = 1000 - (position % 1000);
                if (delayMs < 200) {
                    delayMs += 1000;
                }
            } else {
                delayMs = 1000;
            }
            if(m.isPlaying())
                handler.postDelayed(updateProgressAction, delayMs);
        }
    }

    private final Runnable updateProgressAction = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };

}
