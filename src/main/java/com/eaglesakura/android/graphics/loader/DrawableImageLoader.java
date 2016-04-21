package com.eaglesakura.android.graphics.loader;

import com.eaglesakura.android.graphics.ImageCacheManager;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;

public class DrawableImageLoader extends ImageLoader<DrawableImageLoader> {

    @DrawableRes
    final int mDrawableId;

    public DrawableImageLoader(@NonNull Context context, @DrawableRes int drawableId, @NonNull ImageCacheManager imageManager) {
        super(context, imageManager);
        mDrawableId = drawableId;
    }

    @Override
    protected String getUniqueId() {
        return String.valueOf(mDrawableId);
    }

    @NonNull
    @Override
    protected Object onLoad() throws Throwable {
        return ResourcesCompat.getDrawable(mContext.getResources(), mDrawableId, mContext.getTheme());
    }
}
