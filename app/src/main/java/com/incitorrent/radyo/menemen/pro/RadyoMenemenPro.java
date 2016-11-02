package com.incitorrent.radyo.menemen.pro;

/**
 * Radyo Menemen Pro Created by lutfi on 19.05.2016.
 */
public final class RadyoMenemenPro {

    public final static String LOW_CHANNEL = "Hercules";//64 kbps yayın
    public final static String MID_CHANNEL = "Pegasus";//128 kbps yayın
    public final static String HIGH_CHANNEL = "Zeus"; //320 kbps yayın
    public final static String RADIO_SERVER = "serverR";
    public final static int MUSIC_INFO_SERVICE_INTERVAL = 240;
    public final static int NOW_PLAYING_NOTIFICATION = 20032016; //Notification idsi bi özelliği yok rastgele
    public final static int ON_AIR_NOTIFICATION = 28052016;
    public final static int PODCAST_NOTIFICATION = 19072016;
    public final static int MSG_DOWNLOAD_PROGRESS_NOTIFICATION = 27082016;
    public final static int SONG_CHANGED_BY_USER_NOTIFICATION = 1112016;

    public final static int GALLERY_IMAGE_OVERRIDE_WITDH = 600;
    public final static int GALLERY_IMAGE_OVERRIDE_HEIGHT = 600;

    public final static int ARTWORK_IMAGE_OVERRIDE_DIM = 300;



    public final static String BROADCASTINFO    =   "http://radyomenemen.com/inc/assets/broadcastinfo_pro.php";
    public final static String PODCASTFEED = "http://radyomenemen.com/podcast/feed.xml"; //Ana podcast adresi
    public final static String PODCASTLINK = "http://radyomenemen.com/podcast/media/"; //Podcast linkleri için prefix
    public final static String OLD_PODCASTFEED = "http://podcast.incitorrent.com/feed.xml"; //Eski podcast adresi
    public final static String OLD_PODCASTLINK = "http://podcast.incitorrent.com/media/";
    public final static String HAYKIR_LINK = "http://radyomenemen.com/haykir.php";


    public final static String AUTH = "http://radyomenemen.com/inc/auth.php"; //yetkilendirme adresi
    private final static String APP_INFO = "http://radyomenemen.com/inc/appinfo.php?"; //base url
    private final static String API = "http://api.radyomenemen.com/?"; //Yeni API base urls
    public final static String SYNCCHANNEL = APP_INFO + "channels";
    public final static String DJRESPONSE = APP_INFO + "djresponse";
    public final static String MESAJLAR =  APP_INFO + "mesajlar";
    public final static String HAYKIRMALAR = APP_INFO + "haykirmalarim";
    public final static String OLAN_BITEN = APP_INFO + "olanbiten";
    public final static String MESAJ_GONDER = APP_INFO + "postmessage";
    public final static String MESAJ_SIL = APP_INFO + "deletemessage";
    public final static String TOKEN_ADD = APP_INFO + "fcm_tokenadd";
    public final static String REGISTER_CAPS = APP_INFO + "registercaps";
    public final static String POST_COMMENT_CAPS = APP_INFO + "postcaps_comment";
    public final static String DELETE_COMMENT_CAPS = APP_INFO + "deletecaps_comment";
    public final static String GET_COMMENT_CAPS = APP_INFO + "get_caps_comments";
    public final static String PUSH_ONLINE_SIGNAL = APP_INFO + "pushonline";
    public final static String BROADCASTINFO_NEW = API + "broadcastinfo";
    public final static String MP_ADD = API + "mp_add";
    public final static String MP_CHANGE_SONG = API + "mp_change_song";

    //SHARED PREFERENCES CONSTANTS
    public final static String SHAREDPREF = "Menemen";
    public final static String HAYKIRCACHE = "hkcache";
    public final static String OBCACHE = "olanbitencache";
    public final static String SAVEDOB = "kayitliolabiten";
    public final static String LASTOB = "sonolabiten";
    public final static String LASTURI = "lasturi";
    public final static String SAVED_MUSIC_INFO = "savedcalanonservice";
    public final static String PLAYING_PODCAST = "playingpodcast";
    public static final String IS_PODCAST = "ispodcastplayingnow";
    public static final String FIRST_TIME = "f_t";
    public static final String IS_CHAT_FOREGROUND = "icf";
    public static final String LAST_ID_SEEN_ON_CHAT = "lastseenid"; //Bildirim gruplaması için gerekli
    public static final String LAST_ID_SEEN_ON_CAPS = "lastseenidOnCaps_";
    public static final String MUTE_NOTIFICATION = "mute_notif";
    public static final String DEFAULT_RADIO_WIDGET = "radio_widget_def";
    public static final String SQUARE_RADIO_WIDGET = "radio_widget_sqr";


    public final static class broadcastinfo{
        public final static String DJ = "dj";
        public final static String CALAN = "calan";
        public final static String ARTWORK = "artwork";
        public final static String PODCAST_DESCR = "podcastD";
        public final static String PODCAST_URL = "podcastU";
    }

    public final static class transitionname {
        public final static String ART = "artwork";
        public final static String CALAN = "calan";
        public final static String PODCASTCARD = "podcastcard";
    }

    public final static class AnalyticEvents{
        public final static String LISTENING_RADIO = "listening radio";
        public final static String LISTENING_PODCAST = "listening podcast";
        public final static String DOWNLOADING_PODCAST = "downloading podcast";
        public final static String SEARCH_ON_Y = "searching track on youtube";
        public final static String SEARCH_ON_S = "searching track on spotify";
        public final static String SEARCH_L = "searching track lyrics";
    }

    public final static class FCMTopics{
        final static String TOPIC = "/topics/";
        public final static String NEWS = TOPIC + "news";
        public final static String SYNC = TOPIC + "sync";
        public final static String ONAIR = TOPIC + "onair";
        public final static String PODCAST = TOPIC + "podcast";
        public final static String SONG_CHANGE_EVENT = TOPIC + "songchange";
    }

    public final static class Action{
        public final static String RADIO =  "radyo.menemen.play";
        public final static String CHAT =  "radyo.menemen.chat";
        public final static String PODCAST =  "radyo.menemen.podcast";
        public final static String PODCAST_PLAY =  "radyo.menemen.podcast.play";
        public final static String OLAN_BITEN =  "radyo.menemen.news";
        public final static String HAYKIR =  "radyo.menemen.haykir";
        public final static String TRACK_INFO_LAST =  "radyo.menemen.track.info.last";
        public final static String WIDGET_PLAY =  "radyo.menemen.widget.play";
        public final static String WIDGET_STOP =  "radyo.menemen.widget.stop";
        public final static String CAPS =  "radyo.menemen.caps";
    }

    public final static String OTO_DJ = "Oto Dj";
    public final static String PLAY = "play";

    public final static String CAPS_API_URL    =   "http://caps.radyomenemen.com/api/1/upload";
    public final static String CAPS_API_KEY    =   "capsapi";

    public static final String CHAT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";


}
