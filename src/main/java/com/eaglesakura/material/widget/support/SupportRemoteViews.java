package com.eaglesakura.material.widget.support;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.RemoteViews;

import java.util.HashMap;
import java.util.Map;

/**
 * RemoteViewsをラップする
 */
public class SupportRemoteViews {

    final Context mContext;

    final RemoteViews mRemoteViews;

    final String mInternalAction;

    static final String EXTRA_VIEW_ID = "EXTRA_VIEW_ID";

    Map<Integer, OnRemoteViewClickListener> mListenerMap = new HashMap<>();

    public SupportRemoteViews(Context context, RemoteViews remoteViews) {
        mContext = context;
        mRemoteViews = remoteViews;
        mInternalAction = context.getPackageName() + ".ACTION_SUPPORT_REMOTE_VIEWS." + hashCode();
        mContext.registerReceiver(mInternalReceiver, new IntentFilter(mInternalAction));
    }

    public RemoteViews getRemoteViews() {
        return mRemoteViews;
    }

    /**
     * リスナを設定する
     * プロセスが生きていることが前提となるため、Service+Foreground等の組み合わせが前提となる。
     */
    public SupportRemoteViews setOnClickListener(int viewId, OnRemoteViewClickListener listener) {
        Intent rawIntent = new Intent(mInternalAction)
                .putExtra(EXTRA_VIEW_ID, viewId)
                .setPackage(mContext.getPackageName());
        PendingIntent intent = PendingIntent.getBroadcast(mContext, viewId,
                rawIntent,
                0x00);
        mRemoteViews.setOnClickPendingIntent(viewId, intent);
        mListenerMap.put(viewId, listener);
        return this;
    }

    /**
     * 管理しているリソースを削除する
     */
    public void dispose() {
        try {
            mContext.unregisterReceiver(mInternalReceiver);
        } catch (Exception e) {

        }
    }

    public interface OnRemoteViewClickListener {
        void onClick(SupportRemoteViews self, int viewId);
    }

    final BroadcastReceiver mInternalReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int viewId = intent.getIntExtra(EXTRA_VIEW_ID, 0);
            OnRemoteViewClickListener listener = mListenerMap.get(viewId);
            if (listener != null) {
                listener.onClick(SupportRemoteViews.this, viewId);
            }
        }
    };
}
