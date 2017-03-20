package com.jhonn_aj.descubreperuandroid.app.utils;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.jhonn_aj.descubreperuandroid.R;

/**
 * Created by jhonn_aj on 19/03/2017.
 */

public class GlideUtil {
    public static void loadImage(String url, ImageView imageView) {
        Context context = imageView.getContext();
        ColorDrawable cd = new ColorDrawable(ContextCompat.getColor(context, R.color.colorBlue));
        Glide.with(context)
                .load(url)
                .placeholder(cd)
                .crossFade()
                .centerCrop()
                .into(imageView);
    }

    public static void loadProfileIcon(String url, ImageView imageView) {
        Context context = imageView.getContext();
        Glide.with(context)
                .load(url)
                .placeholder(R.mipmap.ic_launcher)
                .dontAnimate()
                .fitCenter()
                .into(imageView);
    }
}
