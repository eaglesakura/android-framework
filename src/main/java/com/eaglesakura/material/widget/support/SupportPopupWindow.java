package com.eaglesakura.material.widget.support;

import com.eaglesakura.android.device.display.DisplayInfo;
import com.eaglesakura.android.framework.delegate.lifecycle.FragmentLifecycleDelegate;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.lambda.Action1;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v4.widget.PopupWindowCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

/**
 * Android 5.1以下と6.0以上の挙動を適度に合わせるためのPopupWindow Wrapper
 */
public class SupportPopupWindow {
    PopupWindow mWindow;

    int mDummyWidth;

    int mDummyHeight;

    protected boolean isWorkaroundMode() {
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1;
    }

    public SupportPopupWindow(Context context) {
        mWindow = new PopupWindow(context);
        mWindow.setOutsideTouchable(true);
        mWindow.setFocusable(true);
        mWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // 幅と高さを強制的に広げる
        if (isWorkaroundMode()) {
            DisplayInfo info = new DisplayInfo(context);
            mDummyWidth = info.getWidthPixel();
            mDummyHeight = info.getHeightPixel();
            mWindow.setWidth(mDummyWidth);
            mWindow.setHeight(mDummyHeight);
        }
    }

    public SupportPopupWindow contentView(View view) {
        mWindow.setContentView(view);
        return this;
    }

    /**
     * ライフサイクルに合わせて自動的に閉じる
     */
    public SupportPopupWindow autoDismiss(FragmentLifecycleDelegate lifecycleDelegate) {
        lifecycleDelegate.addAutoDismiss(mWindow);
        return this;
    }

    public PopupWindow getWindow() {
        return mWindow;
    }

    /**
     * 何らかの処理をコールバックする
     */
    public SupportPopupWindow action(Action1<SupportPopupWindow> action) {
        try {
            action.action(this);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public SupportPopupWindow showAsDropDown(View anchor) {
        return showAsDropDown(anchor, 0, 0, 0x00);
    }

    public SupportPopupWindow showAsDropDown(View anchor, int xOffset, int yOffset, int gravity) {
        PopupWindowCompat.showAsDropDown(mWindow, anchor, xOffset, yOffset, gravity);
        if (isWorkaroundMode()) {
            UIHandler.postUI(() -> {
                View view = mWindow.getContentView();
                int width = view.getWidth();
                int height = view.getHeight();
                if (width == mDummyWidth && height == mDummyHeight && (view instanceof ViewGroup)) {
                    width = ((ViewGroup) view).getChildAt(0).getWidth();
                    height = ((ViewGroup) view).getChildAt(0).getHeight();
                }
                mWindow.update(anchor, (int) (1.05 * width), (int) (1.05 * height));
            });
        }
        return this;
    }
}
