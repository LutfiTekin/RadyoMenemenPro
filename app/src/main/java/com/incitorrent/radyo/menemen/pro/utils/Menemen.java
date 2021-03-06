package com.incitorrent.radyo.menemen.pro.utils;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.DownloadManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AlertDialog;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RadioWidget;
import com.incitorrent.radyo.menemen.pro.RadioWidgetSqr;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;
import com.incitorrent.radyo.menemen.pro.services.FIREBASE_CM_SERVICE;
import com.incitorrent.radyo.menemen.pro.services.MUSIC_INFO_SERVICE;
import com.incitorrent.radyo.menemen.pro.show_image;
import com.incitorrent.radyo.menemen.pro.show_image_comments;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import static com.incitorrent.radyo.menemen.pro.RadyoMenemenPro.broadcastinfo.CALAN;

/**
 * Created by lutfi on 20.05.2016.
 */
public class Menemen {
    Context context;
    public static final String TAG = "MenemenHelperClass";

    public Menemen(Context context) {
        this.context = context;
    }
    //Küçük bilgileri hızlıca kaydetmek için sharedpref metodu
    public void kaydet(String title,String content){
        final SharedPreferences kaydet = context.getApplicationContext().getSharedPreferences(RadyoMenemenPro.SHAREDPREF, Context.MODE_PRIVATE);
        kaydet.edit().putString(title,content).apply();
    }
    //Kaydedilen bilgileri okumak için sharedpref metodu
    public String oku(String title) throws NullPointerException{
        final SharedPreferences oku = context.getApplicationContext().getSharedPreferences(RadyoMenemenPro.SHAREDPREF, Context.MODE_PRIVATE);
        return oku.getString(title, null); //Değer boş ise null
    }


    public void bool_kaydet(String title,Boolean content){
        final SharedPreferences kaydet = context.getApplicationContext().getSharedPreferences(RadyoMenemenPro.SHAREDPREF, Context.MODE_PRIVATE);
        kaydet.edit().putBoolean(title,content).apply();
    }

    public boolean bool_oku(String title){
        return context.getApplicationContext().getSharedPreferences(RadyoMenemenPro.SHAREDPREF, Context.MODE_PRIVATE).getBoolean(title, false); //Değer boş ise false
    }

    //Uygulama içinde sadece ilk defa yapılacak şeyler için örn: uygulama introsu
    /**
     * Returns true for spesific action only once
     * @param action String key for action
     * @return boolean true if not called before, false afterwards
     */
    public boolean isFirstTime(String action){
        final SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(RadyoMenemenPro.FIRST_TIME, Context.MODE_PRIVATE);
        if(sharedPreferences.getBoolean(action,true)){
            sharedPreferences.edit().putBoolean(action,false).apply();
            return true;
        }
        return false;
    }

    /**
     * Resets first time action to true
     * @param action
     */
    void resetFirstTime(String action){
        final SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(RadyoMenemenPro.FIRST_TIME, Context.MODE_PRIVATE);
    sharedPreferences.edit().putBoolean(action,true).apply();
    }



