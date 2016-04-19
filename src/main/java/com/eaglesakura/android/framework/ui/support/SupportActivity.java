package com.eaglesakura.android.framework.ui.support;

import com.eaglesakura.android.framework.delegate.activity.SupportActivityDelegate;
import com.eaglesakura.android.framework.delegate.lifecycle.ActivityLifecycleDelegate;
import com.eaglesakura.android.margarine.MargarineKnife;
import com.eaglesakura.android.rx.LifecycleState;
import com.eaglesakura.android.rx.ObserveTarget;
import com.eaglesakura.android.rx.RxTask;
import com.eaglesakura.android.rx.RxTaskBuilder;
import com.eaglesakura.android.rx.SubscribeTarget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

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
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        mLifecycleDelegate.onSaveInstanceState(outState, outPersistentState);
        super.onSaveInstanceState(outState, outPersistentState);
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
    public <T> RxTaskBuilder<T> asyncUI(RxTask.Async<T> background) {
        return mLifecycleDelegate.asyncUI(background);
    }

    /**
     * 規定のスレッドとタイミングで非同期処理を行う
     */
    public <T> RxTaskBuilder<T> async(SubscribeTarget subscribe, ObserveTarget observe, RxTask.Async<T> background) {
        return mLifecycleDelegate.async(subscribe, observe, background);
    }

    @Override
    public Activity getActivity(SupportActivityDelegate self) {
        return this;
    }
}
