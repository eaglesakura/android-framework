package com.eaglesakura.android.framework.delegate.fragment;

import com.eaglesakura.android.framework.delegate.activity.SupportActivityDelegate;
import com.eaglesakura.android.framework.delegate.lifecycle.FragmentLifecycleDelegate;
import com.eaglesakura.android.framework.ui.support.annotation.FragmentLayout;
import com.eaglesakura.android.framework.ui.support.annotation.FragmentMenu;
import com.eaglesakura.android.framework.util.AppSupportUtil;
import com.eaglesakura.android.framework.util.FragmentUtil;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.margarine.MargarineKnife;
import com.eaglesakura.android.oari.ActivityResult;
import com.eaglesakura.android.rx.event.OnAttachEvent;
import com.eaglesakura.android.rx.event.OnCreateEvent;
import com.eaglesakura.android.rx.event.OnSaveEvent;
import com.eaglesakura.android.rx.event.OnViewCreateEvent;
import com.eaglesakura.android.saver.BundleState;
import com.eaglesakura.android.saver.LightSaver;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.android.util.ContextUtil;
import com.eaglesakura.android.util.PermissionUtil;
import com.eaglesakura.util.CollectionUtil;
import com.eaglesakura.util.ReflectionUtil;
import com.eaglesakura.util.StringUtil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.InternalSupportFragmentUtil;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * startActivityForResultを行う場合、ParentFragmentが存在していたらそちらのstartActivityForResultを呼び出す。
 * <br>
 * これはchildFragmentの場合にonActivityResultが呼ばれない不具合を可能な限り回避するため。
 * <br>
 * ただし、複数のonActivityResultがハンドリングされる恐れが有るため、RequestCodeの重複には十分に注意すること
 */
public class SupportFragmentDelegate {

    public interface SupportFragmentCompat {
        int FLAG_AFTERVIEW_INITIALIZE = 0x01 << 0;
        int FLAG_AFTERVIEW_RESTORE = 0x01 << 1;

        @NonNull
        Fragment getFragment(SupportFragmentDelegate self);


        @NonNull
        ActionBar getActionBar();

        /**
         * BackstackIndexが確定した
         *
         * SupportFragmentDelegate.setBackStackIndex(int)を呼び出す
         */
        void setBackStackIndex(int index);

        /**
         * Viewバインドが完了した
         *
         * @param flags FLAG_AFTERVIEW_INITIALIZE, FLAG_AFTERVIEW_RESTORE
         */
        void onAfterViews(SupportFragmentDelegate self, int flags);

        /**
         * メニューのバインドが完了した
         */
        void onAfterBindMenu(SupportFragmentDelegate self, Menu menu);

        /**
         * 依存注入が完了した
         */
        void onAfterInjection(SupportFragmentDelegate self);

        /**
         * 依存注入用のBuilderを開始する
         */
        @Nullable
        Garnet.Builder newInjectionBuilder(SupportFragmentDelegate self, Context context);
    }

    static final int BACKSTACK_NONE = 0xFEFEFEFE;

    @NonNull
    private SupportFragmentCompat mCompat;

    private int mBackStackIndex = BACKSTACK_NONE;

    /**
     * 既に依存構築済であればtrue
     */
    private boolean mInjectedInstance = false;

    @BundleState
    boolean mInitializedViews = false;

    @BundleState
    @LayoutRes
    int mInjectionLayoutId;

    @BundleState
    @MenuRes
    int mInjectionOptionMenuId;

    /**
     * Fragment用のView
     */
    View mView;

    public SupportFragmentDelegate(@NonNull SupportFragmentCompat compat, @NonNull FragmentLifecycleDelegate lifecycle) {
        mCompat = compat;

        lifecycle.getCallbackQueue().getObservable().subscribe(it -> {
            switch (it.getState()) {
                case OnCreated:
                    onCreate((OnCreateEvent) it);
                    break;
                case OnAttach:
                    onAttach((OnAttachEvent) it);
                    break;
                case OnViewCreated:
                    onCreateView((OnViewCreateEvent) it);
                    break;
                case OnSaveInstanceState:
                    onSaveInstanceState((OnSaveEvent) it);
                    break;
                case OnViewDestroyed:
                    onDestroyedView();
                    break;
            }
        });

        // InjectionIdを設定する
        {
            FragmentLayout layout = mCompat.getClass().getAnnotation(FragmentLayout.class);
            if (layout != null) {
                mInjectionLayoutId = layout.value();
                if (mInjectionLayoutId == 0 && !StringUtil.isEmpty(layout.resName())) {
                    mInjectionLayoutId = ContextUtil.getLayoutFromName(getFragment().getContext(), layout.resName());
                }
            }
        }

        // MenuIdを設定する
        {
            FragmentMenu menu = mCompat.getClass().getAnnotation(FragmentMenu.class);
            if (menu != null) {
                mInjectionOptionMenuId = menu.value();
                if (mInjectionOptionMenuId == 0 && !StringUtil.isEmpty(menu.resName())) {
                    mInjectionOptionMenuId = ContextUtil.getMenuFromName(getFragment().getContext(), menu.resName());
                }
            }

            if (mInjectionOptionMenuId != 0) {
                mCompat.getFragment(this).setHasOptionsMenu(true);
            }
        }
    }

