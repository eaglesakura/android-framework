package com.eaglesakura.material.widget.support.web;

import com.eaglesakura.android.framework.FwLog;
import com.eaglesakura.android.framework.delegate.lifecycle.LifecycleDelegate;
import com.eaglesakura.android.framework.ui.support.SupportFragment;
import com.eaglesakura.android.framework.util.FragmentUtil;
import com.eaglesakura.android.util.ContextUtil;
import com.eaglesakura.material.widget.SupportWebView;
import com.eaglesakura.util.Util;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;

/**
 * WebView操作用Util
 */
public class SupportWebViewController {
    /**
     * 画面の上部
     */
    public static final int SCROLL_FLAG_TOP = 0x1 << 0;
    /**
     * スクロール中
     */
    public static final int SCROLL_FLAG_MIDDLE = 0x1 << 1;
    /**
     * 終端に達した
     */
    public static final int SCROLL_FLAG_BOTTOM = 0x1 << 2;

    /**
     * ユーザーが操作中である
     */
    public static final int SCROLL_FLAG_FLOATING = 0x1 << 3;

    final Callback mCallback;

    ObservableScrollViewCallbacks mScrollCallback = new ObservableScrollViewCallbacks() {
        @Override
        public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
            FwLog.widget("WebScroll scroll[%d] flag[%x]", scrollY, getScrollFlag());
            nextScrollState();
        }

        @Override
        public void onDownMotionEvent() {

        }

        @Override
        public void onUpOrCancelMotionEvent(ScrollState scrollState) {

        }
    };

    /**
     * 前回コールバック時のスクロール状態
     */
    int mOldScrollFlag;

    public SupportWebViewController(Callback callback, LifecycleDelegate delegate) {
        mCallback = callback;
        delegate.getCallbackQueue().subscribe(event -> {
            switch (event.getState()) {
                case OnResumed:
                    Util.ifPresent(getView(), view -> view.setScrollViewCallbacks(mScrollCallback));
                    break;
            }
        });
    }

    public static SupportWebViewController from(FragmentManager fragmentManager) {
        return FragmentUtil.listInterfaces(fragmentManager, Holder.class).get(0).getWebContentController();
    }

    public static SupportWebViewController from(SupportFragment fragment) {
        return from(fragment.getFragmentManager());
    }

    @Nullable
    public SupportWebView getView() {
        return mCallback.getWebView(this);
    }

    /**
     * キーイベントのハンドリングを行った場合はtrueを返却する
     */
    public boolean dispatchKeyEvent(KeyEvent event) {
        SupportWebView view = getView();

        if (!ContextUtil.isBackKeyEvent(event) || view == null) {
            return false;
        }

        if (!view.canGoBack()) {
            // 戻るべきページがない
            return false;
        }

        // 1段階前へ戻す
        view.goBack();
        return true;
    }

    /**
     * 現在のスクロール状態を取得する
     *
     * @see SupportWebViewController#SCROLL_FLAG_TOP
     * @see SupportWebViewController#SCROLL_FLAG_MIDDLE
     * @see SupportWebViewController#SCROLL_FLAG_BOTTOM
     */
    public int getScrollFlag() {
        SupportWebView view = getView();
        if (view == null) {
            return 0x0;
        }

        int viewScrollPix = view.getScrollY();
        int viewHeightPix = view.getHeight();
        int contentHeightPix = ((int) (view.getWebScale() * view.getContentHeight()));

        // Viewサイズが不定のためフラグは前回と同じものを返す
        if (contentHeightPix == 0 || viewHeightPix == 0) {
            return mOldScrollFlag;
        }

        if (viewHeightPix >= contentHeightPix) {
            // スクロールが不要である
            return SCROLL_FLAG_TOP | SCROLL_FLAG_BOTTOM;
        }

        double delta = ((double) (viewScrollPix + viewHeightPix)) / ((double) contentHeightPix);
        if (viewScrollPix < 0.01) {
            return SCROLL_FLAG_TOP;
        } else if (delta > 0.99) {
            return SCROLL_FLAG_BOTTOM;
        } else {
            return SCROLL_FLAG_MIDDLE;
        }
    }

    private void nextScrollState() {
        int newState = getScrollFlag();

        // 状態が変化していたら親に通知する
        if (newState != mOldScrollFlag) {
            mCallback.onScrollStateChanged(this, getView().getScrollY(), mOldScrollFlag, newState);
            mOldScrollFlag = newState;
        }
    }

    public interface Callback {
        /**
         * Viewを取得する
         */
        @Nullable
        SupportWebView getWebView(SupportWebViewController self);

        /**
         * スクロール状態が変更された
         *
         * @param scrollY       スクロールYピクセル値
         * @param oldScrollFlag 前回の状態
         * @param newScrollFlag 今回の状態
         */
        void onScrollStateChanged(SupportWebViewController self, int scrollY, int oldScrollFlag, int newScrollFlag);
    }

    public interface Holder {
        @NonNull
        SupportWebViewController getWebContentController();
    }
}
