package com.eaglesakura.material.widget.support;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

/**
 * 通知バーの制御
 */
public class SupportNotification {

    @NonNull
    final Context mContext;

    @NonNull
    final Notification mNotification;

    @NonNull
    final NotificationManager mNotificationManager;

    /**
     * 通知ID
     */
    final int mNotificationId;

    public SupportNotification(@NonNull Context context, @NonNull Notification notification, int notificationId) {
        mContext = context;
        mNotification = notification;
        mNotificationId = notificationId;
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * コンテンツを更新する
     */
    public SupportNotification content(RemoteViews views) {
        mNotification.contentView = views;
        return this;
    }

    /**
     * Notificationを適用する
     */
    public void appy() {
        mNotificationManager.notify(mNotificationId, mNotification);
    }

    /**
     * 通知をキャンセルする
     */
    public void cancel() {
        mNotificationManager.cancel(mNotificationId);
    }
}
