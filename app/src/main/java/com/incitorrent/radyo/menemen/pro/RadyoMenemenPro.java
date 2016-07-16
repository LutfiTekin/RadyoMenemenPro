package com.incitorrent.radyo.menemen.pro;

/**
 * Radyo Menemen Pro Created by lutfi on 19.05.2016.
 */
public final class RadyoMenemenPro {

    public final static String LOW_CHANNEL = "Hercules";//64 kbps yayın
    public final static String MID_CHANNEL = "Pegasus";//128 kbps yayın
    public final static String HIGH_CHANNEL = "Zeus"; //320 kbps yayın
    public final static String RADIO_SERVER = "serverR";
    public final static int MUSIC_SERVICE_INFO_INTERVAL = 40;
    public final static int NOW_PLAYING_NOTIFICATION = 20032016; //Notification idsi bi özelliği yok rastgele
    public final static int ON_AIR_NOTIFICATION = 28052016; //Notification idsi bi özelliği yok rastgele



    public final static String BROADCASTINFO    =   "http://radyomenemen.com/inc/assets/broadcastinfo_pro.php";
    public final static String PODCASTFEED = "http://radyomenemen.com/podcast/feed.xml"; //Ana podcast adresi
    public final static String PODCASTLINK = "http://radyomenemen.com/podcast/media/"; //Podcast linkleri için prefix
    public final static String OLD_PODCASTFEED = "http://podcast.incitorrent.com/feed.xml"; //Eski podcast adresi
    public final static String OLD_PODCASTLINK = "http://podcast.incitorrent.com/media/";
    public final static String HAYKIR_LINK = "http://radyomenemen.com/haykir.php";


    public final static String AUTH = "http://radyomenemen.com/inc/auth.php"; //yetkilendirme adresi
    public final static String SYNCCHANNEL = "http://radyomenemen.com/inc/appinfo.php?channels";
    public final static String DJRESPONSE = "http://radyomenemen.com/inc/appinfo.php?djresponse";
    public final static String MESAJLAR = "http://radyomenemen.com/inc/appinfo.php?mesajlar";
    public final static String HAYKIRMALAR = "http://radyomenemen.com/inc/appinfo.php?haykirmalarim";
    public final static String OLAN_BITEN = "http://radyomenemen.com/inc/appinfo.php?olanbiten";
    public final static String MESAJ_GONDER = "http://radyomenemen.com/inc/appinfo.php?postmessage";
    public final static String MESAJ_SIL = "http://radyomenemen.com/inc/appinfo.php?deletemessage";

    //SHARED PREFERENCES CONSTANTS
    public final static String SHAREDPREF = "Menemen";
    public final static String SOHBETCACHE = "sohbetcache";
    public final static String HAYKIRCACHE = "hkcache";
    public final static String OBCACHE = "olanbitencache";
    public final static String SAVEDOB = "kayitliolabiten";
    public final static String LASTOB = "sonolabiten";
    public final static String SAVED_MUSIC_INFO = "savedcalanonservice";
    public final static String SAVED_DJ = "saveddj";
    public final static String PLAYING_PODCAST = "playingpodcast";
    public static final String IS_PODCAST = "podcastmicaliyor";
    public static final String FIRST_TIME = "f_t";
    public static final String IS_CHAT_FOREGROUND = "icf";

    public final static class broadcastinfo{
        public final static String DJ = "dj";
        public final static String CALAN = "calan";
        public final static String ARTWORK = "artwork";
    }

    public final static class transitionname {
        public final static String ART = "artwork";
        public final static String CALAN = "calan";
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
        public final static String TOPIC = "/topics/";
        public final static String GENERAL = TOPIC + "general";
        public final static String NEWS = TOPIC + "news";
    }

    public final static String OTO_DJ = "Oto Dj";
    public final static String PLAY = "play";

    public final static String CAPS_API_URL    =   "http://caps.radyomenemen.com/api/1/upload";
    public final static String CAPS_API_KEY    =   "capsapi";


}
