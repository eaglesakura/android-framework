package com.eaglesakura.android.graphics;

import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;

/**
 *
 */
public class ImageCacheManager {

    /**
     * キャッシュコレクション
     */
    LruCache<String, Drawable> mCaches;

    /**
     * キャッシュコントロールを行う
     */
    public ImageCacheManager(@IntRange(from = 1) int cacheImageNum) {
        mCaches = new LruCache<>(cacheImageNum);
    }

    /**
     * @param id キャッシュ識別ID
     */
    @Nullable
    public Drawable getCache(String id) {
        return mCaches.get(id);
    }


    /**
     * キャッシュを追加する
     */
    public void putCache(@NonNull String id, @NonNull Drawable item) {
        mCaches.put(id, item);
    }
}
