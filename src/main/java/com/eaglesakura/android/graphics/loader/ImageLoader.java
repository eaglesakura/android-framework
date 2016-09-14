package com.eaglesakura.android.graphics.loader;

import com.eaglesakura.android.framework.FwLog;
import com.eaglesakura.android.graphics.ImageCacheManager;
import com.eaglesakura.android.util.ImageUtil;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.util.StringUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.InterruptedIOException;

public abstract class ImageLoader<T extends ImageLoader> {
    @NonNull
    protected final Context mContext;

    /**
     * 管理対象のImageManager
     */
    @NonNull
    protected final ImageCacheManager mImageManager;

    @NonNull
    protected CancelCallback mCancelCallback;

    /**
     * ロード結果をキャッシュを利用する場合true
     *
     * アニメーションのないDrawableをロードする際に有効
     */
    private boolean mCache;

    /**
     * 最大サイズ
     */
    private int mMaxWidth;

    /**
     * 最大サイズ
     */
    private int mMaxHeight;

    public ImageLoader(@NonNull Context context, @NonNull ImageCacheManager imageManager) {
        mContext = context.getApplicationContext();
        mImageManager = imageManager;
    }

    public void setCancelCallback(@NonNull CancelCallback cancelCallback) {
        mCancelCallback = cancelCallback;
    }

    /**
     * キャッシュに登録する場合はtrue
     */
    public T cache() {
        mCache = true;
        return self();
    }

    /**
     * キャッシュ登録する場合はtrue
     */
    public boolean isCache() {
        return mCache;
    }

    /**
     * アスペクト比を保ち、リサイズを行う
     */
    public T keepAspectResize(@IntRange(from = 1) int maxWidth, @IntRange(from = 1) int maxHeight) {
        mMaxHeight = maxHeight;
        mMaxWidth = maxWidth;
        return self();
    }

    /**
     * キャッシュ化するための識別IDを取得する
     */
    @NonNull
    public final String getCacheId() {
        return StringUtil.format("%s,%dx%d,%s", getClass().getName(), mMaxWidth, mMaxHeight, getUniqueId());
    }

    /**
     * 読み込み待ちを行う
     */
    public Drawable await() throws IOException {
        try {
            // キャッシュを読み込む
            String cacheId = getCacheId();
            Drawable cache = mImageManager.getCache(cacheId);
            if (cache != null) {
                FwLog.image("Cache Image id[%s]", cacheId);
                return cache;
            }

            Object image = onLoad();
            if (mCancelCallback.isCanceled()) {
                // キャンセル済
                throw new InterruptedIOException();
            }

            Drawable result;
            if (image instanceof Bitmap) {
                result = new BitmapDrawable(mContext.getResources(), scale((Bitmap) image));
            } else if (image instanceof BitmapDrawable) {
                result = new BitmapDrawable(mContext.getResources(), scale(((BitmapDrawable) image).getBitmap()));
            } else if (image instanceof Drawable) {
                result = (Drawable) image;
            } else {
                // not support
                throw new IllegalStateException("image :: " + image.getClass().getName());
            }

            if (mCache) {
                mImageManager.putCache(cacheId, result);
            }

            return result;
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException(e);
        }
    }

    /**
     * 画像のスケーリングを行う
     */
    @NonNull
    protected Bitmap scale(@NonNull Bitmap raw) {
        if (mMaxWidth <= 0 || mMaxHeight <= 0) {
            return raw;
        } else {
            Bitmap result = ImageUtil.toScaledImage(raw, mMaxWidth, mMaxHeight);
            if (result != raw) {
                raw.recycle();
            }
            return result;
        }
    }

    protected final T self() {
        return (T) this;
    }

    /**
     * Drawableを一意に識別するIDを返却する
     */
    @NonNull
    protected abstract String getUniqueId();

    /**
     * 読込を行う。
     *
     * リサイズ等の処理はsuper側で行うため、読込のみで良い。
     */
    @NonNull
    protected abstract Object onLoad() throws Throwable;
}
