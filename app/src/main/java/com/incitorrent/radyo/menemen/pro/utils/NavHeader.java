package com.incitorrent.radyo.menemen.pro.utils;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.incitorrent.radyo.menemen.pro.R;
import com.incitorrent.radyo.menemen.pro.RadyoMenemenPro;



/**
 * RadyoMenemenPro Created by lutfi on 7.01.2017.
 */

public class NavHeader {
    String header_text,header_sub,header_image_url;
    View header_view;


    public NavHeader(){
    }

    public NavHeader setTitle(@Nullable String header_text){
        this.header_text = header_text;
        return this;
    }

    public NavHeader setSubTitle(@Nullable String header_sub){
        this.header_sub = header_sub;
        return this;
    }

    public NavHeader setImage(@Nullable String header_image_url){
        this.header_image_url = header_image_url;
        return this;
    }

    public NavHeader setHeaderView(View view){
        this.header_view = view;
        return this;
    }

    public TextView getHeaderTextView(){
        return (TextView) header_view.findViewById(R.id.header_txt);
    }

    public TextView getSubHeaderTextView(){
        return (TextView) header_view.findViewById(R.id.header_sub_txt);
    }

    public ImageView getHeaderImage(){
        return (ImageView) header_view.findViewById(R.id.header_img);
    }

    public void clear(){
        if(header_view == null) return;
        ImageView header_img = (ImageView) header_view.findViewById(R.id.header_img);
        TextView header_txt = (TextView) header_view.findViewById(R.id.header_txt);
        TextView header_sub_txt = (TextView) header_view.findViewById(R.id.header_sub_txt);
        header_img.setImageDrawable(null);
        Context context = header_view.getContext().getApplicationContext();
        Menemen m = new Menemen(context);
        if(m.isLoggedIn())
            header_txt.setText(m.getUsername().toUpperCase());
        else header_txt.setText(context.getString(R.string.app_name));
        header_sub_txt.setText(R.string.site_adress);
    }

    public void build(){
        Context context = header_view.getContext().getApplicationContext();
        ImageView header_img = (ImageView) header_view.findViewById(R.id.header_img);
        TextView header_txt = (TextView) header_view.findViewById(R.id.header_txt);
        TextView header_sub_txt = (TextView) header_view.findViewById(R.id.header_sub_txt);
        if (header_txt != null)
            header_txt.setText(header_text);
    if (header_sub_txt != null)
        header_sub_txt.setText(header_sub);
            if(header_image_url != null)
                Glide.with(context)
                        .load(header_image_url)
                        .dontAnimate()
                        .override(RadyoMenemenPro.ARTWORK_IMAGE_OVERRIDE_DIM, RadyoMenemenPro.ARTWORK_IMAGE_OVERRIDE_DIM)
                        .into(header_img);
    }

}
