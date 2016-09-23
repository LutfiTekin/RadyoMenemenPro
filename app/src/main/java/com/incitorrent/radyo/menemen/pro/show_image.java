package com.incitorrent.radyo.menemen.pro;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;
import com.incitorrent.radyo.menemen.pro.utils.TouchImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class show_image extends AppCompatActivity {
    private TouchImageView image;
    private String imageurl;
    private ProgressBar progressBar;
    Menemen m;
    final String root = Environment.getExternalStorageDirectory().toString();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setExitTransition(new Explode());
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);
        m = new Menemen(this);
        progressBar = (ProgressBar) findViewById(R.id.loading);
        image = (TouchImageView) findViewById(R.id.show_image);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            image.setTransitionName("show_image");
        }
        final Intent mintent = getIntent();
        final Uri imageUri = mintent.getData();
        imageurl = imageUri.toString().trim();
        final FloatingActionButton c_fab = (FloatingActionButton) findViewById(R.id.comment_fab);
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
    }


               @Override
               protected void onSaveInstanceState(Bundle outState) {
                   outState.putString("url", imageurl);
                   super.onSaveInstanceState(outState);
               }

               @Override
               protected void onRestoreInstanceState(Bundle savedInstanceState) {
                   if (savedInstanceState != null) imageurl = savedInstanceState.getString("url");
                   super.onRestoreInstanceState(savedInstanceState);
               }

    @Override
    protected void onStart() {
        try {
            Glide.with(show_image.this)
                    .load(Menemen.getThumbnail(imageurl))
                    .into(new SimpleTarget<GlideDrawable>() {
                        @Override
                        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                            image.setImageDrawable(resource);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onStart();
    }

    @Override
               protected void onResume() {
                   super.onResume();
                   try {
                       Glide.with(show_image.this)
                               .load(imageurl)
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
                   String pic = save(false);
                   Uri pictureUri = Uri.parse(pic);
                   Log.v("URI", "BUILD " + pictureUri);
                   Intent shareIntent = new Intent();
                   shareIntent.setAction(Intent.ACTION_SEND);
                   shareIntent.putExtra(Intent.EXTRA_STREAM, pictureUri);
                   shareIntent.setType("image/*");
                   shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                   startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
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
           }

