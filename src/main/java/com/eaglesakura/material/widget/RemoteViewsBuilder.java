package com.eaglesakura.material.widget;

import com.eaglesakura.material.widget.support.SupportRemoteViews;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.widget.RemoteViews;

/**
 * RemoteViewsを構築する
 */
public class RemoteViewsBuilder {

    final Context mContext;

    final RemoteViews mRemoteViews;

    RemoteViewsBuilder(Context context, RemoteViews remoteViews) {
        mContext = context;
        mRemoteViews = remoteViews;
    }

    public SupportRemoteViews build() {
        return new SupportRemoteViews(mContext, mRemoteViews);
    }


    public static RemoteViewsBuilder from(Context context, @LayoutRes int resId) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), resId);
        return new RemoteViewsBuilder(context, remoteViews);
    }
}
