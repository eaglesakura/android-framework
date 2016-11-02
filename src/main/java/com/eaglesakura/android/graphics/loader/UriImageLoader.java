package com.eaglesakura.android.graphics.loader;

import com.eaglesakura.android.graphics.ImageCacheManager;
import com.eaglesakura.io.CancelableInputStream;
import com.eaglesakura.util.IOUtil;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;

public class UriImageLoader extends ImageLoader<UriImageLoader> {

    @NonNull
    final Uri mUri;

    public UriImageLoader(@NonNull Context context, @NonNull ImageCacheManager imageManager, @NonNull Uri uri) {
        super(context, imageManager);
        mUri = uri;
    }

    @NonNull
    @Override
    protected String getUniqueId() {
        return "uri=" + mUri.toString();
    }

    @NonNull
    @Override
    protected Object onLoad() throws Throwable {
        CancelableInputStream stream = new CancelableInputStream(mContext.getContentResolver().openInputStream(mUri), mCancelCallback);
        stream.setBufferSize(1024 * 256);   // 画像ファイルのため、バッファサイズを大きく取る
        try {
            return BitmapFactory.decodeStream(stream);
        } finally {
            IOUtil.close(stream);
        }
    }
}
