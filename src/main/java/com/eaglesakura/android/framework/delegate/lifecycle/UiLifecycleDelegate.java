package com.eaglesakura.android.framework.delegate.lifecycle;

import com.eaglesakura.android.framework.FwLog;
import com.eaglesakura.android.util.DialogUtil;

import android.app.Dialog;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.widget.PopupWindow;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public abstract class UiLifecycleDelegate extends LifecycleDelegate {
    /**
     * 監視対象とするDialog
     */
    private List<AutoDismissObject> mAutoDismissDialogs = new LinkedList<>();

    /**
     * ライフサイクルに合わせて自動的に開放するオブジェクト
     */
    public interface AutoDismissObject {
        /**
         * 開放処理を行う
         */
        void dismiss();

        /**
         * オブジェクトが生存している
         */
        boolean exist();

        /**
         * 管理タグを取得する
         */
        Object getTag();

        Object getObject();
    }

    /**
     * 指定したタグのオブジェクトを取得する
     */
    public Object getAutoDismissObject(@NonNull Object tag) {
        compactAutoDismissDialogs();
        for (AutoDismissObject obj : mAutoDismissDialogs) {
            if (tag.equals(obj.getTag())) {
                return obj.getObject();
            }
        }
        return null;
    }

    /**
     * 指定したタグのオブジェクトを取得する
     */
    public boolean hasAutoDismissObject(@NonNull Object tag) {
        return getAutoDismissObject(tag) != null;
    }

    @UiThread
    public <T extends Dialog> T addAutoDismiss(@NonNull T dialog) {
        return addAutoDismiss(dialog, null);
    }

    @UiThread
    public <T extends Dialog> T addAutoDismiss(@NonNull T dialog, Object tag) {
        compactAutoDismissDialogs();
        if (dialog != null) {
            mAutoDismissDialogs.add(new AutoDismissObject() {
                @Override
                public void dismiss() {
                    DialogUtil.dismiss(dialog);
                }

                @Override
                public boolean exist() {
                    return dialog.isShowing();
                }

                @Override
                public Object getTag() {
                    return tag;
                }

                @Override
                public Object getObject() {
                    return dialog;
                }
            });
        }
        return dialog;
    }

    @UiThread
    public <T extends PopupWindow> T addAutoDismiss(@NonNull T window) {
        return addAutoDismiss(window, null);
    }

    @UiThread
    public <T extends PopupWindow> T addAutoDismiss(@NonNull T window, Object tag) {
        compactAutoDismissDialogs();
        if (window != null) {
            mAutoDismissDialogs.add(new AutoDismissObject() {
                @Override
                public void dismiss() {
                    try {
                        if (window.isShowing()) {
                            window.dismiss();
                        }
                    } catch (Exception e) {

                    }
                }

                @Override
                public boolean exist() {
                    return window.isShowing();
                }

                @Override
                public Object getTag() {
                    return tag;
                }

                @Override
                public Object getObject() {
                    return window;
                }
            });
        }
        return window;
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
        Iterator<AutoDismissObject> iterator = mAutoDismissDialogs.iterator();
        while (iterator.hasNext()) {
            AutoDismissObject next = iterator.next();
            if (next == null) {
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
        Iterator<AutoDismissObject> iterator = mAutoDismissDialogs.iterator();
        while (iterator.hasNext()) {
            AutoDismissObject obj = iterator.next();
            if (obj != null) {
                FwLog.widget("AutoDismiss :: %s", obj.getClass());
                obj.dismiss();
            }
        }
        mAutoDismissDialogs.clear();
    }

}
