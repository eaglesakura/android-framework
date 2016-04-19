package com.eaglesakura.android.framework.delegate.lifecycle;

import com.eaglesakura.android.rx.LifecycleEvent;
import com.eaglesakura.android.rx.LifecycleState;
import com.eaglesakura.android.rx.ObserveTarget;
import com.eaglesakura.android.rx.RxTask;
import com.eaglesakura.android.rx.RxTaskBuilder;
import com.eaglesakura.android.rx.SubscribeTarget;
import com.eaglesakura.android.rx.SubscriptionController;
import com.eaglesakura.android.rx.event.LifecycleEventImpl;

import rx.subjects.BehaviorSubject;

public abstract class LifecycleDelegate {

    protected final BehaviorSubject<LifecycleEvent> mLifecycleSubject = BehaviorSubject.create(new LifecycleEventImpl(LifecycleState.NewObject));

    protected final SubscriptionController mSubscription = new SubscriptionController();

    public LifecycleDelegate() {
        mSubscription.bind(mLifecycleSubject);
    }

    /**
     * ライフサイクル状態を取得する
     */
    public LifecycleState getLifecycleState() {
        return mLifecycleSubject.getValue().getState();
    }

    public SubscriptionController getSubscription() {
        return mSubscription;
    }

    /**
     * UIに関わる処理を非同期で実行する。
     *
     * 処理順を整列するため、非同期・直列処理されたあと、アプリがフォアグラウンドのタイミングでコールバックされる。
     */
    public <T> RxTaskBuilder<T> asyncUI(RxTask.Async<T> background) {
        return async(SubscribeTarget.Pipeline, ObserveTarget.Foreground, background);
    }

    /**
     * 規定のスレッドとタイミングで非同期処理を行う
     */
    public <T> RxTaskBuilder<T> async(SubscribeTarget subscribe, ObserveTarget observe, RxTask.Async<T> background) {
        return new RxTaskBuilder<T>(mSubscription)
                .subscribeOn(subscribe)
                .observeOn(observe)
                .async(background);
    }
}
