package com.eaglesakura.android.graphics;

import com.eaglesakura.android.util.AndroidThreadUtil;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Transformation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * スクロール等で大量にタスクを積むとOOMの原因となるため一旦非推奨とする
 */
@Deprecated
public class PicassoImageLoader {
    @NonNull
    final RequestCreator mRequestCreator;

    @NonNull
    final Context mContext;

    Drawable mErrorDrawable;

    @DrawableRes
    int mErrorDrawableId;

    PicassoImageLoader(Context context, RequestCreator requestCreator) {
        mRequestCreator = requestCreator;
        mContext = context;
    }

    /**
     * キャンセルタイムアウトが発生するまで画像の取得待ちを行う
     */
    @Nullable
    public Drawable await() throws Throwable {
        AndroidThreadUtil.assertBackgroundThread();

        try {
            Bitmap bitmap = mRequestCreator.get();
            if (bitmap != null) {
                return new BitmapDrawable(mContext.getResources(), bitmap);
            }
        } catch (IOException e) {
        }

        if (mErrorDrawable != null) {
            return mErrorDrawable;
        } else if (mErrorDrawableId != 0) {
            return ResourcesCompat.getDrawable(mContext.getResources(), mErrorDrawableId, mContext.getTheme());
        }

        throw new FileNotFoundException();
    }

    public PicassoImageLoader error(@DrawableRes int errorResId) {
        mRequestCreator.error(errorResId);
        mErrorDrawableId = errorResId;
        return this;
    }

    public PicassoImageLoader error(Drawable errorDrawable) {
        mRequestCreator.error(errorDrawable);
        mErrorDrawable = errorDrawable;
        return this;
    }

    public PicassoImageLoader fit() {
        mRequestCreator.fit();
        return this;
    }

    public PicassoImageLoader resize(int targetWidth, int targetHeight) {
        mRequestCreator.resize(targetWidth, targetHeight);
        return this;
    }

    public PicassoImageLoader resizeDimen(int targetWidthResId, int targetHeightResId) {
        mRequestCreator.resizeDimen(targetWidthResId, targetHeightResId);
        return this;
    }

    public PicassoImageLoader centerInside() {
        mRequestCreator.centerInside();
        return this;
    }

    public PicassoImageLoader centerCrop() {
        mRequestCreator.centerCrop();
        return this;
    }

    public PicassoImageLoader rotate(float degrees, float pivotX, float pivotY) {
        mRequestCreator.rotate(degrees, pivotX, pivotY);
        return this;
    }

    public PicassoImageLoader rotate(float degrees) {
        mRequestCreator.rotate(degrees);
        return this;
    }

    public PicassoImageLoader transform(Transformation transformation) {
        mRequestCreator.transform(transformation);
        return this;
    }

    public PicassoImageLoader config(Bitmap.Config config) {
        mRequestCreator.config(config);
        return this;
    }

    public PicassoImageLoader noFade() {
        mRequestCreator.noFade();
        return this;
    }

    public PicassoImageLoader skipMemoryCache() {
        mRequestCreator.skipMemoryCache();
        return this;
    }

    public static PicassoImageLoader create(Context context, File file) {
        RequestCreator load = Picasso.with(context.getApplicationContext()).load(file);
        return new PicassoImageLoader(context.getApplicationContext(), load);
    }
}
