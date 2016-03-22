package com.eaglesakura.android.framework.ui;

import com.eaglesakura.android.framework.R;
import com.eaglesakura.android.framework.util.AppSupportUtil;
import com.eaglesakura.android.margarine.MargarineKnife;
import com.eaglesakura.android.oari.ActivityResult;
import com.eaglesakura.android.rx.LifecycleState;
import com.eaglesakura.android.rx.ObserveTarget;
import com.eaglesakura.android.rx.RxTask;
import com.eaglesakura.android.rx.RxTaskBuilder;
import com.eaglesakura.android.rx.SubscribeTarget;
import com.eaglesakura.android.rx.SubscriptionController;
import com.eaglesakura.android.util.ContextUtil;
import com.eaglesakura.util.LogUtil;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;

import java.util.List;

import icepick.State;
import rx.subjects.BehaviorSubject;

/**
 *
 */
public abstract class BaseActivity extends AppCompatActivity {

    private BehaviorSubject<LifecycleState> mLifecycleSubject = BehaviorSubject.create(LifecycleState.NewObject);

    private SubscriptionController mSubscription = new SubscriptionController();

    /**
     * Fragment管理のコールバックを受け付ける
     */
    private FragmentChooser.Callback mChooserCallbackImpl = new FragmentChooser.Callback() {
        @Override
        public FragmentManager getFragmentManager(FragmentChooser chooser) {
            return getSupportFragmentManager();
        }

        @Override
        public boolean isFragmentExist(FragmentChooser chooser, Fragment fragment) {
            if (fragment == null) {
                return false;
            }

            if (fragment instanceof BaseFragment) {
                // 廃棄済みはさっさと排除する
                if (((BaseFragment) fragment).getLifecycleState() == LifecycleState.OnDestroyed) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public Fragment newFragment(FragmentChooser chooser, String requestTag) {
            return null;
        }
    };

    @State
    FragmentChooser mFragmentChooser = new FragmentChooser(mChooserCallbackImpl);

    protected BaseActivity() {
        mSubscription.bind(mLifecycleSubject);
    }

    /**
     * ライフサイクル状態を取得する
     */
    public LifecycleState getLifecycleState() {
        return mLifecycleSubject.getValue();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        edgeColorToPrimaryColor();
        mLifecycleSubject.onNext(LifecycleState.OnCreated);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLifecycleSubject.onNext(LifecycleState.OnStarted);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLifecycleSubject.onNext(LifecycleState.OnResumed);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLifecycleSubject.onNext(LifecycleState.OnPaused);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLifecycleSubject.onNext(LifecycleState.OnStopped);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLifecycleSubject.onNext(LifecycleState.OnDestroyed);
    }

    protected void requestInjection(@LayoutRes int layoutId) {
        setContentView(layoutId);
        MargarineKnife.bind(this);
    }

    public <T extends View> T findViewById(Class<T> clazz, int id) {
        return (T) findViewById(id);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setStatusBarColor(int argb) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(argb);
        }
    }

    /**
     * Scroll系レイアウトでのEdgeColorをブランドに合わせて変更する
     * <br/>
     * Lollipopでは自動的にcolorPrimary系の色が反映されるため、何も行わない。
     * <br/>
     * 参考: http://stackoverflow.com/questions/28978989/set-recyclerview-edge-glow-pre-lollipop-when-using-appcompat
     */
    private void edgeColorToPrimaryColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        try {
            TypedArray typedArray = getTheme().obtainStyledAttributes(R.styleable.AppCompatTheme);
            int id = typedArray.getResourceId(R.styleable.AppCompatTheme_colorPrimaryDark, 0);
            int brandColor = getResources().getColor(id);

            //glow
            int glowDrawableId = getResources().getIdentifier("overscroll_glow", "drawable", "android");
            Drawable androidGlow = getResources().getDrawable(glowDrawableId);
            androidGlow.setColorFilter(brandColor, PorterDuff.Mode.SRC_IN);
            //edge
            int edgeDrawableId = getResources().getIdentifier("overscroll_edge", "drawable", "android");
            Drawable androidEdge = getResources().getDrawable(edgeDrawableId);
            androidEdge.setColorFilter(brandColor, PorterDuff.Mode.SRC_IN);
        } catch (Exception e) {
            LogUtil.log(e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (ActivityResult.invoke(this, requestCode, resultCode, data)) {
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Fragmentがアタッチされたタイミングで呼び出される。
     * <br>
     * このFragmentは最上位階層のみが扱われる。
     */
    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);

        // キャッシュに登録する
        mFragmentChooser.compact();
        mFragmentChooser.addFragment(FragmentChooser.ReferenceType.Weak, fragment, fragment.getTag(), 0);
    }

    /**
     * キーイベントハンドリングを行う
     *
     * @return ハンドリングを行えたらtrue
     */
    protected boolean handleFragmentsKeyEvent(KeyEvent event) {
        if (!ContextUtil.isBackKeyEvent(event)) {
            return false;
        }

        List<Fragment> list = mFragmentChooser.listExistFragments();
        for (Fragment frag : list) {
            if (frag.isVisible() && frag instanceof BaseFragment) {
                if (((BaseFragment) frag).handleBackButton()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (handleFragmentsKeyEvent(event)) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * Runtime Permissionの更新を行わせる
     *
     * @return パーミッション取得を開始した場合はtrue
     */
    public boolean requestRuntimePermissions(String[] permissions) {
        return AppSupportUtil.requestRuntimePermissions(this, permissions);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        AppSupportUtil.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
