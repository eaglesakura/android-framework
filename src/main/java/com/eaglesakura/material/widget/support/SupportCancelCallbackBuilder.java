package com.eaglesakura.material.widget.support;

import com.eaglesakura.android.net.NetworkConnector;
import com.eaglesakura.android.net.Result;
import com.eaglesakura.android.rx.BackgroundTask;
import com.eaglesakura.android.rx.PendingCallbackQueue;
import com.eaglesakura.lambda.CallbackUtils;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.material.widget.adapter.CardAdapter;
import com.eaglesakura.util.Timer;

import android.app.Dialog;

import java.util.concurrent.TimeUnit;

/**
 * フレームワークが要求するキャンセルチェックオブジェクトを生成する
 */
public class SupportCancelCallbackBuilder {
    /**
     * 現在のキャンセルオブジェクト
     */
    CancelCallback mCancelCallback;

    SupportCancelCallbackBuilder(CancelCallback cancelCallback) {
        mCancelCallback = cancelCallback;
    }


    /**
     * タイムアウト時間を設定する
     *
     * @param time 値
     * @param unit 単位
     */
    public SupportCancelCallbackBuilder andTimeout(long time, TimeUnit unit) {
        return and(timeout(time, unit));
    }

    /**
     * タイムアウト時間を設定する
     *
     * @param time 値
     * @param unit 単位
     */
    public SupportCancelCallbackBuilder orTimeout(long time, TimeUnit unit) {
        return or(timeout(time, unit));
    }

    /**
     * RecyclerViewのCard状態とリンクする
     *
     * @param bind 対象card
     */
    public SupportCancelCallbackBuilder and(CardAdapter.CardBind bind) {
        return and(as(bind));
    }

    /**
     * RecyclerViewのCard状態とリンクする
     *
     * @param bind 対象card
     */
    public SupportCancelCallbackBuilder or(CardAdapter.CardBind bind) {
        return or(as(bind));
    }

    /**
     * ダイアログの可視状態とリンクする
     */
    public SupportCancelCallbackBuilder and(Dialog dialog) {
        return and(as(dialog));
    }

    /**
     * ダイアログの可視状態とリンクする
     */
    public SupportCancelCallbackBuilder or(Dialog dialog) {
        return or(as(dialog));
    }

    /**
     * AND条件を追加する
     *
     * @param next 同時に満たさなければならないキャンセル条件
     */
    public SupportCancelCallbackBuilder and(CancelCallback next) {
        CancelCallback current = this.mCancelCallback;
        mCancelCallback = () -> CallbackUtils.isCanceled(next) && CallbackUtils.isCanceled(current);
        return this;
    }

    /**
     * OR条件を追加する
     *
     * @param next どちらかを満たせば良いキャンセル条件
     */
    public SupportCancelCallbackBuilder or(CancelCallback next) {
        CancelCallback current = this.mCancelCallback;
        mCancelCallback = () -> CallbackUtils.isCanceled(next) || CallbackUtils.isCanceled(current);
        return this;
    }

    /**
     * キャンセルチェック用オブジェクトを生成する
     */
    public CancelChecker build() {
        return new CancelChecker(mCancelCallback);
    }

    static CancelCallback as(BackgroundTask task) {
        return () -> task.isCanceled();
    }

    /**
     * ダイアログの可視状態とリンクする
     */
    static CancelCallback as(Dialog dialog) {
        return () -> !dialog.isShowing();
    }

    /**
     * カード状態とリンクする
     */
    static CancelCallback as(CardAdapter.CardBind bind) {
        return () -> !bind.isBinded();
    }

    static CancelCallback timeout(long time, TimeUnit unit) {
        final long timeout = unit.toMillis(time);
        return new CancelCallback() {
            Timer mTimer = new Timer();

            @Override
            public boolean isCanceled() throws Throwable {
                return (mTimer.end() > timeout);
            }
        };
    }

    public static SupportCancelCallbackBuilder from(BackgroundTask task) {
        return new SupportCancelCallbackBuilder(as(task));
    }

    public static SupportCancelCallbackBuilder from(CancelCallback cancelCallback) {
        return new SupportCancelCallbackBuilder(cancelCallback);
    }

    public static SupportCancelCallbackBuilder from(CardAdapter.CardBind bind) {
        return new SupportCancelCallbackBuilder(as(bind));
    }

    public static SupportCancelCallbackBuilder from(Dialog dialog) {
        return new SupportCancelCallbackBuilder(as(dialog));
    }

    public class CancelChecker implements
            CancelCallback,
            BackgroundTask.Signal,
            PendingCallbackQueue.CancelCallback,
            NetworkConnector.CancelCallback {

        CancelCallback mCancelCallback;

        CancelChecker(CancelCallback cancelCallback) {
            mCancelCallback = cancelCallback;
        }

        @Override
        public boolean is(BackgroundTask task) {
            return CallbackUtils.isCanceled(mCancelCallback);
        }

        @Override
        public boolean isCanceled(Result connection) {
            return CallbackUtils.isCanceled(mCancelCallback);
        }

        @Override
        public boolean isCanceled() throws Throwable {
            return CallbackUtils.isCanceled(mCancelCallback);
        }
    }
}
