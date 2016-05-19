package com.eaglesakura.android.graphics.loader;

import com.eaglesakura.android.graphics.ImageCacheManager;
import com.eaglesakura.android.net.NetworkConnector;
import com.eaglesakura.android.net.Result;
import com.eaglesakura.android.net.parser.BitmapParser;
import com.eaglesakura.android.net.request.ConnectRequest;
import com.eaglesakura.lambda.CallbackUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;

public class NetworkImageLoader extends ImageLoader<NetworkImageLoader> {
    @NonNull
    final NetworkConnector mNetworkConnector;

    @NonNull
    final ConnectRequest mRequest;

    public NetworkImageLoader(@NonNull Context context, @NonNull ImageCacheManager imageManager, @NonNull NetworkConnector networkConnector, @NonNull ConnectRequest request) {
        super(context, imageManager);
        mNetworkConnector = networkConnector;
        mRequest = request;
    }

    @NonNull
    @Override
    protected String getUniqueId() {
        return mRequest.getCachePolicy().getCacheKey(mRequest);
    }

    @NonNull
    @Override
    protected Object onLoad() throws Throwable {
        Result<Bitmap> result = mNetworkConnector.connect(mRequest, new BitmapParser(), (it) -> CallbackUtils.isCanceled(mCancelCallback));
        return new BitmapDrawable(mNetworkConnector.getContext().getResources(), result.getResult());
    }
}
