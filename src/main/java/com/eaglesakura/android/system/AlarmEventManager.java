package com.eaglesakura.android.system;

import com.eaglesakura.android.framework.delegate.lifecycle.LifecycleDelegate;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

/**
 * AlarmManagerによる定時コールバックを制御する
 *
 * コールバック中はCPUが起きていることが保証されるが、それをキーにService等を起動した場合は別途CPU WakeUpが必要となる（保証外となる）
 */
public class AlarmEventManager {
    /**
     * 呼び出しリクエストを指定する
     */
    private static final String EXTRA_WAKEUP_REQUEST_CODE = "EXTRA_WAKEUP_REQUEST_CODE";

    /**
     * 呼び出しリクエストの引数を指定する
     */
    private static final String EXTRA_WAKEUP_REQUEST_ARGMENTS = "EXTRA_WAKEUP_REQUEST_ARGMENTS";

    /**
     * アラームを作成した時刻
     */
    private static final String EXTRA_WAKEUP_REQUEST_ALARM_TIME = "EXTRA_WAKEUP_REQUEST_ALARM_TIME";

    private String ACTION_SELF_WAKEUP_BROADCAST;

    @NonNull
    private final AlarmManager mAlarmManager;

    @NonNull
    private final Context mContext;

    @NonNull
    private final Callback mCallback;

    public interface Callback {
        /**
         * AlarmManagerによってCPUが叩き起こされたタイミングで呼び出される
         * <br>
         * このメソッドは必ずonReceiveの中で呼び出されることを保証する。
         *
         * @param self            呼び出し元
         * @param requestCode     呼び出しリクエスト
         * @param requestArgments コールバックに呼び出される引数
         * @param delayedTimeMs   設定から実際に遅延した時間
         */
        void onAlarmReceived(AlarmEventManager self, int requestCode, Bundle requestArgments, long delayedTimeMs);
    }

    public AlarmEventManager(Service context, LifecycleDelegate lifecycleDelegate, Callback callback) {
        this(context, lifecycleDelegate, (AlarmManager) context.getSystemService(Context.ALARM_SERVICE), callback);
    }

    public AlarmEventManager(Activity context, LifecycleDelegate lifecycleDelegate, Callback callback) {
        this(context, lifecycleDelegate, (AlarmManager) context.getSystemService(Context.ALARM_SERVICE), callback);
    }

    private AlarmEventManager(Context context, LifecycleDelegate lifecycleDelegate, AlarmManager alarmManager, Callback callback) {
        mContext = context.getApplicationContext();
        mAlarmManager = alarmManager;
        mCallback = callback;

        ACTION_SELF_WAKEUP_BROADCAST = context.getPackageName() + "/" + getClass().getName() + ".ACTION_SELF_WAKEUP_BROADCAST" + "/" + hashCode();
        lifecycleDelegate.getSubscription().getObservable().subscribe(it -> {
            switch (it.getState()) {
                case OnCreated:
                    onCreate();
                    break;
                case OnDestroyed:
                    onDestroy();
                    break;
            }
        });
    }

    /**
     * Systemのアラームに対応する
     */
    private final BroadcastReceiver mWakeupBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int requestCode = intent.getIntExtra(EXTRA_WAKEUP_REQUEST_CODE, 0);
            Bundle argments = intent.getBundleExtra(EXTRA_WAKEUP_REQUEST_ARGMENTS);
            long alarmTime = intent.getLongExtra(EXTRA_WAKEUP_REQUEST_ALARM_TIME, 0);


            final long current = SystemClock.elapsedRealtime();
            mCallback.onAlarmReceived(AlarmEventManager.this, requestCode, argments, current - alarmTime);
        }
    };

    /**
     * 指定したミリ秒後、再度CPUを叩き起こす。
     * <br>
     * 繰り返しには対応しない。
     *
     * @param requestCode     呼び出しリクエスト
     * @param requestArgments コールバックに呼び出される引数
     * @param delayTimeMs     遅延時間
     */
    public void requestNextAlarmDelayed(int requestCode, Bundle requestArgments, long delayTimeMs) {
        requestNextAlarmDelayed(requestCode, requestArgments, delayTimeMs, true);
    }


    /**
     * 指定したミリ秒後、再度CPUを叩き起こす。
     * <br>
     * 繰り返しには対応しない。
     *
     * @param requestCode     呼び出しリクエスト
     * @param requestArgments コールバックに呼び出される引数
     * @param delayTimeMs     遅延時間
     * @param extract         時間保証を有効にする場合はtrue
     */
    @SuppressLint("NewApi")
    public void requestNextAlarmDelayed(int requestCode, Bundle requestArgments, long delayTimeMs, boolean extract) {
        Intent intent = new Intent(ACTION_SELF_WAKEUP_BROADCAST);
        intent.putExtra(EXTRA_WAKEUP_REQUEST_CODE, requestCode);
        if (requestArgments != null) {
            intent.putExtra(EXTRA_WAKEUP_REQUEST_ARGMENTS, requestArgments);
        }
        final long current = SystemClock.elapsedRealtime();
        intent.putExtra(EXTRA_WAKEUP_REQUEST_ALARM_TIME, current);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                mContext, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT
        );

        if (extract && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mAlarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, current + delayTimeMs, pendingIntent);
        } else {
            mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, current + delayTimeMs, pendingIntent);
        }
    }

    /**
     * セットしてあるアラームを解除する
     */
    public void cancelAlarm(int requestCode) {
        Intent intent = new Intent(ACTION_SELF_WAKEUP_BROADCAST);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                mContext, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT
        );
        mAlarmManager.cancel(pendingIntent);
    }

    @UiThread
    @CallSuper
    protected void onCreate() {
        mContext.registerReceiver(mWakeupBroadcastReceiver, new IntentFilter(ACTION_SELF_WAKEUP_BROADCAST));
    }

    @UiThread
    @CallSuper
    protected void onDestroy() {
        mContext.unregisterReceiver(mWakeupBroadcastReceiver);
    }
}
