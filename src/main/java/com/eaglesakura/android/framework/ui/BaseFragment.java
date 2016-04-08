package com.eaglesakura.android.framework.ui;

import com.eaglesakura.android.framework.util.AppSupportUtil;
import com.eaglesakura.android.margarine.MargarineKnife;
import com.eaglesakura.android.oari.ActivityResult;
import com.eaglesakura.android.rx.LifecycleState;
import com.eaglesakura.android.rx.ObserveTarget;
import com.eaglesakura.android.rx.RxTask;
import com.eaglesakura.android.rx.RxTaskBuilder;
import com.eaglesakura.android.rx.SubscribeTarget;
import com.eaglesakura.android.rx.SubscriptionController;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.android.util.ContextUtil;
import com.eaglesakura.android.util.PermissionUtil;
import com.eaglesakura.util.ReflectionUtil;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import icepick.Icepick;
import icepick.State;
import rx.subjects.BehaviorSubject;

/**
 * startActivityForResultを行う場合、ParentFragmentが存在していたらそちらのstartActivityForResultを呼び出す。
 * <br>
 * これはchildFragmentの場合にonActivityResultが呼ばれない不具合を可能な限り回避するため。
 * <br>
 * ただし、複数のonActivityResultがハンドリングされる恐れが有るため、RequestCodeの重複には十分に注意すること
 */
public abstract class BaseFragment extends Fragment {

    static final int BACKSTACK_NONE = 0xFEFEFEFE;

    private int mBackStackIndex = BACKSTACK_NONE;

    @State
    boolean mInitializedViews = false;

    private boolean mInjectionViews = false;

    @LayoutRes
    private int mInjectionLayoutId;

    @MenuRes
    private int mInjectionOptionMenuId;

    private BehaviorSubject<LifecycleState> mLifecycleSubject = BehaviorSubject.create(LifecycleState.NewObject);

    private SubscriptionController mSubscription = new SubscriptionController();

    public BaseFragment() {
        mSubscription.bind(mLifecycleSubject);
    }

    /**
     * ライフサイクル状態を取得する
     */
    public LifecycleState getLifecycleState() {
        return mLifecycleSubject.getValue();
    }

    public void requestInjection(@LayoutRes int layoutId) {
        mInjectionLayoutId = layoutId;
        mInjectionViews = (mInjectionLayoutId != 0);
    }

    public void requestOptionMenu(@MenuRes int menuId) {
        mInjectionOptionMenuId = menuId;
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mInjectionViews) {
            View result = inflater.inflate(mInjectionLayoutId, container, false);
            MargarineKnife.bind(this, result);
            // getView対策で、１クッション置いて実行する
            UIHandler.postUI(() -> {
                onAfterViews();
            });
            return result;
        } else {
            return null;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(mInjectionOptionMenuId, menu);
    }

    public <T extends AppCompatActivity> T getActivity(Class<T> clazz) {
        return (T) getActivity();
    }

    public <T extends View> T findViewById(Class<T> clazz, int id) {
        return (T) getView().findViewById(id);
    }

    public <T extends View> T findViewByIdFromActivity(Class<T> clazz, int id) {
        return (T) getActivity().findViewById(id);
    }

    /**
     * 親クラスを特定のインターフェースに変換する
     *
     * 変換できない場合、このメソッドはnullを返却する
     */
    @Nullable
    public <T> T getParent(@NonNull Class<T> clazz) {
        Fragment fragment = getParentFragment();
        Activity activity = getActivity();
        if (ReflectionUtil.instanceOf(fragment, clazz)) {
            return (T) fragment;
        }

        if (ReflectionUtil.instanceOf(activity, clazz)) {
            return (T) activity;
        }

        return null;
    }

    /**
     * 親クラスを特定のインターフェースに変換する
     *
     * 変換できない場合、このメソッドはnullを返却する
     */
    @NonNull
    public <T> T getParentOrThrow(@NonNull Class<T> clazz) {
        Fragment fragment = getParentFragment();
        Activity activity = getActivity();
        if (ReflectionUtil.instanceOf(fragment, clazz)) {
            return (T) fragment;
        }

        if (ReflectionUtil.instanceOf(activity, clazz)) {
            return (T) activity;
        }

        throw new IllegalStateException(clazz.getName());
    }

