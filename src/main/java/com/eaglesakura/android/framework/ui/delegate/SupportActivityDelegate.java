package com.eaglesakura.android.framework.ui.delegate;

import com.eaglesakura.android.framework.R;
import com.eaglesakura.android.framework.util.AppSupportUtil;
import com.eaglesakura.android.oari.ActivityResult;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.view.KeyEvent;
import android.view.View;

import icepick.Icepick;

/**
 *
 */
public class SupportActivityDelegate extends LifecycleDelegate {

    @NonNull
    private final SupportActivityCompat mCompat;

    public interface SupportActivityCompat {
        Activity getActivity(SupportActivityDelegate self);
    }

    public SupportActivityDelegate(@NonNull SupportActivityCompat compat) {
        mCompat = compat;
    }

    @CallSuper
    @UiThread
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Icepick.restoreInstanceState(getActivity(), savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
    }

    @CallSuper
    @UiThread
    public void onCreate(@Nullable Bundle savedInstanceState) {
        edgeColorToPrimaryColor();
        super.onCreate();
    }

    @CallSuper
    @UiThread
    @Override
    public void onStart() {
        super.onStart();
    }

    @CallSuper
    @UiThread
    @Override
    public void onResume() {
        super.onResume();
    }

    @CallSuper
    @UiThread
    @Override
    public void onPause() {
        super.onPause();
    }

    @CallSuper
    @UiThread
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        Icepick.saveInstanceState(getActivity(), outState);
        Icepick.saveInstanceState(this, outState);
    }

    @CallSuper
    @UiThread
    @Override
    public void onStop() {
        super.onStop();
    }

    @CallSuper
    @UiThread
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public <T extends View> T findViewById(@NonNull Class<T> clazz, @IdRes int id) {
        return (T) getActivity().findViewById(id);
    }

    protected final Activity getActivity() {
        return mCompat.getActivity(this);
    }

    protected final Resources getResources() {
        return getActivity().getResources();
    }

    protected final Resources.Theme getTheme() {
        return getActivity().getTheme();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setStatusBarColor(int argb) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setStatusBarColor(argb);
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
            Activity activity = getActivity();

            TypedArray typedArray = activity.getTheme().obtainStyledAttributes(R.styleable.AppCompatTheme);
            int id = typedArray.getResourceId(R.styleable.AppCompatTheme_colorPrimaryDark, 0);
            int brandColor = ResourcesCompat.getColor(getResources(), id, getTheme());

            //glow
            int glowDrawableId = getResources().getIdentifier("overscroll_glow", "drawable", "android");
            Drawable androidGlow = ResourcesCompat.getDrawable(getResources(), glowDrawableId, getTheme());
            androidGlow.setColorFilter(brandColor, PorterDuff.Mode.SRC_IN);
            //edge
            int edgeDrawableId = getResources().getIdentifier("overscroll_edge", "drawable", "android");
            Drawable androidEdge = ResourcesCompat.getDrawable(getResources(), edgeDrawableId, getTheme());
            androidEdge.setColorFilter(brandColor, PorterDuff.Mode.SRC_IN);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * onActivityResultのハンドリングを行う
     *
     * @return ハンドリングを行った場合trueを返却する
     */
    @CallSuper
    @UiThread
    public boolean onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        return ActivityResult.invoke(this, requestCode, resultCode, data);
    }

    /**
     * Fragmentがアタッチされたタイミングで呼び出される。
     * <br>
     * このFragmentは最上位階層のみが扱われる。
     */
    @CallSuper
    @UiThread
    public void onAttachFragment(@NonNull Fragment fragment) {
    }

    /**
     * キーイベントのハンドリングを行う
     *
     * @return ハンドリングを行った場合true
     */
    @CallSuper
    @UiThread
    public boolean dispatchKeyEvent(KeyEvent event) {
        return false;
    }

    /**
     * Runtime Permissionの更新を行わせる
     *
     * @return パーミッション取得を開始した場合はtrue
     */
    @CallSuper
    @UiThread
    public boolean requestRuntimePermissions(String[] permissions) {
        return AppSupportUtil.requestRuntimePermissions(getActivity(), permissions);
    }

    @CallSuper
    @UiThread
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        AppSupportUtil.onRequestPermissionsResult(getActivity(), requestCode, permissions, grantResults);
    }
}
