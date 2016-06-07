package com.incitorrent.radyo.menemen.pro.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;

import com.bumptech.glide.Glide;
import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
    //İnternet var mı?
    public boolean isInternetAvailable(){
        try {
            ConnectivityManager nInfo = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            nInfo.getActiveNetworkInfo().isConnectedOrConnecting();
            Log.v(TAG, "Network:"
                    + nInfo.getActiveNetworkInfo().isConnectedOrConnecting());
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
    //Eğer resim hali hazırda resim kayıtlı değilse indirir
    public void downloadImageIfNecessary(String songid,String url){
        try {
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root + "/Satbax/Radio/artworks");
            myDir.mkdirs();
            String fname = songid + ".jpg";
            File file = new File(myDir, fname);
            File nomedia = new File(myDir, ".nomedia");
            nomedia.createNewFile();  //Nomedia Oluştur, artworklerin telefon galerisinde görünmemesi için
            //Dosya işlemleri end
            if (!url.equals("default")) {
                if(!file.exists()){//Eğer dbde yoksa
                    Log.i("RADYO SERVİSİ", "Yeni artwork indiriliyor" + url + "saved" + oku("savedurl"));
                    URL urlConnection = new URL(url);
                    HttpURLConnection connection = (HttpURLConnection) urlConnection
                            .openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    Bitmap myBitmap = BitmapFactory.decodeStream(input);
                    //Artwork İndir
                    FileOutputStream out = new FileOutputStream(file);
                    myBitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
                    out.flush();
                    out.close();
                }else Log.v("Radyo Servisi", "Dosya var " + file.exists());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getElapsed(String Sdate){
        if(Sdate==null) return context.getString(R.string.time_moment);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date _date = null;
        try {
            _date = df.parse(Sdate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date _now = new Date(System.currentTimeMillis());
        Calendar cal = Calendar.getInstance();
        cal.setTime(_date);
//        cal.add(Calendar.HOUR_OF_DAY, 3);
        _date = cal.getTime();
        long diff = _now.getTime() - _date.getTime();
        int diffMinutes = (int)diff / (60 * 1000);
        String mesaj = "";
        if(diffMinutes < 60 && diffMinutes>1)
            mesaj = String.valueOf(diffMinutes) + context.getString(R.string.time_mins) + " " + context.getString(R.string.time_ago);
        else if(60 <= diffMinutes && diffMinutes < 60*24)
            mesaj = String.valueOf(diffMinutes / 60) + context.getString(R.string.time_hrs) + " " + context.getString(R.string.time_ago);
        else if(60*24 <= diffMinutes && diffMinutes < 30*60*24)
            mesaj = String.valueOf(diffMinutes / (60 * 24)) + context.getString(R.string.time_days) + " " + context.getString(R.string.time_ago);
        else if(diffMinutes<1)
            mesaj = context.getString(R.string.time_moment); //Az önce
        else
            mesaj = context.getString(R.string.time_moments); //Biraz önce (Belli değil)
        return mesaj;
    }
    //site kullanılan smileylerın BBcode karşılıkları
    public static String getIncitorrentSmileys(String post){
        String smileys[] = {"gmansmile","YSB",":arap:","\\(gc\\)","SBH",":lan\\!","aygötüm","\\(S\\)",":cahil",":NS:","lan\\!\\?",":ypm:","\\[i\\]","\\[b\\]","\\[\\/i\\]","\\[\\/b\\]",":\\)",":D","\n"};
        String smileyres[] = {"<img src='gmansmile'/>","<img src='YSB'/>","<img src='arap'/>","<img src='gc'/>","<img src='SBH'/>","<img src='lann0lebowski'/>","<img src='ayg'/>","<img src='<sikimizdedegil>'/>","<img src='<cahil>'/>","<img src='<nereyeS>'/>","<img src='000lan000'/>","<img src='<ypm>'/>","<i>","<b>","</i>","</b>","<img src='olumlu'/>","<img src='lol'/>","<br>"};
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return line;
    }

    //POST methodu ile veri gönder ve JSON döndür
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
        } catch (IOException e) {
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

           int dim = 48; //Bildirim için resim boyutu (yükseklik & genişlik)
            final float scale = context.getResources().getDisplayMetrics().density;
            dim = (int) (48 * scale + 0.5f); // dp olarak
            int fallback = R.mipmap.album_placeholder;
            if(locksreen) {
                dim = 500; //pixel olarak
                fallback = R.mipmap.ic_header_background;
            }
            return Glide.with(context).load(songurl).asBitmap().error(fallback).into(dim,dim).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public void runEnterAnimation(View view, int delay) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;
        view.setTranslationY(height);
        view.animate()
                .translationY(0)
                .setInterpolator(new DecelerateInterpolator(3.f))
                .setDuration(700 + delay)
                .start();
    }
}