    //İnternet var mı?
    /**
     * Internet check
     * @return true if is device connected
     */
    public boolean isInternetAvailable(){
        try {
            ConnectivityManager nInfo = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            nInfo.getActiveNetworkInfo().isConnectedOrConnecting();
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnectedOrConnecting()) return true;
            else {
                Log.e(TAG, "Network yok");
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     *
     * @return true if device connected to wifi
     */

    public boolean isConnectedWifi(){
        try {
            ConnectivityManager nInfo = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            nInfo.getActiveNetworkInfo().isConnectedOrConnecting();
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return (netInfo!=null && netInfo.isConnected() && netInfo.getType() == ConnectivityManager.TYPE_WIFI);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if the connection is fast
     * @return
     */
    public boolean isConnectionFast(){
        if(!isInternetAvailable()) return false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        int type = netInfo.getType();
        int subType = netInfo.getSubtype();
        if(type==ConnectivityManager.TYPE_WIFI){
            return true;
        }else if(type==ConnectivityManager.TYPE_MOBILE){
            switch(subType){
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    return false; // ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    return true; // ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    return true; // ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return false; // ~ 100 kbps
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    return true; // ~ 2-14 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    return true; // ~ 700-1700 kbps
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    return true; // ~ 1-23 Mbps
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    return true; // ~ 400-7000 kbps
            /*
             * Above API level 7, make sure to set android:targetSdkVersion
             * to appropriate level to use these
             */
                case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
                    return true; // ~ 1-2 Mbps
                case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
                    return true; // ~ 5 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
                    return true; // ~ 10-20 Mbps
                case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
                    return false; // ~25 kbps
                case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
                    return true; // ~ 10+ Mbps
                // Unknown
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                default:
                    return false;
            }
        }else{
            return false;
        }
    }

    public static String getEncodedData(Map<String,String> data) {
        StringBuilder sb = new StringBuilder();
        for(String key : data.keySet()) {
            String value = null;
            try {
                value = URLEncoder.encode(data.get(key), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            if(sb.length()>0)
                sb.append("&");

            sb.append(key + "=" + value);
        }
        return sb.toString();
    }

    public static String decodefix(String s){
        //TR karakter sorununa merdiven altı çözüm :)
        String search[] = new String[] {"Ã\u0087","Ã§","Ä","Ä\u009F","Ä±","Ä°","Ã\u0096","Ã¶","Å\u009E","Å\u009F","Ã\u009C","Ã¼"};
        String replace[] = new String[] {"Ç","ç","Ğ","ğ","ı","İ","Ö","ö","Ş","ş","Ü","ü"};

        for(int i = 0; i < search.length; i++){
            s = s.replaceAll(search[i],replace[i]);
        }
        return s;
    }

    public static String getThumbnail(String capsurl) {
        String mediumurl = capsurl.substring(capsurl.length() - 4);
        if(!capsurl.contains(".th."))
            return capsurl.replaceAll(mediumurl,".th" + mediumurl);
        else return capsurl;
    }

    public static String radiodecodefix(String s){
        String search[] = new String[] {"&#xC3;&#x87;", "&#xC3;&#xA7;", "&#xC4;&#x9E;", "&#xC4;&#x9F;", "&#xC4;&#xB1;", "&#xC4;&#xB0;", "&#xC3;&#x96;", "&#xC3;&#xB6;", "&#xC5;&#x9E;", "&#xC5;&#x9F;", "&#xC3;&#x9C;", "&#xC3;&#xBC;"};
        String replace[] = new String[] {"Ç","ç","Ğ","ğ","ı","İ","Ö","ö","Ş","ş","Ü","ü"};

        for(int i = 0; i < search.length; i++){
            s = s.replaceAll(search[i],replace[i]);
        }
        return s;
    }

    public static String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }



    //site kullanılan smileylerın BBcode karşılıkları
    private static String getIncitorrentSmileys(String post){
        String smileys[] = {"gmansmile","YSB",":arap:","\\(gc\\)","SBH",":lan\\!","aygötüm","\\(S\\)",":cahil",":NS:","lan\\!\\?",":ypm:","\\[i\\]","\\[b\\]","\\[\\/i\\]","\\[\\/b\\]",":\\)",":D","\n","\\(hl\\?\\)","\\*nopanic","\\:V\\:","demeya\\!\\?","\\:hmm"};
        String smileyres[] = {"<img src='gmansmile'/>","<img src='YSB'/>","<img src='arap'/>","<img src='gc'/>","<img src='SBH'/>","<img src='lann0lebowski'/>","<img src='ayg'/>","<img src='<sikimizdedegil>'/>","<img src='<cahil>'/>","<img src='<nereyeS>'/>","<img src='000lan000'/>","<img src='<ypm>'/>","<i>","<b>","</i>","</b>","<img src='olumlu'/>","<img src='lol'/>","<br>","<img src='hl'/>","<img src='nopanic'/>","<img src='v'/>","<img src='yds'/>","<img src='eizen'/>"};
        for(int i = 0; i< smileys.length; i++) post =  post.replaceAll(smileys[i],smileyres[i]);
        return post;
    }


    public static String getCapsUrl(String mesaj) {
        String capsurl = mesaj.split("caps")[1];
        capsurl = capsurl.split(" ")[0];
        return "https://caps" +capsurl.trim();
    }

    /**
     * Get Youtube Id from chat post
     * @param post
     * @return youtube id string
     */
    public static String getYoutubeId(String post){
        if(post.contains("youtube.com/watch")){
            post = post.split("v=")[1];
            return post.split(" ")[0];
        }else if(post.contains("youtu.be/")){
            post = post.split("youtu.be/")[1];
            return post.split(" ")[0];
        }else return null;
    }

    /**
     * Get youtube thumbnail from youtube video id
     * @param id youtube id
     * @return thumbnail image link
     */
    public static String getYoutubeThumbnail(String id){
        return "http://img.youtube.com/vi/" + id + "/0.jpg";
    }

    /**
     * Lauch youtube app
     * @param id youtube id
     */
    public void openYoutubeLink(String id){
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://www.youtube.com/watch?v=" + id));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    /**
     * Go to caps intent relative to condition
     * @param capsurl url of the image
     * @param ımageView imageview to load
     * @param activity activity required for workaroud
     */
    public void goToCapsIntent(String capsurl, ImageView ımageView, Activity activity) {
        Intent image_intent;
        Boolean showcomments = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("open_gallery", false);
        if (!isLoggedIn()) {
            image_intent = new Intent(context, show_image.class);
            image_intent.setData(Uri.parse(capsurl));
        } else if (showcomments) {
            image_intent = new Intent(context, show_image_comments.class);
            image_intent.putExtra("url", capsurl);
        } else {
            image_intent = new Intent(context, show_image.class);
            image_intent.setData(Uri.parse(capsurl));
        }
        image_intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity,
                    new Pair<View, String>(ımageView, ımageView.getTransitionName()));
            activity.startActivity(image_intent, options.toBundle());
        } else
            activity.startActivity(image_intent);
    }

//Bildirim, son çalanlar, ve kilit ekranı için şarkı albüm kapağını hafızadan çek
    public Bitmap getMenemenArt(String songurl,Boolean locksreen){
        //Artwork indirilme ayarı açık değilse varsayılan resim döndür
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), (locksreen) ? R.mipmap.ic_header_background : R.mipmap.album_placeholder);
        if(!PreferenceManager.getDefaultSharedPreferences(context).getBoolean("download_artwork",true) || songurl.equals("default") || !isConnectionFast())
            return bitmap;
        try {
            int dim = RadyoMenemenPro.ARTWORK_IMAGE_OVERRIDE_DIM;
            int fallback = R.mipmap.album_placeholder;
            if(locksreen) {
                dim = 500; //pixel olarak
                fallback = R.mipmap.ic_header_background;
            }
            return Glide.with(context)
                    .load(songurl)
                    .asBitmap()
                    .error(fallback)
                    .into(dim,dim)
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            bitmap = BitmapFactory.decodeResource(context.getResources(), (locksreen) ? R.mipmap.ic_header_background : R.mipmap.album_placeholder);
        }
        return bitmap;
    }

    public void runEnterAnimation(View view, int delay) {
        view.setVisibility(View.VISIBLE);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;
        view.setTranslationY(height);
        int anim_id = 0;
        try {
            anim_id = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("enter_anim","0"));
        }catch (Exception e){
            anim_id = 1;
            e.printStackTrace();
        }
        view.animate()
                .translationY(0)
                .setInterpolator(enter_anim(anim_id))
                .setDuration(700 + delay)
                .start();
    }

    private Interpolator enter_anim(int anim_id){
        switch (anim_id){
            case 1:
                return new AccelerateDecelerateInterpolator();
            case 2:
                return new AccelerateInterpolator();
            case 3:
                return new AnticipateInterpolator();
            case 4:
                return new AnticipateOvershootInterpolator();
            case 5:
                return new BounceInterpolator();
            case 6:
                return new DecelerateInterpolator(3.f);
            case 7:
                return new FastOutLinearInInterpolator();
            case 8:
                return new FastOutSlowInInterpolator();
            case 9:
                return new LinearInterpolator();
            case 10:
                return new LinearOutSlowInInterpolator();
            case 11:
                return new OvershootInterpolator();
            default:
                return new DecelerateInterpolator(3.f);
        }
    }

    public void runExitAnimation(View view, int delay) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;
        view.setTranslationY(0);
        view.animate()
                .translationY(height)
                .setInterpolator(new DecelerateInterpolator(3.f))
                .setDuration(700 + delay)
                .start();
    }

