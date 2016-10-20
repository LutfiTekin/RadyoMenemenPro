package com.incitorrent.radyo.menemen.pro;

import android.app.ActivityOptions;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;
import com.incitorrent.radyo.menemen.pro.utils.OnSwipeTouchListener;
import com.incitorrent.radyo.menemen.pro.utils.TouchImageView;
import com.incitorrent.radyo.menemen.pro.utils.capsDB;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class show_image extends AppCompatActivity{
    private static final String TAG = "show_image";
    private TouchImageView image;
    private String imageurl;
    private ProgressBar progressBar;
    Menemen m;
    Context context;
    FloatingActionButton c_fab;
    capsDB sql;
    final String root = Environment.getExternalStorageDirectory().toString();
    ContentResolver contentResolver;
    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setExitTransition(new Explode());
        }
        super.onCreate(savedInstanceState);
        context = show_image.this;
        setContentView(R.layout.activity_show_image);
        m = new Menemen(context);
        sql = new capsDB(context,null,null,1);
        contentResolver = this.getContentResolver();
        progressBar = (ProgressBar) findViewById(R.id.loading);
        image = (TouchImageView) findViewById(R.id.show_image);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            image.setTransitionName("show_image");
        }
        final Intent mintent = getIntent();
        final Uri imageUri = mintent.getData();
        imageurl = imageUri.toString().trim();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);
        c_fab = (FloatingActionButton) findViewById(R.id.comment_fab);
        if (c_fab != null) c_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!m.isLoggedIn()){
                    Toast.makeText(show_image.this, R.string.toast_caps_comment_required_login, Toast.LENGTH_LONG).show();
                    return;
                }
                Intent showimagecomment = new Intent(show_image.this, show_image_comments.class);
                showimagecomment.putExtra("url", imageurl);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    c_fab.setTransitionName("fab");
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(show_image.this,
                            new Pair<View, String>(image, image.getTransitionName()),
                            new Pair<View, String>(c_fab, c_fab.getTransitionName()));
                    startActivity(showimagecomment, options.toBundle());
                }else
                startActivity(showimagecomment);
            }
        });
        loadThumbnail();
        image.setOnTouchListener(new OnSwipeTouchListener(show_image.this){
        @Override
        public void onSwipeRight() {
            loadNextCaps(true);
            Log.v(TAG,"t");
            super.onSwipeRight();
        }

        @Override
        public void onSwipeLeft() {
            loadNextCaps(false);
            Log.v(TAG,"f");
            super.onSwipeLeft();
        }

            void loadNextCaps(Boolean next) {
                if(image.getCurrentZoom() == 1 && progressBar.getVisibility() == View.GONE) {
                    Log.v(TAG," önce " + imageurl);
                    imageurl = m.getChatDB().getNextCaps(imageurl, next);
                    Log.v(TAG," sonra " +  imageurl);
                    new AsyncTask<Void,Void,Void>(){
                        @Override
                        protected void onPreExecute() {
                            toolbar.setSubtitle("");
                            loadcomments();
                            loadThumbnail();
                            progressBar.setVisibility(View.VISIBLE);
                            super.onPreExecute();
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            loadImage();
                            super.onPostExecute(aVoid);
                        }

                        @Override
                        protected Void doInBackground(Void... voids) {
                            return null;
                        }
                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }

            @Override
            public void onSwipeTop() {
                if(image.getCurrentZoom() == 1){
                    if(!m.isLoggedIn()){
                        Toast.makeText(show_image.this, R.string.toast_caps_comment_required_login, Toast.LENGTH_LONG).show();
                        return;
                    }
                    Intent showimagecomment = new Intent(show_image.this, show_image_comments.class);
                    showimagecomment.putExtra("url", imageurl);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        c_fab.setTransitionName("fab");
                        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(show_image.this,
                                new Pair<View, String>(image, image.getTransitionName()),
                                new Pair<View, String>(c_fab, c_fab.getTransitionName()));
                        startActivity(showimagecomment, options.toBundle());
                    }else
                        startActivity(showimagecomment);
                }
                super.onSwipeTop();
            }
        });
    }

    private void loadThumbnail() {
        try {
            Glide.with(show_image.this)
                    .load(Menemen.getThumbnail(imageurl))
                    .into(new SimpleTarget<GlideDrawable>() {
                        @Override
                        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                            image.setImageDrawable(resource);
                            try {
                                new SetColors().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("url", imageurl);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            imageurl = savedInstanceState.getString("url");
            new AsyncTask<Void,Void,Void>(){
                @Override
                protected void onPreExecute() {
                    toolbar.setSubtitle("");
                    loadcomments();
                    loadThumbnail();
                    progressBar.setVisibility(View.VISIBLE);
                    super.onPreExecute();
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    loadImage();
                    super.onPostExecute(aVoid);
                }

                @Override
                protected Void doInBackground(Void... voids) {
                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        super.onRestoreInstanceState(savedInstanceState);
    }


    @Override
    protected void onStart() {
        loadImage();
        super.onStart();
    }

    private void loadImage() {
        try {
            Glide.with(show_image.this)
                    .load(imageurl)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(new SimpleTarget<GlideDrawable>() {
                        @Override
                        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                            image.setImageDrawable(resource);
                            progressBar.setVisibility(View.GONE);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        if(getSupportActionBar() != null && getSupportActionBar().getSubtitle() == null)
            loadcomments();
        super.onResume();
    }

    @Override
     public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.image_menu, menu);
        return true;
    }

               @Override
               public boolean onOptionsItemSelected(MenuItem item) {
                   int id = item.getItemId();

                   switch (id) {
                       case R.id.action_save:
                           save(true);
                           break;
                       case R.id.action_share:
                           share();
                           break;
                   }

                   return super.onOptionsItemSelected(item);
               }

               public void share() {
                   new Thread(new Runnable() {
                       @Override
                       public void run() {
                           try {
                               String msg = (sql.getFirstComment(imageurl) != null) ? sql.getFirstComment(imageurl) : imageurl;
                               Intent intent = new Intent();
                               intent.setAction(Intent.ACTION_SEND);
                               Bitmap bit = Glide.with(show_image.this).load(imageurl).asBitmap().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();
                               intent.putExtra(Intent.EXTRA_TEXT, msg);
                               String url = MediaStore.Images.Media.insertImage(contentResolver, bit, "Radyo Menemen", msg);
                               Log.v(TAG,url);
                               intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(url));
                               intent.setType("image/*");
                               intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                               startActivity(Intent.createChooser(intent, getString(R.string.share)));
                           } catch (Exception e) {
                               e.printStackTrace();
                           }
                       }
                   }).start();
               }

               public String save(final Boolean toast) {
                   final File compressed = new File(root + "/RadyoMemenen/images/compressed");
                   long time = System.currentTimeMillis();
                   final String fname = String.valueOf(time).substring(0, 12) + ".jpg";
                   final int width = image.getWidth();
                   final int height = image.getHeight();
                   new Thread(new Runnable() {
                       @Override
                       public void run() {
                           try {
                               compressed.mkdirs();
                               File file = new File(compressed, fname);
                               FileOutputStream out = new FileOutputStream(file);
                               Bitmap bit = Glide.with(show_image.this).load(imageurl).asBitmap().into(width, height).get();
                               bit.compress(Bitmap.CompressFormat.JPEG, 100, out);
                               out.flush();
                               out.close();
                           } catch (InterruptedException | ExecutionException | IOException e) {
                               e.printStackTrace();
                           }
                       }
                   }).start();
                   if (toast)
                       Toast.makeText(show_image.this, R.string.toast_image_saved, Toast.LENGTH_SHORT).show();
                   return compressed + "/" + fname;
               }

    class SetColors extends AsyncTask<Void,Void,Integer[]>{

        final int accentcolor = ContextCompat.getColor(context,R.color.colorAccent);
        final int backgroundcolor = ContextCompat.getColor(context,R.color.colorBackgroundsofter);
        final int statusbarcolor = ContextCompat.getColor(context,R.color.colorPrimaryDark);
        final int primary = ContextCompat.getColor(context,R.color.colorPrimary);
        final int primaryText = ContextCompat.getColor(context,R.color.textColorPrimary);


        @Override
        protected Integer[] doInBackground(Void... voids) {
            try {
                Bitmap bitmap = Glide.with(show_image.this).load(Menemen.getThumbnail(imageurl)).asBitmap().into(Target.SIZE_ORIGINAL,Target.SIZE_ORIGINAL).get();
                Palette palette = Palette.from(bitmap).generate();
                int accent = palette.getVibrantColor(accentcolor);
                int background = palette.getDominantColor(backgroundcolor);
                int bar = palette.getDarkVibrantColor(statusbarcolor);
                int actionbar = palette.getVibrantColor(primary);
                int title = palette.getVibrantSwatch() == null ? primaryText : palette.getVibrantSwatch().getTitleTextColor();
                return new Integer[]{accent,background,bar,actionbar,title};
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Integer[] colors) {
            if(colors!=null){
                  c_fab.setBackgroundTintList(ColorStateList.valueOf(colors[0]));
                if(progressBar!=null)
                    progressBar.getIndeterminateDrawable().setColorFilter(colors[0], android.graphics.PorterDuff.Mode.MULTIPLY);
                image.setBackgroundColor(colors[1]);
                  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Window window = getWindow();
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                        window.setStatusBarColor(colors[2]);
                  }
                  if(getSupportActionBar()!=null) {
                      getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Menemen.adjustAlpha(colors[3], 0.4f)));
                      toolbar.setTitleTextColor(colors[4]);
                      toolbar.setSubtitleTextColor(Menemen.adjustAlpha(colors[4], 0.7f));
                  }
            }

            super.onPostExecute(colors);
        }
    }
    private void loadcomments(){
        if(!sql.isHistoryExist(imageurl, m.getUsername())){
            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest postRequest = new StringRequest(Request.Method.POST, RadyoMenemenPro.GET_COMMENT_CAPS,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(final String response) {
                            new AsyncTask<Void,Void,Void>(){
                                @Override
                                protected Void doInBackground(Void... voids) {
                                    try {
                                        String id,nick,post,time;
                                        JSONArray arr = new JSONObject(response).getJSONArray("mesajlar");
                                        JSONObject c;
                                        for(int i = 0;i<arr.getJSONArray(0).length();i++){
                                            JSONArray innerJarr = arr.getJSONArray(0);
                                            c = innerJarr.getJSONObject(i);
                                            id = c.getString("id");
                                            nick = c.getString("nick");
                                            post = c.getString("post");
                                            time = c.getString("time");
                                            //db ye ekle
                                            sql.addtoHistory(new capsDB.CAPS(id,imageurl,nick,post,time));
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Void aVoid) {
                                    try {
                                        String firscomment = m.getCapsDB().getFirstComment(imageurl);
                                        if(getSupportActionBar() != null && firscomment != null) {
                                            getSupportActionBar().setTitle(m.getChatDB().getCapsUploader(imageurl));
                                            firscomment = firscomment.replaceAll(m.getChatDB().getCapsUploader(imageurl) + ": ","");
                                            getSupportActionBar().setSubtitle(firscomment);
                                        }
                                    } catch (Exception e) {
                                        getSupportActionBar().setSubtitle("");
                                        e.printStackTrace();
                                    }
                                    super.onPostExecute(aVoid);
                                }
                            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                    },null){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    HashMap<String, String> dataToSend = new HashMap<>();
                    dataToSend.put("capsurl", imageurl);
                    return dataToSend;
                }

                @Override
                public Priority getPriority() {
                    if(m.isConnectedWifi())
                        return Priority.IMMEDIATE;
                    else
                        return Priority.NORMAL;
                }
            };
            queue.add(postRequest);
        }
    }
           }