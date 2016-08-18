package com.eaglesakura.android.graphics.loader;

import com.eaglesakura.android.graphics.ImageCacheManager;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.res.ResourcesCompat;

public class DrawableImageLoader extends ImageLoader<DrawableImageLoader> {

    @DrawableRes
    final int mDrawableId;

    @ColorInt
    Integer mTintColor;

    public DrawableImageLoader(@NonNull Context context, @DrawableRes int drawableId, @NonNull ImageCacheManager imageManager) {
        super(context, imageManager);
        mDrawableId = drawableId;
    }

    public DrawableImageLoader tint(@ColorInt int color) {
        mTintColor = color;
        return this;
    }

    @Override
    protected String getUniqueId() {
        return String.valueOf(mDrawableId) + "@" + mTintColor;
    }

    @NonNull
    @Override
    protected Object onLoad() throws Throwable {
        Drawable drawable = ResourcesCompat.getDrawable(mContext.getResources(), mDrawableId, mContext.getTheme());
        if (mTintColor != null && drawable instanceof VectorDrawableCompat) {
            ((VectorDrawableCompat) drawable).setTint(mTintColor);
        }
        return drawable;
    }
}
