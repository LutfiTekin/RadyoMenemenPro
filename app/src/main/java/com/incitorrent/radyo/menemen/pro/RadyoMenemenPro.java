package com.incitorrent.radyo.menemen.pro;

/**
 * Radyo Menemen Pro Created by lutfi on 19.05.2016.
 */
public class RadyoMenemenPro {

    public final static String LOW_CHANNEL = "Hercules";//64 kbps yayın
    public final static String MID_CHANNEL = "Pegasus";//128 kbps yayın
    public final static String HIGH_CHANNEL = "Zeus"; //320 kbps yayın
    public final static String RADIO_SERVER = "serverR";
    public final static int MUSIC_SERVICE_INFO_INTERVAL = 40;
    public final static int NOW_PLAYING_NOTIFICATION = 20032016; //Notification idsi bi özelliği yok rastgele
    public final static int ON_AIR_NOTIFICATION = 28052016; //Notification idsi bi özelliği yok rastgele


    public static String HAYKIRURL    =   "http://radyo.incitorrent.com/haykir.php";
    public static String BROADCASTINFO    =   "http://radyomenemen.com/inc/assets/broadcastinfo.php";
    public static String PODCASTFEED = "http://radyomenemen.com/podcast/feed.xml"; //Ana podcast adresi
    public static String PODCASTLINK = "http://radyomenemen.com/podcast/media/"; //Podcast linkleri için prefix

    public final static String AUTH = "http://radyomenemen.com/inc/auth.php"; //yetkilendirme adresi
    public final static String SYNCCHANNEL = "http://radyomenemen.com/inc/appinfo.php?channels";
    public final static String DJRESPONSE = "http://radyomenemen.com/inc/appinfo.php?djresponse";
    public final static String MESAJLAR = "http://radyomenemen.com/inc/appinfo.php?mesajlar";
    public final static String OLAN_BITEN = "http://radyomenemen.com/inc/appinfo.php?olanbiten";
    public final static String MESAJ_GONDER = "http://radyomenemen.com/inc/appinfo.php?postmessage";

    public final static String SHAREDPREF = "Menemen";
    public final static String SOHBETCACHE = "sohbetcache";
    public final static String OBCACHE = "olanbitencache";
    public final static String SAVED_MUSIC_INFO = "savedcalanonservice";
    public final static String SAVED_DJ = "saveddj";
    public final static String PLAYING_PODCAST = "playingpodcast";
    public static final String IS_PODCAST = "podcastmicaliyor";

    public final static String OTO_DJ = "Oto Dj";
    public final static String PLAY = "play";

    public final static String CAPS_API_URL    =   "http://caps.incitorrent.com/api/1/upload";
    public final static String CAPS_API_KEY    =   "capsapi";


}
