package com.eaglesakura.android.system;

import com.eaglesakura.android.framework.delegate.lifecycle.LifecycleDelegate;

import android.content.Context;
import android.os.PowerManager;
import android.support.annotation.NonNull;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * CPUのウェイクアップ制御を行う
 */
public class CpuWakeDelegate {

    /**
     * CPU稼働保証を行うカウント
     */
    private final AtomicInteger mWakeUpRef = new AtomicInteger(0);

    @NonNull
    private final PowerManager mPowerManager;

    @NonNull
    private PowerManager.WakeLock wakeLock;

    public CpuWakeDelegate(@NonNull Context context, @NonNull LifecycleDelegate lifecycle) {

        mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        lifecycle.getSubscription().getObservable().subscribe(it -> {
            switch (it.getState()) {
                case OnDestroyed:
                    break;
            }

        });
    }

    /**
     * CPU稼働参照を減らす
     */
    public void popCpuWakeup() {
        if (mWakeUpRef.decrementAndGet() == 0) {
            wakeLock.release();
            wakeLock = null;
        }
    }

    /**
     * CPU稼働参照を増やす
     */
    public void pushCpuWakeup() {
        if (mWakeUpRef.incrementAndGet() == 1) {
            wakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getSimpleName());
            wakeLock.acquire();
        }
    }

    protected void onDestroyed() {
        synchronized (this) {
            // 強制的にCPU稼働を停止させる
            if (wakeLock != null) {
                wakeLock.release();
                wakeLock = null;
                mWakeUpRef.set(0);
            }
        }
    }
}
