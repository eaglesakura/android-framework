package com.eaglesakura.android.framework.delegate.fragment;

import com.eaglesakura.android.framework.FwLog;
import com.eaglesakura.android.framework.delegate.lifecycle.FragmentLifecycleDelegate;
import com.eaglesakura.android.framework.delegate.lifecycle.UiLifecycleDelegate;
import com.eaglesakura.android.rx.event.OnCreateEvent;
import com.eaglesakura.util.StringUtil;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

/**
 * DialogFragment風の管理を行う場合に使用する
 */
public class DialogFragmentDelegate {
    private Dialog mDialog;

    @NonNull
    private final SupportDialogFragmentCompat mCompat;

    @NonNull
    private final UiLifecycleDelegate mLifecycleDelegate;

    public DialogFragmentDelegate(@NonNull SupportDialogFragmentCompat compat, @NonNull FragmentLifecycleDelegate lifecycle) {
        mCompat = compat;
        mLifecycleDelegate = lifecycle;
        lifecycle.getSubscription().getObservable().subscribe(it -> {
            switch (it.getState()) {
                case OnCreated:
                    onCreate((OnCreateEvent) it);
                    break;
                case OnDestroyed:
                    onDestroy();
                    break;
            }
        });
    }

    @Nullable
    public Dialog getDialog() {
        return mDialog;
    }

    void onCreate(OnCreateEvent event) {
        FwLog.widget("show dialog");
        mDialog = mLifecycleDelegate.addAutoDismiss(mCompat.onCreateDialog(this, event.getBundle()));
        mDialog.setOnDismissListener(it -> {
            if (mDialog == null) {
                return;
            }
            FwLog.widget("Detach DialogFragment");
            mCompat.onDismiss(this);
        });
        mDialog.show();
    }

    private void onDestroy() {
        FwLog.widget("suspend dialog");
        if (mDialog != null && mDialog.isShowing()) {
            Dialog dialog = mDialog;
            mDialog = null;
            dialog.dismiss();
        }
        FwLog.widget("remove dialog");
    }

    /**
     * ダイアログ表示を行う
     */
    public void onShow(FragmentManager fragmentManager, String tag) {
        if (mDialog != null) {
            throw new IllegalStateException();
        }

        if (StringUtil.isEmpty(tag)) {
            tag = mDialog.toString();
        }

        fragmentManager
                .beginTransaction()
                .add(mCompat.getFragment(this), tag)
                .commit();
    }

    public interface SupportDialogFragmentCompat {
        /**
         * ダイアログの生成を行わせる
         *
         * この呼出が行われたタイミングでは、全ての@Stateオブジェクトはレストア済である。
         */
        @NonNull
        Dialog onCreateDialog(DialogFragmentDelegate self, Bundle savedInstanceState);

        /**
         * ダイアログを閉じたため、Fragmentをcloseする
         */
        void onDismiss(DialogFragmentDelegate self);

        /**
         * Fragment取得
         */
        Fragment getFragment(DialogFragmentDelegate self);
    }
}
