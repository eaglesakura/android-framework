package com.eaglesakura.android.util;


import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;

public class ResourceUtil {
    public static int argb(@NonNull Context context, @ColorRes int colorId) {
        return ResourcesCompat.getColor(context.getResources(), colorId, context.getTheme());
    }
}