    /**
     * パース対象のLayoutIdを指定する
     */
    public void setLayoutId(@LayoutRes int injectionLayoutId) {
        mInjectionLayoutId = injectionLayoutId;
    }

    /**
     * パース対象のMenuIdを指定する
     */
    public void setOptionMenuId(@MenuRes int injectionOptionMenuId) {
        mInjectionOptionMenuId = injectionOptionMenuId;
        getFragment().setHasOptionsMenu(true);
    }

    @CallSuper
    protected void onCreateView(OnViewCreateEvent event) {
        if (mView != null) {
            return;
        }

        if (mInjectionLayoutId != 0) {
            mView = event.getInflater().inflate(mInjectionLayoutId, event.getContainer(), false);
            MargarineKnife.from(mView).to(mCompat).bind();
            onAfterViews(mView);
        }
    }

    @CallSuper
    protected void onDestroyedView() {
        mView = null;
    }

    /**
     * Created Menu
     */
    @CallSuper
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        if (mInjectionOptionMenuId != 0) {
            inflater.inflate(mInjectionOptionMenuId, menu);
            MargarineKnife.bindMenu(menu, mCompat);
            UIHandler.postUI(() -> {
                mCompat.onAfterBindMenu(this, menu);
            });
        }
    }

    @NonNull
    @CallSuper
    public FragmentManager getFragmentManager() {
        return getFragment().getFragmentManager();
    }

    @NonNull
    @CallSuper
    public FragmentManager getChildFragmentManager() {
        return getFragment().getChildFragmentManager();
    }

    @NonNull
    @CallSuper
    public Fragment getFragment() {
        return mCompat.getFragment(this);
    }

    @Nullable
    @CallSuper
    public Fragment getParentFragment() {
        return getFragment().getParentFragment();
    }

    @CallSuper
    public Activity getActivity() {
        return getFragment().getActivity();
    }

    @CallSuper
    @Nullable
    public View getView() {
        return mView;
    }

    @CallSuper
    public <T extends Activity> T getActivity(@NonNull Class<T> clazz) {
        return (T) getActivity();
    }

    @Nullable
    public <T extends View> T findViewById(Class<T> clazz, int id) {
        return (T) getFragment().getView().findViewById(id);
    }

    @Nullable
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
     * 指定したインターフェースを実装しているクラス全てをeachで実行する
     *
     * 自分自身もインターフェースを実装している場合もコールバックを行う
     *
     * @param clazz 検索するインターフェース
     */
    @NonNull
    public <T> List<T> listInterfaces(@NonNull Class<T> clazz) {
        List<T> result = new ArrayList<>();
        for (Fragment frag : FragmentUtil.listFragments(getActivity(AppCompatActivity.class).getSupportFragmentManager(), fragment -> ReflectionUtil.instanceOf(fragment, clazz))) {
            result.add((T) ((Object) frag));
        }

        if (ReflectionUtil.instanceOf(getActivity(), clazz)) {
            result.add((T) ((Object) getActivity()));
        }

        return result;
    }

    /**
     * 指定されたインターフェースを列挙する。
     *
     * インターフェースが見つからない場合、例外を投げる。
     */
    @NonNull
    @Size(min = 1)
    public <T> List<T> listInterfacesOrThrow(@NonNull Class<T> clazz) {
        List<T> result = listInterfaces(clazz);
        if (result.isEmpty()) {
            throw new IllegalStateException(clazz.getName());
        }

        return result;
    }

    /**
     * 指定されたインターフェースを検索し、最初に見つかったものを返す。
     * インターフェースが見つからない場合、例外を投げる。
     *
     * @param clazz 検索対象
     */
    public <T> T findInterfaceOrThrow(@NonNull Class<T> clazz) {
        return listInterfacesOrThrow(clazz).get(0);
    }

    /**
     * 親クラスを特定のインターフェースに変換する
     *
     * 変換できない場合、このメソッドはnullを返却する。
     * 親Fragmentを辿り、対応しているFragmentを検索する。
     */
    @NonNull
    public <T> T getParentOrThrow(@NonNull Class<T> clazz) {
        Fragment fragment = getParentFragment();
        Activity activity = getActivity();

        while (fragment != null) {
            if (ReflectionUtil.instanceOf(fragment, clazz)) {
                return (T) fragment;
            }

            fragment = fragment.getParentFragment();
        }

        if (ReflectionUtil.instanceOf(activity, clazz)) {
            return (T) activity;
        }

        throw new IllegalStateException(clazz.getName());
    }

    /**
     * ActionBarを取得する
     */
    @Nullable
    public ActionBar getActionBar() {
        return mCompat.getActionBar();
    }

    /**
     * View構築が完了した
     *
     * このメソッドはonCreateView()内部で呼び出される。そのため、まだgetView()をすることはできない。
     * viewを参照する必要があるのなら、引数のfragmentViewを使用する。
     */
    @CallSuper
    @UiThread
    protected void onAfterViews(View fragmentView) {
        if (!mInitializedViews) {
            mCompat.onAfterViews(this, SupportFragmentCompat.FLAG_AFTERVIEW_INITIALIZE);
            mInitializedViews = true;
        } else {
            mCompat.onAfterViews(this, SupportFragmentCompat.FLAG_AFTERVIEW_RESTORE);
        }
    }

    @CallSuper
    @UiThread
    protected void onAttach(OnAttachEvent event) {
        if (!mInjectedInstance) {
            Garnet.Builder builder = mCompat.newInjectionBuilder(this, event.getContext());
            if (builder == null) {
                builder = Garnet.create(mCompat);
            }
            builder.depend(Context.class, event.getContext()).inject();
            mInjectedInstance = true;
            UIHandler.postUI(() -> {
                mCompat.onAfterInjection(this);
            });
        }
    }

    @CallSuper
    @UiThread
    protected void onCreate(OnCreateEvent event) {
        LightSaver.create(event.getBundle())
                .target(mCompat).restore()
                .target(this).restore();
    }

    @CallSuper
    @UiThread
    protected void onSaveInstanceState(OnSaveEvent event) {
        LightSaver.create(event.getBundle())
                .target(mCompat).save()
                .target(this).save();
    }

    /**
     * backstack idを指定する
     */
    public void setBackStackIndex(int backStackIndex) {
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
    public boolean isCurrentBackstack() {
        Fragment parentFragment = getParentFragment();
        if (parentFragment == null) {
            return mBackStackIndex == getFragment().getFragmentManager().getBackStackEntryCount();
        } else {
            return mBackStackIndex == parentFragment.getFragmentManager().getBackStackEntryCount();
        }
    }

    /**
     * 自身のFragmentから排除する
     *
     * @param withBackStack backstack階層も含めて排除する場合はtrue
     */
    public void detatchSelf(final boolean withBackStack) {
        UIHandler.postUIorRun(() -> {
            if (withBackStack && hasBackstackIndex()) {
                getFragmentManager().popBackStack(mBackStackIndex, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            } else {
                getFragmentManager().beginTransaction().remove(getFragment()).commit();
            }
        });
    }

    protected boolean hasChildBackStack() {
        return getChildFragmentManager().getBackStackEntryCount() > 0;
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

    public void startActivityForResult(Intent intent, int requestCode) {
        startActivityForResult(intent, requestCode, null);
    }

    public void startActivityForResult(Intent intent, int requestCode, @Nullable Bundle options) {
        Fragment result = getFragment();
        while (result.getParentFragment() != null) {
            result = result.getParentFragment();
        }
        InternalSupportFragmentUtil.startActivityForResult(result, requestCode, intent, options);
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        return ActivityResult.invokeRecursive(getFragment(), requestCode, resultCode, data);
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        AppSupportUtil.onRequestPermissionsResult(getActivity(), requestCode, permissions, grantResults);
    }

    /**
     * 複数Permissionをリクエストする
     */
    public boolean requestRuntimePermission(PermissionUtil.PermissionType... types) {
        Set<String> permissions = new HashSet<>();
        for (PermissionUtil.PermissionType type : types) {
            for (String pm : type.getPermissions()) {
                permissions.add(pm);
            }
        }
        return requestRuntimePermission(CollectionUtil.asArray(permissions, new String[permissions.size()]));
    }

    /**
     * 複数Permissionをリクエストする
     */
    public boolean requestRuntimePermission(List<PermissionUtil.PermissionType> types) {
        Set<String> permissions = new HashSet<>();
        for (PermissionUtil.PermissionType type : types) {
            for (String pm : type.getPermissions()) {
                permissions.add(pm);
            }
        }
        return requestRuntimePermission(CollectionUtil.asArray(permissions, new String[permissions.size()]));
    }

    /**
     * 持ち越しデータを含めて、Activityを開始する。
     * 持ち越しデータはonActivityResultで再度返却される。
     */
    public void startActivityForResultWithCarryData(Intent intent, int requestCode, Bundle carryState) {
        intent.putExtra(SupportActivityDelegate.EXTRA_CARRY_OVER_DATA, carryState);
        startActivityForResult(intent, requestCode);
    }
}