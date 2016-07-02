package com.incitorrent.radyo.menemen.pro;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

public class Intro extends AppIntro {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addSlide(AppIntroFragment.newInstance("Deneme", "açıklama deneme", R.drawable.ic_menu_settings, Color.parseColor("#757575")));
        addSlide(AppIntroFragment.newInstance("Deneme", "açıklama deneme", R.drawable.powered_by_google_dark, Color.parseColor("#757575")));

        setVibrate(true);
        setVibrateIntensity(30);
        setDepthAnimation();
        setDoneText(getString(android.R.string.ok));
        setSkipText(getString(R.string.skip));
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
