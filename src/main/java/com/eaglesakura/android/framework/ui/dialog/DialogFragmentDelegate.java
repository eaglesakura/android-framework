package com.eaglesakura.android.framework.ui.dialog;

import com.eaglesakura.android.framework.FwLog;
import com.eaglesakura.util.StringUtil;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

/**
 * App側で継承したFragmentでDialogFramentを生成する場合に
 */
public class DialogFragmentDelegate {
    private Dialog mDialog;

    @NonNull
    private InternalCallback mInternalCallback;

    @NonNull
    private Fragment mFragment;

    public DialogFragmentDelegate(@NonNull Fragment fragment, @NonNull InternalCallback internalCallback) {
        mFragment = fragment;
        mInternalCallback = internalCallback;
    }

    @Nullable
    public Dialog getDialog() {
        return mDialog;
    }

    public void onCreate(Bundle savedInstanceState) {
        FwLog.widget("show dialog");
        mDialog = mInternalCallback.onCreateDialog(this, savedInstanceState);
        mDialog.setOnDismissListener(it -> {
            if (mDialog == null) {
                return;
            }
            FwLog.widget("Detach DialogFragment");
            mInternalCallback.onDismiss(this);
        });
        mDialog.show();
    }

    public void onStart() {

    }

    public void onStop() {

    }

    public void onDestroy() {
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
                .add(mFragment, tag)
                .commit();
    }

    public interface InternalCallback {
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

    }
}
