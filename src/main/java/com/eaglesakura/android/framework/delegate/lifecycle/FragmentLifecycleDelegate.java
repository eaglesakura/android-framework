package com.eaglesakura.android.framework.delegate.lifecycle;

import com.eaglesakura.android.rx.LifecycleState;
import com.eaglesakura.android.rx.event.LifecycleEventImpl;
import com.eaglesakura.android.rx.event.OnAttachEvent;
import com.eaglesakura.android.rx.event.OnCreateEvent;
import com.eaglesakura.android.rx.event.OnSaveEvent;
import com.eaglesakura.android.rx.event.OnViewCreateEvent;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.LayoutInflater;
import android.view.ViewGroup;

public class FragmentLifecycleDelegate extends UiLifecycleDelegate {


    @CallSuper
    @UiThread
    public void onAttach(Context context) {
        mLifecycleSubject.onNext(new OnAttachEvent(context));
    }

    @CallSuper
    @UiThread
    public void onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mLifecycleSubject.onNext(new OnViewCreateEvent(inflater, container, savedInstanceState));
    }


    @CallSuper
    @UiThread
    public void onCreate(Bundle savedInstanceState) {
        mLifecycleSubject.onNext(new OnCreateEvent(savedInstanceState));
    }

    @CallSuper
    @UiThread
    public void onStart() {
        mLifecycleSubject.onNext(new LifecycleEventImpl(LifecycleState.OnStarted));
    }

    @CallSuper
    @UiThread
    @Override
    public void onResume() {
        super.onResume();
        mLifecycleSubject.onNext(new LifecycleEventImpl(LifecycleState.OnResumed));
    }

    @CallSuper
    @UiThread
    public void onSaveInstanceState(Bundle outState) {
        mLifecycleSubject.onNext(new OnSaveEvent(outState));
    }

    @CallSuper
    @UiThread
    @Override
    public void onPause() {
        super.onPause();
        mLifecycleSubject.onNext(new LifecycleEventImpl(LifecycleState.OnPaused));
    }

    @CallSuper
    @UiThread
    public void onStop() {
        mLifecycleSubject.onNext(new LifecycleEventImpl(LifecycleState.OnStopped));
    }

    @CallSuper
    @UiThread
    public void onDestroyView() {
        mLifecycleSubject.onNext(new LifecycleEventImpl(LifecycleState.OnViewDestroyed));
    }

    @CallSuper
    @UiThread
    @Override
    public void onDestroy() {
        super.onDestroy();
        mLifecycleSubject.onNext(new LifecycleEventImpl(LifecycleState.OnDestroyed));
    }
}