    public void setBadge(TextView view, String badge) {
        view.setGravity(Gravity.CENTER_VERTICAL);
        view.setTypeface(null, Typeface.BOLD);
        view.setTextColor(ContextCompat.getColor(context,R.color.colorAccent));
        view.setText(badge);
    }

    public void setToken() {
        try {
            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest postRequest = new StringRequest(Request.Method.POST, RadyoMenemenPro.TOKEN_ADD,
                    new Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(String response) {
                        }
                    },null
            ) {

                @Override
                protected Map<String, String> getParams(){
                    String token = FirebaseInstanceId.getInstance().getToken();
                    HashMap<String, String> dataToSend = new HashMap<>();
                    dataToSend.put("nick", oku("username"));
                    dataToSend.put("token", token);
                    return dataToSend;
                }
            };
            postRequest.setRetryPolicy(new DefaultRetryPolicy(2500,10,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(postRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    public static String fromHtmlCompat(String title){
        if(title == null) return "null";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
            return Html.fromHtml(title, Html.FROM_HTML_MODE_LEGACY).toString();
        else
            return Html.fromHtml(title).toString();
    }

    public Spanned getSpannedTextWithSmileys(String title){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N){
           return  (Html.fromHtml(getIncitorrentSmileys(title),new Html.ImageGetter() {
                @Override
                public Drawable getDrawable(String source) {
                    int id = 0;
                    switch (source){
                        case "gmansmile": id= R.mipmap.smile_gman;  break;
                        case "YSB": id= R.mipmap.ysb;  break;
                        case "arap": id= R.mipmap.smile_arap;  break;
                        case "gc": id= R.mipmap.smile_keci;  break;
                        case "SBH": id= R.mipmap.smile_sbh;  break;
                        case "000lan000": id= R.mipmap.smile_lan;  break;
                        case "lann0lebowski": id= R.mipmap.smile_lann;  break;
                        case "olumlu": id= R.mipmap.smile_olumlu;  break;
                        case "lol": id= R.mipmap.smile_gulme;  break;
                        case "ayg": id= R.mipmap.smile_ayg;  break;
                        case "<sikimizdedegil>": id= R.mipmap.smile_sd;  break;
                        case "<cahil>": id = R.mipmap.smile_cahil; break;
                        case "<nereyeS>": id = R.mipmap.smile_ns; break;
                        case "<ypm>": id = R.mipmap.ypm; break;
                        case "hl": id = R.mipmap.smile_harbimi; break;
                        case "nopanic": id = R.mipmap.smile_panikyok; break;
                        case "v": id = R.mipmap.v; break;
                        case "yds": id = R.mipmap.yds; break;
                        case "eizen": id = R.mipmap.eizen; break;
                    }


                    Drawable d = context.getResources().getDrawable(id);
                    d.setBounds(0,0,d.getIntrinsicWidth(),d.getIntrinsicHeight());
                    return d;
                }
            },null));
        }else {
            return (Html.fromHtml(getIncitorrentSmileys(title),Html.FROM_HTML_MODE_LEGACY,new Html.ImageGetter() {
                @Override
                public Drawable getDrawable(String source) {
                    int id = 0;
                    switch (source){
                        case "gmansmile": id= R.mipmap.smile_gman;  break;
                        case "YSB": id= R.mipmap.ysb;  break;
                        case "arap": id= R.mipmap.smile_arap;  break;
                        case "gc": id= R.mipmap.smile_keci;  break;
                        case "SBH": id= R.mipmap.smile_sbh;  break;
                        case "000lan000": id= R.mipmap.smile_lan;  break;
                        case "lann0lebowski": id= R.mipmap.smile_lann;  break;
                        case "olumlu": id= R.mipmap.smile_olumlu;  break;
                        case "lol": id= R.mipmap.smile_gulme;  break;
                        case "ayg": id= R.mipmap.smile_ayg;  break;
                        case "<sikimizdedegil>": id= R.mipmap.smile_sd;  break;
                        case "<cahil>": id = R.mipmap.smile_cahil; break;
                        case "<nereyeS>": id = R.mipmap.smile_ns; break;
                        case "<ypm>": id = R.mipmap.ypm; break;
                        case "hl": id = R.mipmap.smile_harbimi; break;
                        case "nopanic": id = R.mipmap.smile_panikyok; break;
                        case "v": id = R.mipmap.v; break;
                        case "yds": id = R.mipmap.yds; break;
                        case "eizen": id = R.mipmap.eizen; break;
                    }


                    Drawable d = context.getResources().getDrawable(id);
                    d.setBounds(0,0,d.getIntrinsicWidth(),d.getIntrinsicHeight());
                    return d;
                }
            },null));
        }
    }


    public void saveTime(String key, long period){
        final SharedPreferences kaydet = context.getApplicationContext().getSharedPreferences(RadyoMenemenPro.SHAREDPREF, Context.MODE_PRIVATE);
        kaydet.edit().putLong(key,System.currentTimeMillis() + period).apply();
    }

    public synchronized long getSavedTime(String key){
      return context.getApplicationContext().getSharedPreferences(RadyoMenemenPro.SHAREDPREF, Context.MODE_PRIVATE).getLong(key,0);
    }

    public void trackEvent(String event, Bundle bundle){
        try {
            FirebaseAnalytics.getInstance(context).logEvent(event, bundle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get players basic state
     * @return true if radio is playing
     */
    public boolean isPlaying() { return bool_oku("playing"); }

    /**
     * Saves the radio "playing" state across everywhere in app persistantly
     * @param isPlaying radio playing state
     */
    public void setPlaying(boolean isPlaying){
        bool_kaydet("playing",isPlaying);
    }

    public String getMobilKey(){
        return oku(RadyoMenemenPro.MOBIL_KEY);
    }

    public String getUsername(){
        return oku("username");
    }

    public Boolean isLoggedIn() { return bool_oku("loggedin"); }

    public Map<String, String> getAuthMap(){
        Map<String, String> dataToSend = new HashMap<>();
        dataToSend.put(RadyoMenemenPro.NICK, getUsername());
        dataToSend.put(RadyoMenemenPro.MOBIL_KEY, getMobilKey());
        return dataToSend;
    }

    public boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    public static final String PENDING = "pending";
    public static final String NOT_DELIVERED = "notdelivered";
    public static final String DELIVERED = "delivered";

    /**
     * Get time in prettier formatted string
     * @param Sdate date in string format
     * @param c Context instance for localized strings
     * @return pretty formatted string
     */
    public static String getTimeAgo(String Sdate, Context c) {
        if(Sdate==null) return c.getString(R.string.time_moment);
        if(Sdate.equals(PENDING)) return c.getString(R.string.sending);
        if(Sdate.equals(NOT_DELIVERED)) return c.getString(R.string.not_delivered);
        if(Sdate.equals(DELIVERED)) return c.getString(R.string.delivered);
        DateFormat df = new SimpleDateFormat(RadyoMenemenPro.CHAT_DATE_FORMAT, Locale.US);
        Date date = null;
        try {
            date = df.parse(Sdate);
        } catch (ParseException e) {
            e.printStackTrace();
            }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        date = cal.getTime();
        long time = date.getTime();


        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return c.getString(R.string.time_moments);
        }

        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return c.getString(R.string.time_moment);
        } else if (diff < 2 * MINUTE_MILLIS) {
            return c.getString(R.string.time_moments);
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + c.getString(R.string.time_mins) + " " + c.getString(R.string.time_ago);
        } else if (diff < 90 * MINUTE_MILLIS) {
            return c.getString(R.string.hour_ago);
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + c.getString(R.string.time_hrs) + " " + c.getString(R.string.time_ago);
        } else if (diff < 48 * HOUR_MILLIS) {
            return c.getString(R.string.time_yesterday);
        } else {
            return diff / DAY_MILLIS + c.getString(R.string.time_days) + " " + c.getString(R.string.time_ago);
        }
    }

    /**
     * Return date in specified format.
     * @param milliSeconds Date in milliseconds
     * @param dateFormat Date format
     * @return String representing date in specified format
     */
    public static String getFormattedDate(long milliSeconds, String dateFormat)
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat, Locale.US);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public static int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }
    public capsDB getCapsDB(){
        return new capsDB(context,null,null,1);
    }

    public chatDB getChatDB(){
        return new chatDB(context,null,null,1);
    }

    public topicDB getTopicDB(){
        return new topicDB(context,null,null,1);
    }

    public boolean isReachable(String siteurl){
        if(!isInternetAvailable()) return false;
        try {
            URL url = new URL(siteurl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(1000);
            int code = connection.getResponseCode();
            connection.disconnect();
            if(code == 200) {
                Log.d("RespCode","200");
                return true;
            } else {
                Log.d("RespCode", "code: " + code);
                return false;
            }
        } catch (Exception e) {
            Log.d("RespCode", e.toString());
            e.printStackTrace();
        }
        return false;
    }
    /**
     * Change stream adress depending on phones net stat and streams availability
     */
    public void selectChannelAuto() {
        final String selected_channel = oku(PreferenceManager.getDefaultSharedPreferences(context).getString("radio_channel",RadyoMenemenPro.HIGH_CHANNEL));
        if(!isInternetAvailable()) return; //internet is unavailable
        if(isPlaying()) return; //Radio is playing do not touch channels
        new AsyncTask<Void,Void,Void>(){

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean("adaptive_quality",true)){
                        //Wi-Fi connected switch to highest channel
                        if(isConnectedWifi()){
                            if(isReachable(oku(RadyoMenemenPro.HIGH_CHANNEL))){
                                PreferenceManager.getDefaultSharedPreferences(context)
                                        .edit()
                                        .putString("radio_channel",RadyoMenemenPro.HIGH_CHANNEL)
                                        .apply();
                            }else setDefaultChannel(); //Fallback channel
                        }else{
                            //Not connected via Wi-Fi lower the quality
                            if(isReachable(oku(RadyoMenemenPro.LOW_CHANNEL))){
                                PreferenceManager.getDefaultSharedPreferences(context)
                                        .edit()
                                        .putString("radio_channel",RadyoMenemenPro.LOW_CHANNEL)
                                        .apply();
                            }else setDefaultChannel();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                try {
                    if(!oku(PreferenceManager.getDefaultSharedPreferences(context).getString("radio_channel",RadyoMenemenPro.HIGH_CHANNEL)).equals(selected_channel))
                        Toast.makeText(context, R.string.toast_channel_change_auto, Toast.LENGTH_SHORT).show();
                } catch (NullPointerException | Resources.NotFoundException e) {
                    e.printStackTrace();
                }
                super.onPostExecute(aVoid);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);



    }
    private void setDefaultChannel() {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString("radio_channel",RadyoMenemenPro.MID_CHANNEL)
                .apply();
    }

    /**
     * Download podcast files or else with alertdialog
     * @param url
     * @param filename
     * @param icon
     * @param dir
     * @param ext
     * @param activity
     */
    public void downloadMenemenFile(final String url, final String filename, int icon, final String dir, final String ext, Activity activity) {
        new AlertDialog.Builder(new ContextThemeWrapper(activity, R.style.alertDialogTheme))
                .setTitle(filename)
                .setMessage(context.getString(R.string.download_file))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                            request.setDescription(context.getString(R.string.downloading_file))
                                    .setTitle(filename)
                                    .allowScanningByMediaScanner();
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                            String sanitized = filename.replaceAll("[^\\w.-]", "_");
                            Log.v(TAG," Sanitized string " + sanitized);
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, sanitized + ext);
                            else
                                request.setDestinationInExternalPublicDir(dir, sanitized + ext);
                            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                            manager.enqueue(request);
                            Toast.makeText(context, context.getString(R.string.downloading_file), Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(context, R.string.error_occured, Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setIcon(icon)
                .show();
    }

    public void updateRadioWidget() {
        if(!bool_oku(RadyoMenemenPro.DEFAULT_RADIO_WIDGET)) return;
        int[] ids = AppWidgetManager.getInstance(context.getApplicationContext()).getAppWidgetIds(new ComponentName(context.getApplicationContext(), RadioWidget.class));
        RadioWidget radioWidget = new RadioWidget();
        radioWidget.onUpdate(context.getApplicationContext(), AppWidgetManager.getInstance(context.getApplicationContext()),ids);
    }

    public void updateSqrRadioWidget() {
        if(!bool_oku(RadyoMenemenPro.SQUARE_RADIO_WIDGET)) return;
        int[] ids = AppWidgetManager.getInstance(context.getApplicationContext()).getAppWidgetIds(new ComponentName(context.getApplicationContext(), RadioWidgetSqr.class));
        RadioWidgetSqr radioWidget = new RadioWidgetSqr();
        radioWidget.onUpdate(context.getApplicationContext(), AppWidgetManager.getInstance(context.getApplicationContext()),ids);
    }

    public void showNPToast(final String track, final String artwork){
        if(bool_oku(RadyoMenemenPro.IS_RADIO_FOREGROUND)) return;
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    new AsyncTask<Void,Void,Palette>(){
                        String t_track;
                        String t_art;
                        @Override
                        protected void onPreExecute() {
                            t_track = (track == null) ? Menemen.fromHtmlCompat(oku(CALAN)) : track;
                            t_art = (artwork == null) ? oku(MUSIC_INFO_SERVICE.LAST_ARTWORK_URL) : artwork;
                            super.onPreExecute();
                        }

                        @Override
                        protected Palette doInBackground(Void... voids) {
                            try {
                                if(t_art.equals("default")) return null;
                                Bitmap resim = Glide.with(context)
                                        .load(t_art)
                                        .asBitmap()
                                        .error(R.mipmap.album_placeholder)
                                        .into(RadyoMenemenPro.ARTWORK_IMAGE_OVERRIDE_DIM,RadyoMenemenPro.ARTWORK_IMAGE_OVERRIDE_DIM)
                                        .get();
                                return Palette.from(resim).generate();

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Palette palette) {
                            try {
                                Toast toast = NPToast(t_track,t_art,palette);
                                if(toast != null) toast.show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            super.onPostExecute(palette);
                        }
                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Toast NPToast(String track, String artwork, Palette palette) throws NullPointerException{
        try {
            final int backgroundcolor = ContextCompat.getColor(context,R.color.cardviewBG);
            final int textcolor = ContextCompat.getColor(context,R.color.textColorPrimary);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            final View toastview = inflater.inflate(R.layout.now_playing_toast,null);
            CardView toastcard = (CardView) toastview.findViewById(R.id.toast_card);
            ImageView t_image = (ImageView) toastview.findViewById(R.id.toast_art);
            TextView t_text = (TextView) toastview.findViewById(R.id.toast_text);
            if(palette != null) {
                toastcard.setCardBackgroundColor(adjustAlpha(palette.getMutedColor(backgroundcolor),0.8f));
                t_text.setTextColor((palette.getMutedSwatch() != null) ? palette.getMutedSwatch().getTitleTextColor() : textcolor);
            }

            if(!artwork.equals("default"))
                Glide.with(context)
                        .load(artwork)
                        .override(RadyoMenemenPro.ARTWORK_IMAGE_OVERRIDE_DIM, RadyoMenemenPro.ARTWORK_IMAGE_OVERRIDE_DIM)
                        .centerCrop()
                        .placeholder(R.mipmap.album_placeholder)
                        .error(R.mipmap.album_placeholder)
                        .into(t_image);
            t_text.setText(track);
            Toast toast = new Toast(context);
            toast.setView(toastview);
            toast.setDuration(Toast.LENGTH_LONG);
            TypedValue tv = new TypedValue();
            int actionBarHeight = 0;
            if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
            }
            toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, actionBarHeight + 16);
            return toast;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public RetryPolicy menemenRetryPolicy(){
      return new RetryPolicy() {
          @Override
          public int getCurrentTimeout() {
              return RadyoMenemenPro.MENEMEN_TIMEOUT;
          }

          @Override
          public int getCurrentRetryCount() {
              return 0;
          }

          @Override
          public void retry(VolleyError error) throws VolleyError {
            Log.d(TAG, "Volley Error " + error.toString());
              throw new VolleyError("Do Not Retry");
          }
      };
    }
    public static Bitmap resizeBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 0) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public NotificationCompat.Action getDirectReplyAction(Intent direct_reply_intent){
        RemoteInput remoteinput = new RemoteInput.Builder(FIREBASE_CM_SERVICE.DIRECT_REPLY_KEY)
                .setLabel(context.getString(R.string.hint_write_something))
                .build();
        return new NotificationCompat.Action.Builder(R.mipmap.ic_chat,
                context.getString(R.string.hint_write_something),
                PendingIntent.getBroadcast(context, new Random().nextInt(200),direct_reply_intent,PendingIntent.FLAG_ONE_SHOT))
                .addRemoteInput(remoteinput)
                .setAllowGeneratedReplies(true)
                .build();
    }

    public String getRadioDataSource(){
//        return oku(PreferenceManager.getDefaultSharedPreferences(context).getString("radio_channel",RadyoMenemenPro.FALLBACK_CHANNEL));
        return RadyoMenemenPro.FALLBACK_CHANNEL;
    }

    public static ArrayList<podcast_objs> PodcastList = new ArrayList<>();

    /**
     * Setting a long text on toolbar and make it marquee
     * @param toolbar
     * @param text
     */
    public void setToolbarSubtitleMarquee(@Nullable Toolbar toolbar, String text){
        if(toolbar == null) return;
        if(text == null || text.length()<1) return;
        try{
            toolbar.setSubtitle(".");
            Field field = Toolbar.class.getDeclaredField("mSubtitleTextView");
            field.setAccessible(true);
            TextView subtitleTextView = (TextView)field.get(toolbar);
            subtitleTextView.setSingleLine(true);
            subtitleTextView.setFocusable(true);
            subtitleTextView.setFocusableInTouchMode(true);
            subtitleTextView.requestFocus();
            subtitleTextView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            subtitleTextView.setText(text);
        }catch(Exception e) {
            e.printStackTrace();
        }
    }



}
