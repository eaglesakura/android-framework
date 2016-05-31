package com.eaglesakura.android.framework.delegate.lifecycle;

import com.eaglesakura.android.framework.FwLog;
import com.eaglesakura.android.framework.delegate.lifecycle.LifecycleDelegate;
import com.eaglesakura.android.util.DialogUtil;

import android.app.Dialog;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public abstract class UiLifecycleDelegate extends LifecycleDelegate {
    /**
     * 監視対象とするDialog
     */
    private List<WeakReference<Dialog>> mAutoDismissDialogs = new LinkedList<>();

    @UiThread
    public <T extends Dialog> T addAutoDismiss(@NonNull T dialog) {
        compactAutoDismissDialogs();
        if (dialog != null) {
            mAutoDismissDialogs.add(new WeakReference<>(dialog));
        }
        return dialog;
    }

    /**
     * 監視対象のDialogを持っていたらtrue
     */
    public boolean hasDialogs() {
        compactAutoDismissDialogs();
        return !mAutoDismissDialogs.isEmpty();
    }

    /**
     * Dialogを全て開放する
     */
    @UiThread
    public void compactAutoDismissDialogs() {
        Iterator<WeakReference<Dialog>> iterator = mAutoDismissDialogs.iterator();
        while (iterator.hasNext()) {
            WeakReference<Dialog> next = iterator.next();
            Dialog dialog = next.get();
            if (dialog == null) {
                iterator.remove();
            }
        }
    }

    @CallSuper
    @UiThread
    protected void onResume() {
        compactAutoDismissDialogs();
    }

    @CallSuper
    @UiThread
    protected void onPause() {
        compactAutoDismissDialogs();
    }

    @CallSuper
    @UiThread
    protected void onDestroy() {
        Iterator<WeakReference<Dialog>> iterator = mAutoDismissDialogs.iterator();
        while (iterator.hasNext()) {
            Dialog dialog = iterator.next().get();
            if (dialog != null) {
                FwLog.widget("AutoDismiss :: %s", dialog.getClass());
                DialogUtil.dismiss(dialog);
            }
        }
        mAutoDismissDialogs.clear();
    }

}
