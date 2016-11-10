package com.eaglesakura.android.framework.delegate.lifecycle;

import com.eaglesakura.android.rx.BackgroundTask;
import com.eaglesakura.android.rx.BackgroundTaskBuilder;
import com.eaglesakura.android.rx.CallbackTime;
import com.eaglesakura.android.rx.ExecuteTarget;
import com.eaglesakura.android.rx.LifecycleEvent;
import com.eaglesakura.android.rx.LifecycleState;
import com.eaglesakura.android.rx.ObserveTarget;
import com.eaglesakura.android.rx.PendingCallbackQueue;
import com.eaglesakura.android.rx.SubscribeTarget;
import com.eaglesakura.android.rx.event.LifecycleEventImpl;

import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;

public abstract class LifecycleDelegate {

    protected final BehaviorSubject<LifecycleEvent> mLifecycleSubject = BehaviorSubject.create(new LifecycleEventImpl(LifecycleState.NewObject));

    protected final PendingCallbackQueue mCallbackQueue = new PendingCallbackQueue();

    public LifecycleDelegate() {
        mCallbackQueue.bind(mLifecycleSubject);
    }

    /**
     * ライフサイクル状態を取得する
     */
    public LifecycleState getLifecycleState() {
        return mLifecycleSubject.getValue().getState();
    }

    public PendingCallbackQueue getCallbackQueue() {
        return mCallbackQueue;
    }

    public Subscription subscribe(Action1<? super LifecycleEvent> onNext) {
        return mLifecycleSubject.subscribe(onNext);
    }

    public Subscription unsafeSubscribe(Subscriber<? super LifecycleEvent> subscriber) {
        return mLifecycleSubject.unsafeSubscribe(subscriber);
    }

    /**
     * UIに関わる処理を非同期で実行する。
     *
     * 処理順を整列するため、非同期・直列処理されたあと、アプリがフォアグラウンドのタイミングでコールバックされる。
     */
    public <T> BackgroundTaskBuilder<T> asyncUI(BackgroundTask.Async<T> background) {
        return async(ExecuteTarget.LocalQueue, CallbackTime.Foreground, background);
    }

    /**
     * 規定のスレッドとタイミングで非同期処理を行う
     */
    public <T> BackgroundTaskBuilder<T> async(ExecuteTarget execute, CallbackTime time, BackgroundTask.Async<T> background) {
        return new BackgroundTaskBuilder<T>(mCallbackQueue)
                .executeOn(execute)
                .callbackOn(time)
                .async(background);
    }

    /**
     * 規定のスレッドとタイミングで非同期処理を行う
     */
    @Deprecated
    public <T> BackgroundTaskBuilder<T> async(SubscribeTarget subscribe, ObserveTarget observe, BackgroundTask.Async<T> background) {
        return new BackgroundTaskBuilder<T>(mCallbackQueue)
                .subscribeOn(subscribe)
                .observeOn(observe)
                .async(background);
    }
}
