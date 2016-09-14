package com.eaglesakura.android.framework.delegate.activity;

import com.eaglesakura.android.framework.R;
import com.eaglesakura.android.framework.delegate.lifecycle.ActivityLifecycleDelegate;
import com.eaglesakura.android.framework.util.AppSupportUtil;
import com.eaglesakura.android.framework.util.FragmentUtil;
import com.eaglesakura.android.oari.ActivityResult;
import com.eaglesakura.android.rx.event.OnRestoreEvent;
import com.eaglesakura.android.rx.event.OnSaveEvent;
import com.eaglesakura.android.saver.LightSaver;
import com.eaglesakura.util.ReflectionUtil;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class SupportActivityDelegate {
    @NonNull
    private final SupportActivityCompat mCompat;

    public interface SupportActivityCompat {
        Activity getActivity(SupportActivityDelegate self);
    }

    public SupportActivityDelegate(@NonNull SupportActivityCompat compat, @NonNull ActivityLifecycleDelegate lifecycle) {
        mCompat = compat;
        lifecycle.getCallbackQueue().getObservable().subscribe(it -> {
            switch (it.getState()) {
                case OnCreated:
                    onCreate();
                    break;
                case OnSaveInstanceState:
                    onSaveInstanceState((OnSaveEvent) it);
                    break;
                case OnRestoreInstanceState:
                    onRestoreInstanceState((OnRestoreEvent) it);
                    break;
            }
        });
    }

    @CallSuper
    @UiThread
    protected void onCreate() {
        edgeColorToPrimaryColor();
    }

    @CallSuper
    @UiThread
    protected void onRestoreInstanceState(OnRestoreEvent event) {
        LightSaver.create(event.getBundle())
                .target(getActivity()).restore()
                .target(this).restore();
    }

    @CallSuper
    @UiThread
    protected void onSaveInstanceState(OnSaveEvent event) {
        LightSaver.create(event.getBundle())
                .target(getActivity()).save()
                .target(this).save();
    }

    public <T extends View> T findViewById(@NonNull Class<T> clazz, @IdRes int id) {
        return (T) getActivity().findViewById(id);
    }

    @NonNull
    public Activity getActivity() {
        return mCompat.getActivity(this);
    }

    @Nullable
    public Intent getIntent() {
        return getActivity().getIntent();
    }

    @NonNull
    public Resources getResources() {
        return getActivity().getResources();
    }

    @NonNull
    public Resources.Theme getTheme() {
        return getActivity().getTheme();
    }

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
        return ActivityResult.invoke(getActivity(), requestCode, resultCode, data);
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
        for (Fragment frag : FragmentUtil.listFragments(((AppCompatActivity) getActivity()).getSupportFragmentManager(), fragment -> ReflectionUtil.instanceOf(fragment, clazz))) {
            result.add((T) frag);
        }
        return result;
    }

}
