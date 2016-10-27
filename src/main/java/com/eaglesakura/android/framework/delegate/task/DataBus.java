package com.eaglesakura.android.framework.delegate.task;

import com.eaglesakura.android.framework.delegate.lifecycle.LifecycleDelegate;
import com.eaglesakura.android.framework.util.AppSupportUtil;
import com.eaglesakura.android.rx.BackgroundTask;
import com.eaglesakura.android.rx.BackgroundTaskBuilder;
import com.eaglesakura.android.rx.CallbackTime;
import com.eaglesakura.android.rx.ExecuteTarget;
import com.eaglesakura.android.rx.error.TaskCanceledException;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.android.util.AndroidThreadUtil;
import com.eaglesakura.lambda.Action1;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.util.Util;
import com.squareup.otto.Bus;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.eaglesakura.android.framework.util.AppSupportUtil.assertNotCanceled;

/**
 * Nullを許容したデータバスを構築する
 *
 * データハンドリングは必ずUIスレッドで行われる。
 * Nullを許容するため、DataBusそれ自体をpostする。
 * Subscribe側は、DataBusをextendsしたクラスを引数にしてデータを受け取る
 *
 * ex. void onModified(ExampleDataBus bus){}
 */
public abstract class DataBus<DataType> {
    @Nullable
    Handler mHandler = UIHandler.getInstance();

    @NonNull
    final Bus mBus = new Bus() {
        @Override
        public void post(Object event) {
            if (mHandler == null || AndroidThreadUtil.isHandlerThread(mHandler)) {
                // ハンドラ設定がない、もしくは所属しているハンドラのスレッドであればすぐさま実行
                super.post(event);
            } else {
                mHandler.post(() -> super.post(event));
            }
        }
    };

    @Nullable
    DataType mData;

    public DataBus() {
    }

    public DataBus(@Nullable DataType data) {
        mData = data;
    }

    public DataBus(@Nullable Handler handler, @Nullable DataType data) {
        mHandler = handler;
        mData = data;
    }

    public DataBus(@Nullable Handler handler) {
        mHandler = handler;
    }

    /**
     * ハンドリング対象のハンドラを設定する。
     * !=nullである場合、そのハンドラのスレッドでpost処理が行われる。
     * nullである場合、即座にpost処理が行われる。
     */
    public void setHandler(@Nullable Handler handler) {
        mHandler = handler;
    }

    public Bus getBus() {
        return mBus;
    }

    /**
     * イベントバスに対してレシーバを登録する
     */
    public void register(Object object) {
        mBus.register(object);
    }

    /**
     * イベントバスからレシーバを削除する
     */
    public void unregister(Object object) {
        mBus.unregister(object);
    }

    /**
     * 握っているデータを取得する
     */
    @Nullable
    public DataType getData() {
        return mData;
    }

    /**
     * データを持っている場合true
     */
    public boolean hasData() {
        return getData() != null;
    }

    /**
     * オブジェクトの変更通知を行なう
     */
    public void modified(DataType data) {
        mData = data;
        mBus.post(this);
    }

    /**
     * オブジェクトに変更があったことを通知する
     */
    public void modified() {
        mBus.post(mData);
    }

    /**
     * 持っているデータをクリアする
     */
    public void clear() {
        modified(null);
    }

    /**
     * データを持っている場合処理を実行する
     */
    public void ifPresent(Action1<DataType> action) {
        try {
            DataType data = getData();
            if (data != null) {
                action.action(data);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public <T extends DataBus> T bind(LifecycleDelegate delegate, Object receiver) {
        LifecycleLinkBus.register(delegate, getBus(), receiver);
        return (T) this;
    }

    /**
     * hasData() == trueを満たすまでウェイトをかける。
     */
    public DataType await(CancelCallback cancelCallback) throws TaskCanceledException {
        while (!hasData()) {
            Util.sleep(1);
            assertNotCanceled(cancelCallback);
        }

        return getData();
    }

    /**
     * データの受取待ちを行うタスクを生成する
     */
    public static BackgroundTaskBuilder<List<DataBus>> awaitTask(LifecycleDelegate delegate, ExecuteTarget executeTarget, CallbackTime callbackTime, DataBus... bus) {
        return delegate.async(executeTarget, callbackTime, (BackgroundTask<List<DataBus>> task) -> {
            List<DataBus> list = new ArrayList<>();
            for (DataBus it : bus) {
                list.add(it);
            }
            await(AppSupportUtil.asCancelCallback(task), list);
            return list;
        });
    }

    /**
     * 全てのイベントバスがデータを持つまでウェイトをかける
     */
    public static void await(CancelCallback cancelCallback, DataBus... bus) throws TaskCanceledException {
        List<DataBus> list = new ArrayList<>();
        for (DataBus it : bus) {
            list.add(it);
        }

        await(cancelCallback, bus);
    }

    /**
     * 全てのイベントバスがデータを持つまでウェイトをかける
     */
    public static void await(CancelCallback cancelCallback, List<? extends DataBus> bus) throws TaskCanceledException {
        while (true) {
            int completed = 0;
            for (DataBus it : bus) {
                if (it.hasData()) {
                    ++completed;
                }
            }

            // 全てのデータの生成が完了したので抜ける
            if (bus.size() == completed) {
                return;
            }

            assertNotCanceled(cancelCallback);
            Util.sleep(1);
        }
    }
}
