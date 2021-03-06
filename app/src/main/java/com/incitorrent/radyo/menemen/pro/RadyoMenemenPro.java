package com.incitorrent.radyo.menemen.pro;

/**
 * Radyo Menemen Pro Created by lutfi on 19.05.2016.
 */
public final class RadyoMenemenPro {

    public final static String LOW_CHANNEL = "Hercules";//64 kbps yayın
    public final static String MID_CHANNEL = "Prometheus";//128 kbps yayın
    public final static String HIGH_CHANNEL = "Zeus"; //320 kbps yayın
    public final static String FALLBACK_CHANNEL = "http://stream.incitorrent.com:443/live"; //320 kbps yayın
    public final static String ARTWORK_ONLINE = "online_art";
    public final static int MUSIC_INFO_SERVICE_INTERVAL = 240;
    public final static int MENEMEN_TIMEOUT = 30000;
    public final static int NOW_PLAYING_NOTIFICATION = 20032016; //Notification idsi bi özelliği yok rastgele
    public final static int ON_AIR_NOTIFICATION = 28052016;
    public final static int PODCAST_NOTIFICATION = 19072016;
    public final static int CAPS_NOTIFICATION = 2122016;
    public final static int MSG_DOWNLOAD_PROGRESS_NOTIFICATION = 27082016;
    public final static int SONG_CHANGED_BY_USER_NOTIFICATION = 1112016;

    public final static int GALLERY_IMAGE_OVERRIDE_WITDH = 600;
    public final static int GALLERY_IMAGE_OVERRIDE_HEIGHT = 600;
    public final static int ARTWORK_IMAGE_OVERRIDE_DIM = 300;

    public final static String PODCASTFEED = "http://menemen.incitorrent.com/podcast/feed.xml"; //Ana podcast adresi
    public final static String PODCASTLINK = "http://menemen.incitorrent.com/podcast/media/"; //Podcast linkleri için prefix
    public final static String OLD_PODCASTFEED = "http://podcast.incitorrent.com/feed.xml"; //Eski podcast adresi
    public final static String OLD_PODCASTLINK = "http://podcast.incitorrent.com/media/";

    //Api URL Constants
    private final static String APP_INFO = "http://menemen.incitorrent.com/inc/appinfo.php?"; //base url
    private final static String API = "http://menemen.incitorrent.com/api/?"; //Yeni API base urls
    public final static String AUTH = "http://menemen.incitorrent.com/inc/auth.php"; //yetkilendirme adresi
    public final static String SYNCCHANNEL = "https://app.menemen.incitorrent.com/channels.html";
    public final static String MESAJLAR =  APP_INFO + "mesajlar";
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
    public final static String MP_LIST_TRANSACTIONS = API + "list_mp_transactions";
    public final static String CONTACT = API + "contact";
    public static final String SEARCH_TRACK  = API + "search_track";
    public static final String REQUEST_TRACK  = API + "request_track";
    private final static String MENEMEN_TOPICS = API + "topics";
    public final static String MENEMEN_TOPICS_CREATE = MENEMEN_TOPICS + "&action=create";
    public final static String MENEMEN_TOPICS_DELETE = MENEMEN_TOPICS + "&action=delete";
    public final static String MENEMEN_TOPICS_JOIN = MENEMEN_TOPICS + "&action=join";
    public final static String MENEMEN_TOPICS_LEAVE = MENEMEN_TOPICS + "&action=leave";
    public final static String MENEMEN_TOPICS_CLOSE = MENEMEN_TOPICS + "&action=close";
    public final static String MENEMEN_TOPICS_EDIT = MENEMEN_TOPICS + "&action=edit";
    public final static String MENEMEN_TOPICS_LIST = MENEMEN_TOPICS + "&action=list";
    public final static String MENEMEN_TOPICS_POST = MENEMEN_TOPICS + "&action=post";
    public final static String MENEMEN_TOPICS_ADD_USER = MENEMEN_TOPICS + "&action=adduser";
    public final static String SEARCH_USER = API + "&finduser";
    public final static String SEARCH_USER_AVATAR = API + "findavatar";
    public final static String VALID_VERSION = "https://app.menemen.incitorrent.com/validversion.html";

    //SHARED PREFERENCES CONSTANTS
    public final static String SHAREDPREF = "Menemen";
    public final static String HAYKIRCACHE = "hkcache";
    public final static String PODCASTCACHE = "podcache";
    public final static String SAVEDOB = "kayitliolabiten";
    public final static String LASTOB = "sonolabiten";
    public final static String LASTURI = "lasturi";
    public final static String SAVED_MUSIC_INFO = "savedcalanonservice";
    public final static String PLAYING_PODCAST = "playingpodcast";
    public static final String IS_PODCAST = "ispodcastplayingnow";
    public static final String FIRST_TIME = "f_t";
    public static final String IS_CHAT_FOREGROUND = "icf";
    public static final String IS_RADIO_FOREGROUND = "irf";
    public static final String LAST_ID_SEEN_ON_CHAT = "lastseenid"; //Bildirim gruplaması için gerekli
    public static final String LAST_ID_SEEN_ON_CAPS = "lastseenidOnCaps_";
    public static final String LAST_ID_SEEN_ON_TOPIC = "lastseenidOnTopic_";
    public static final String MUTE_NOTIFICATION = "mute_notif";
    public static final String DEFAULT_RADIO_WIDGET = "radio_widget_def";
    public static final String SQUARE_RADIO_WIDGET = "radio_widget_sqr";
    public static final String LISTENERS_COUNT  = "listenersC";


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



    public final static class FCMTopics{
        final static String TOPIC = "/topics/";
        public final static String NEWS = TOPIC + "news";
        public final static String SYNC = TOPIC + "sync";
        public final static String ONAIR = TOPIC + "onair";
        public final static String PODCAST = TOPIC + "podcast";
        public final static String SONG_CHANGE_EVENT = TOPIC + "songchange";
        public final static String NEW_PUBLIC_TOPIC = TOPIC + "newtopic";
    }

    public final static class Action{
        public final static String RADIO =  "radyo.menemen.play";
        public final static String CHAT =  "radyo.menemen.chat";
        public final static String PODCAST =  "radyo.menemen.podcast";
        public final static String PODCAST_PLAY =  "radyo.menemen.podcast.play";
        public final static String OLAN_BITEN =  "radyo.menemen.news";
        public final static String TRACK_INFO_LAST =  "radyo.menemen.track.info.last";
        public final static String WIDGET_PLAY =  "radyo.menemen.widget.play";
        public final static String WIDGET_STOP =  "radyo.menemen.widget.stop";
        public final static String CAPS =  "radyo.menemen.caps";
        public final static String TOPICS =  "radyo.menemen.topics";
        public final static String TOPIC_MESSAGES =  "radyo.menemen.topic.messages";
        public final static String PRIVATE_MESSAGE =  "radyo.menemen.pm";
        public final static String MP_TRANSACTIONS = "radyo.menemen.mp.transactions";
        public final static String PLAY_NOW = "radyo.menemen.play.now";
    }

    public final static String OTO_DJ = "Oto Dj";
    public final static String PLAY = "play";
    public final static String DATA_SOURCE = "dataSource";

    public final static String CAPS_API_URL    =   "https://caps.incitorrent.com/api/1/upload";
    public final static String CAPS_API_KEY    =   "capsapi";
    public final static String CAPS_IMAGES_PATH  =   "https://caps.incitorrent.com/images/";

    public static final String CHAT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String NICK = "nick";
    public static final String MOBIL_KEY = "mkey";

    public static final String PM = "pm";
}