    /**
     * ActionBarを取得する
     */
    public ActionBar getActionBar() {
        Activity activity = getActivity();
        if (activity instanceof AppCompatActivity) {
            return ((AppCompatActivity) activity).getSupportActionBar();
        } else {
            return null;
        }
    }

    /**
     * 初回のView構築
     */
    protected void onInitializeViews() {
    }

    /**
     * 二度目以降のView構築
     */
    protected void onRestoreViews() {

    }

    /**
     * View構築が完了した
     */
    protected void onAfterViews() {
        if (!mInitializedViews) {
            onInitializeViews();
            mInitializedViews = true;
        } else {
            onRestoreViews();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLifecycleSubject.onNext(LifecycleState.OnCreated);
        if (savedInstanceState != null) {
            Icepick.restoreInstanceState(this, savedInstanceState);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mLifecycleSubject.onNext(LifecycleState.OnStarted);
    }

    @Override
    public void onResume() {
        super.onResume();
        mLifecycleSubject.onNext(LifecycleState.OnResumed);
    }

    @Override
    public void onStop() {
        super.onStop();
        mLifecycleSubject.onNext(LifecycleState.OnStopped);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLifecycleSubject.onNext(LifecycleState.OnDestroyed);
    }

    /**
     * backstack idを指定する
     */
    void setBackStackIndex(int backStackIndex) {
        this.mBackStackIndex = backStackIndex;
    }

    /**
     * Backstackを持つならばtrue
     */
    boolean hasBackstackIndex() {
        return mBackStackIndex != BACKSTACK_NONE;
    }

    public int getBackStackIndex() {
        return mBackStackIndex;
    }

    /**
     * バックスタックが一致したらtrue
     */
    @SuppressLint("NewApi")
    public boolean isCurrentBackstack() {
        if (!ContextUtil.supportedChildFragmentManager() || getParentFragment() == null) {
            return mBackStackIndex == getFragmentManager().getBackStackEntryCount();
        } else {
            return mBackStackIndex == getParentFragment().getFragmentManager().getBackStackEntryCount();
        }
    }

    /**
     * 自身をFragmentから外す
     *
     * @param withBackStack backstack階層も含めて排除する場合はtrue
     */
    public void detatchSelf(final boolean withBackStack) {
        UIHandler.postUIorRun(new Runnable() {
            @Override
            public void run() {
                if (withBackStack && hasBackstackIndex()) {
                    getFragmentManager().popBackStack(mBackStackIndex, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                } else {
                    getFragmentManager().beginTransaction().remove(BaseFragment.this).commit();
                }
            }
        });
    }

    protected boolean hasChildBackStack() {
        return getChildFragmentManager().getBackStackEntryCount() > 0;
    }

    public final String createSimpleTag() {
        return ((Object) this).getClass().getSimpleName();
    }

    /**
     * 戻るボタンのハンドリングを行う
     *
     * @return ハンドリングを行えたらtrue
     */
    public boolean handleBackButton() {
        if (hasChildBackStack()) {
            // backStackを解放する
            getChildFragmentManager().popBackStack();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if (getParentFragment() != null) {
            getParentFragment().startActivityForResult(intent, requestCode);
        } else {
            super.startActivityForResult(intent, requestCode);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        ActivityResult.invokeRecursive(this, requestCode, resultCode, data);
    }

    /**
     * Runtime Permissionの更新を行わせる
     *
     * @return パーミッション取得を開始した場合はtrue
     */
    public boolean requestRuntimePermission(String[] permissions) {
        return AppSupportUtil.requestRuntimePermissions(getActivity(), permissions);
    }

    /**
     * Runtime Permissionのブロードキャストを行わせる
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        AppSupportUtil.onRequestPermissionsResult(getActivity(), requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public boolean requestRuntimePermission(PermissionUtil.PermissionType type) {
        return requestRuntimePermission(type.getPermissions());
    }


    /**
     * UIスレッドで実行する
     */
    protected void runOnUiThread(Runnable runnable) {
        UIHandler.postUIorRun(runnable);
    }

    /**
     * タスクコントローラを取得する
     */
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