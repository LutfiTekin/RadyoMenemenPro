package com.incitorrent.radyo.menemen.pro.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.util.Pair;
import android.view.Display;
import android.view.Gravity;
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

import com.bumptech.glide.Glide;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;
import com.incitorrent.radyo.menemen.pro.show_image;
import com.incitorrent.radyo.menemen.pro.show_image_comments;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

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
        Log.v("Kayıt", "yazılıyor " + title + " " + content);
    }
    //Kaydedilen bilgileri okumak için sharedpref metodu
    public String oku(String title){
        final SharedPreferences oku = context.getApplicationContext().getSharedPreferences(RadyoMenemenPro.SHAREDPREF, Context.MODE_PRIVATE);
        Log.v("Kayıt", "okunuyor " + title);
        return oku.getString(title, "yok"); //Değer boş ise "yok"
    }


    public void bool_kaydet(String title,Boolean content){
        final SharedPreferences kaydet = context.getApplicationContext().getSharedPreferences(RadyoMenemenPro.SHAREDPREF, Context.MODE_PRIVATE);
        kaydet.edit().putBoolean(title,content).apply();
        Log.v("Kayıt", "yazılıyor " + title + " " + content);
    }

    public Boolean bool_oku(String title){
        final SharedPreferences oku = context.getApplicationContext().getSharedPreferences(RadyoMenemenPro.SHAREDPREF, Context.MODE_PRIVATE);
        Log.v("Kayıt", "okunuyor " + title);
        return oku.getBoolean(title, false); //Değer boş ise false
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
    public void resetFirstTime(String action){
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
    public static String encodefix(String s){
        String search[] = new String[] {"Ç","ç","Ğ","ğ","ı","İ","Ö","ö","Ş","ş","Ü","ü"};
        String replace[] = new String[] {"&#199","&#231","&#286","&#287","&#305","&#304","&#214","&#246","&#350","&#351","&#220","&#252"};
        for(int i = 0; i < search.length; i++){
            s = s.replaceAll(search[i],replace[i]);
        }
        return s;
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
    public static String getIncitorrentSmileys(String post){
        String smileys[] = {"gmansmile","YSB",":arap:","\\(gc\\)","SBH",":lan\\!","aygötüm","\\(S\\)",":cahil",":NS:","lan\\!\\?",":ypm:","\\[i\\]","\\[b\\]","\\[\\/i\\]","\\[\\/b\\]",":\\)",":D","\n","(hl\\?)","\\*nopanic","\\:V\\:","demeya\\!\\?","\\:hmm"};
        String smileyres[] = {"<img src='gmansmile'/>","<img src='YSB'/>","<img src='arap'/>","<img src='gc'/>","<img src='SBH'/>","<img src='lann0lebowski'/>","<img src='ayg'/>","<img src='<sikimizdedegil>'/>","<img src='<cahil>'/>","<img src='<nereyeS>'/>","<img src='000lan000'/>","<img src='<ypm>'/>","<i>","<b>","</i>","</b>","<img src='olumlu'/>","<img src='lol'/>","<br>","<img src='hl'/>","<img src='nopanic'/>","<img src='v'/>","<img src='yds'/>","<img src='eizen'/>"};
        for(int i = 0; i< smileys.length; i++) post =  post.replaceAll(smileys[i],smileyres[i]);
        return post;
    }
    //Json stringi döndür
    public static String getMenemenData(String url){
        //Json datası döndür
        String line = null;
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("GET");
            con.setDoOutput(true);
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    con.getInputStream(), "iso-8859-9"), 8);
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
           line = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return line;
    }

    public static String getCapsUrl(String mesaj) {
        String capsurl = mesaj.split("caps")[1];
        capsurl = capsurl.split(" ")[0];
        Log.v(TAG, "getCapsurl" + capsurl);
        return "http://caps" +capsurl.trim();
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
     * @param id
     * @return thumbnail image link
     */
    public static String getYoutubeThumbnail(String id){
        return "http://img.youtube.com/vi/" + id + "/0.jpg";
    }

    /**
     * Lauch youtube app
     * @param id
     */
    public void openYoutubeLink(String id){
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
            context.startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://www.youtube.com/watch?v=" + id));
            context.startActivity(intent);
        }
    }

    /**
     * Go to caps intent relative to condition
     * @param capsurl
     * @param ımageView
     * @param activity
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity,
                    new Pair<View, String>(ımageView, ımageView.getTransitionName()));
            activity.startActivity(image_intent, options.toBundle());
        } else
            activity.startActivity(image_intent);
    }
    //POST methodu ile veri gönder ve JSON döndür
    /**
     * Posts data to server using post method with httpurlconnection
     * @param url server url
     * @param encodedstr data to send
     * @return json string
     */
    public static String postMenemenData(String url,String encodedstr){
        //Json datası döndür
        String line = null;
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
            writer.write(encodedstr);
            writer.flush();
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    con.getInputStream(), "iso-8859-9"), 8);
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            line = sb.toString();
            Log.v("POSTMENEMENDATA",line);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return line;
    }
//Bildirim, son çalanlar, ve kilit ekranı için şarkı albüm kapağını hafızadan çek
    public Bitmap getMenemenArt(String songurl,Boolean locksreen){
        //Artwork indirilme ayarı açık değilse varsayılan resim döndür
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), (locksreen) ? R.mipmap.ic_header_background : R.mipmap.album_placeholder);
        if(!PreferenceManager.getDefaultSharedPreferences(context).getBoolean("download_artwork",true))
            return bitmap;
        try {
            final float scale = context.getResources().getDisplayMetrics().density;
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
//                .setInterpolator(new DecelerateInterpolator(3.f))
                .setInterpolator(enter_anim(anim_id))
                .setDuration(700 + delay)
                .start();
    }

    public Interpolator enter_anim(int anim_id){
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
            String token = FirebaseInstanceId.getInstance().getToken();
            Map<String, String> dataToSend = new HashMap<>();
            dataToSend.put("nick", oku("username"));
            dataToSend.put("token", token);
            String encodedStr = getEncodedData(dataToSend);
            Menemen.postMenemenData(RadyoMenemenPro.TOKEN_ADD,encodedStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String fromHtmlCompat(String title){
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

    public long getSavedTime(String key){
      return context.getApplicationContext().getSharedPreferences(RadyoMenemenPro.SHAREDPREF, Context.MODE_PRIVATE).getLong(key,0);
    }

    public void trackEvent(String event, Bundle bundle){
        try {
            FirebaseAnalytics.getInstance(context).logEvent(event, bundle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Boolean isPlaying() { return oku("caliyor").equals("evet"); }

    public String getMobilKey(){
        return oku("mkey");
    }

    public String getUsername(){
        return oku("username");
    }

    public Boolean isLoggedIn() { return bool_oku("loggedin"); }

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
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
}
