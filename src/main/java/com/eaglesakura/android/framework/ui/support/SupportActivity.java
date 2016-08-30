package com.eaglesakura.android.framework.ui.support;

import com.eaglesakura.android.framework.delegate.activity.SupportActivityDelegate;
import com.eaglesakura.android.framework.delegate.lifecycle.ActivityLifecycleDelegate;
import com.eaglesakura.android.margarine.MargarineKnife;
import com.eaglesakura.android.rx.BackgroundTask;
import com.eaglesakura.android.rx.BackgroundTaskBuilder;
import com.eaglesakura.android.rx.CallbackTime;
import com.eaglesakura.android.rx.ExecuteTarget;
import com.eaglesakura.android.rx.LifecycleState;
import com.eaglesakura.android.rx.ObserveTarget;
import com.eaglesakura.android.rx.SubscribeTarget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.PopupWindow;

/**
 *
 */
public abstract class SupportActivity extends AppCompatActivity implements SupportActivityDelegate.SupportActivityCompat {

    protected final ActivityLifecycleDelegate mLifecycleDelegate = new ActivityLifecycleDelegate();

    protected final SupportActivityDelegate mActivityDelegate = new SupportActivityDelegate(this, mLifecycleDelegate);

    protected SupportActivity() {
    }

    /**
     * ライフサイクル状態を取得する
     */
    public LifecycleState getLifecycleState() {
        return mLifecycleDelegate.getLifecycleState();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        mLifecycleDelegate.onRestoreInstanceState(savedInstanceState);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLifecycleDelegate.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLifecycleDelegate.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLifecycleDelegate.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLifecycleDelegate.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mLifecycleDelegate.onSaveInstanceState(outState);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLifecycleDelegate.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLifecycleDelegate.onDestroy();
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        MargarineKnife.bind(this);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        MargarineKnife.bind(this);
    }

    public <T extends View> T findViewById(Class<T> clazz, int id) {
        return mActivityDelegate.findViewById(clazz, id);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mActivityDelegate.onActivityResult(requestCode, resultCode, data)) {
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @UiThread
    public <T extends Dialog> T addAutoDismiss(@NonNull T dialog) {
        return mLifecycleDelegate.addAutoDismiss(dialog);
    }

    @UiThread
    public <T extends Dialog> T addAutoDismiss(@NonNull T dialog, Object tag) {
        return mLifecycleDelegate.addAutoDismiss(dialog, tag);
    }

    @UiThread
    public <T extends PopupWindow> T addAutoDismiss(@NonNull T window) {
        return mLifecycleDelegate.addAutoDismiss(window);
    }

    @UiThread
    public <T extends PopupWindow> T addAutoDismiss(@NonNull T window, Object tag) {
        return mLifecycleDelegate.addAutoDismiss(window, tag);
    }

    /**
     * Runtime Permissionの更新を行わせる
     *
     * @return パーミッション取得を開始した場合はtrue
     */
    public boolean requestRuntimePermissions(String[] permissions) {
        return mActivityDelegate.requestRuntimePermissions(permissions);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        mActivityDelegate.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * UIに関わる処理を非同期で実行する。
     *
     * 処理順を整列するため、非同期・直列処理されたあと、アプリがフォアグラウンドのタイミングでコールバックされる。
     */
    public <T> BackgroundTaskBuilder<T> asyncUI(BackgroundTask.Async<T> background) {
        return mLifecycleDelegate.asyncUI(background);
    }

    /**
     * 規定のスレッドとタイミングで非同期処理を行う
     */
    public <T> BackgroundTaskBuilder<T> async(ExecuteTarget execute, CallbackTime time, BackgroundTask.Async<T> background) {
        return mLifecycleDelegate.async(execute, time, background);
    }


    /**
     * 規定のスレッドとタイミングで非同期処理を行う
     */
    @Deprecated
    public <T> BackgroundTaskBuilder<T> async(SubscribeTarget subscribe, ObserveTarget observe, BackgroundTask.Async<T> background) {
        return mLifecycleDelegate.async(subscribe, observe, background);
    }

    @Override
    public Activity getActivity(SupportActivityDelegate self) {
        return this;
    }
}
