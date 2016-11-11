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
import android.support.v4.graphics.drawable.DrawableCompat;

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
        try {
            return ResourcesCompat.getDrawable(context.getResources(), drawableId, context.getTheme());
        } catch (Exception e) {
            return vectorDrawable(context, drawableId);
        }
    }

    @NonNull
    public static VectorDrawableCompat vectorDrawable(@NonNull Context context, @DrawableRes int drawableId) {
        return vectorDrawable(context, drawableId, 0);
    }

    @NonNull
    public static VectorDrawableCompat vectorDrawable(@NonNull Context context, @DrawableRes int drawableId, @ColorRes int colorId) {
        VectorDrawableCompat drawableCompat = VectorDrawableCompat.create(context.getResources(), drawableId, context.getTheme());
        if (colorId != 0) {
            DrawableCompat.setTint(drawableCompat, ContextCompat.getColor(context, colorId));
        }
        return drawableCompat;
    }
}
