package com.eaglesakura.android.graphics;

import com.eaglesakura.android.graphics.loader.DrawableImageLoader;
import com.eaglesakura.android.graphics.loader.FileImageLoader;
import com.eaglesakura.android.graphics.loader.ImageLoader;
import com.eaglesakura.android.graphics.loader.NetworkImageLoader;
import com.eaglesakura.android.net.NetworkConnector;
import com.eaglesakura.android.net.request.ConnectRequest;
import com.eaglesakura.android.net.request.SimpleHttpRequest;
import com.eaglesakura.lambda.CancelCallback;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;

/**
 * キャッシュコントロールと割り込みに対応したイメージローダ
 */
public class CachedImageLoader {
    @NonNull
    final Context mContext;

    /**
     * エラー画像用の画像キャッシュ
     */
    final ImageCacheManager mErrorCache;

    /**
     * 成功した画像用の画像キャッシュ
     */
    final ImageCacheManager mImageCache;

    public CachedImageLoader(Context context) {
        this(context, 8, 4);
    }

    public CachedImageLoader(Context context, @IntRange(from = 1) int imageCacheNum, @IntRange(from = 1) int errorCacheNum) {
        mContext = context.getApplicationContext();
        mImageCache = new ImageCacheManager(imageCacheNum);
        mErrorCache = new ImageCacheManager(errorCacheNum);
    }

    /**
     * ファイルからのローダーを生成する
     */
    public Builder newImage(File src, boolean onNemoryCache) {
        FileImageLoader loader = new FileImageLoader(mContext, mImageCache, src);
        if (onNemoryCache) {
            loader.cache();
        }

        return new Builder(loader);
    }

    /**
     * ネットワークダウンローダーを生成する
     */
    public Builder newImage(NetworkConnector connector, String url, boolean onMemoryCache) {
        SimpleHttpRequest request = new SimpleHttpRequest(ConnectRequest.Method.GET);
        request.setUrl(url, null);
        return newImage(connector, request, onMemoryCache);
    }

    /**
     * ネットワークダウンローダーを生成する
     */
    public Builder newImage(NetworkConnector connector, ConnectRequest request, boolean onMemoryCache) {
        NetworkImageLoader loader = new NetworkImageLoader(mContext, mImageCache, connector, request);
        if (onMemoryCache) {
            loader.cache();
        }
        return new Builder(loader);
    }

    public class Builder {
        ImageLoader mErrorLoader;

        ImageLoader mLoader;

        public Builder(ImageLoader loader) {
            mLoader = loader;
        }

        /**
         * エラー時の代替画像を指定する
         */
        public Builder errorImage(@DrawableRes int drawableId, boolean cached) {
            mErrorLoader = new DrawableImageLoader(mContext, drawableId, mErrorCache);
            if (cached) {
                mErrorLoader.cache();
            }
            return this;
        }

        public ImageLoader getErrorLoader() {
            return mErrorLoader;
        }

        public ImageLoader getLoader() {
            return mLoader;
        }

        /**
         * アスペクト比を保ってリサイズする
         */
        public Builder keepAspectResize(int width, int height) {
            mLoader.keepAspectResize(width, height);
            if (mErrorLoader != null) {
                mErrorLoader.keepAspectResize(width, height);
            }
            return this;
        }

        /**
         * 画像の取得待ちを行う
         */
        public Drawable await(@NonNull CancelCallback cancelCallback) throws IOException {
            if (cancelCallback == null) {
                throw new IllegalArgumentException();
            }
            mLoader.setCancelCallback(cancelCallback);
            if (mErrorLoader != null) {
                mErrorLoader.setCancelCallback(cancelCallback);
            }

            try {
                return mLoader.await();
            } catch (InterruptedIOException e) {
                throw e;
            } catch (IOException e) {
                if (mErrorLoader != null) {
                    return mErrorLoader.await();
                } else {
                    throw e;
                }
            }
        }
    }
}
