package com.eaglesakura.android.util;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;

public class ResourceUtil {
    @ColorInt
    public static int argb(@NonNull Context context, @ColorRes int colorId) {
        return ResourcesCompat.getColor(context.getResources(), colorId, context.getTheme());
    }

    @NonNull
    public static Drawable drawable(@NonNull Context context, @DrawableRes int drawableId) {
        if (drawableId == 0) {
            return null;
        }
        return ResourcesCompat.getDrawable(context.getResources(), drawableId, context.getTheme());
    }

    @NonNull
    public static VectorDrawableCompat vectorDrawable(@NonNull Context context, @DrawableRes int drawableId) {
        return vectorDrawable(context, drawableId, 0);
    }

    @NonNull
    public static VectorDrawableCompat vectorDrawable(@NonNull Context context, @DrawableRes int drawableId, @ColorRes int colorId) {
        VectorDrawableCompat drawableCompat = VectorDrawableCompat.create(context.getResources(), drawableId, context.getTheme());
        if (colorId != 0) {
            drawableCompat.setTint(ContextCompat.getColor(context, colorId));
        }
        return drawableCompat;
    }
}
