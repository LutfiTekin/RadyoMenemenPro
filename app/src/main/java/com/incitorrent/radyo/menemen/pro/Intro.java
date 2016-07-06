package com.incitorrent.radyo.menemen.pro;


import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;


import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

public class Intro extends AppIntro {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       final int background_color = ContextCompat.getColor(this,R.color.colorBackgroundsoft);
       final int accent_color = ContextCompat.getColor(this,R.color.colorAccent);

        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_slide_music_title), getString(R.string.intro_slide_music_descr), R.drawable.intro_slide_play, background_color));
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_slide_chat_title),getString(R.string.intro_slide_chat_descr),R.drawable.intro_slide_chat,accent_color));
        addSlide(AppIntroFragment.newInstance(getString(R.string.podcast), getString(R.string.intro_slide_podcast_descr), R.drawable.podcast, background_color));
        addSlide(AppIntroFragment.newInstance(getString(R.string.news), getString(R.string.intro_slide_news_descr),R.drawable.announce,accent_color));
        addSlide(AppIntroFragment.newInstance(getString(R.string.nav_haykir), getString(R.string.intro_slide_haykir_descr),R.drawable.intro_slide_haykir,background_color));

        setVibrate(true);
        setVibrateIntensity(30);
        setSkipText(getString(R.string.skip));
        setDoneText(getString(R.string.done));
        setZoomAnimation();


    }
    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        startActivity(new Intent(this,MainActivity.class));
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        startActivity(new Intent(this,MainActivity.class));
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }

}