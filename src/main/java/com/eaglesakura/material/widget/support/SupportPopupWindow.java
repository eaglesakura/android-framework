package com.eaglesakura.material.widget.support;

import com.eaglesakura.android.device.display.DisplayInfo;
import com.eaglesakura.android.framework.FwLog;
import com.eaglesakura.android.framework.delegate.lifecycle.FragmentLifecycleDelegate;
import com.eaglesakura.android.util.ViewUtil;
import com.eaglesakura.lambda.Action1;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.widget.PopupWindowCompat;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;

/**
 * Android 5.1以下と6.0以上の挙動を適度に合わせるためのPopupWindow Wrapper
 */
public class SupportPopupWindow {
    Context mContext;

    PopupWindow mWindow;

    int mContentWidth;

    int mContentHeight;

    protected boolean isWorkaroundMode() {
//        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1;
        return true;
    }

    public SupportPopupWindow(Context context) {
        mContext = context;
        mWindow = new PopupWindow(context);
        mWindow.setOutsideTouchable(true);
        mWindow.setFocusable(true);
        mWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

//        // 幅と高さを強制的に広げる
//        if (isWorkaroundMode()) {
//            DisplayInfo info = new DisplayInfo(context);
//            mDummyWidth = info.getWidthPixel();
//            mDummyHeight = info.getHeightPixel();
//            mWindow.setWidth(mDummyWidth);
//            mWindow.setHeight(mDummyHeight);
//        }
    }

    public SupportPopupWindow contentView(View view) {
        mWindow.setContentView(view);
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        mContentWidth = view.getMeasuredWidth();
        mContentHeight = view.getMeasuredHeight();
        FwLog.widget("Window size[%dx%d]", mContentWidth, mContentHeight);
        mWindow.setWidth((int) (mContentWidth * 1.05));
        mWindow.setHeight((int) (mContentHeight * 1.05));
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

    /**
     * 計算した固定座標にPopupを表示する。
     * RecyclerView内部等、正常にPopupが表示されない場合に対するワークアラウンドとして利用する。
     */
    public SupportPopupWindow showAsDropDownAbs(View anchor) {

        Rect area = ViewUtil.getScreenArea(anchor);
        DisplayInfo display = new DisplayInfo(mContext);

        int xOffset = 0;
        int yOffset = 0;

        if ((area.bottom + mContentHeight) > display.getHeightPixel()) {
            // ウィンドウがはみ出す場合はViewの上に乗せる
            FwLog.widget("Window Popup => Top");
            yOffset = -(area.height() + mWindow.getHeight());
        }

        int[] viewXY = new int[2];
        anchor.getLocationInWindow(viewXY);
        mWindow.showAtLocation(anchor, Gravity.LEFT | Gravity.TOP, viewXY[0], viewXY[1] + area.height() + yOffset);
        return this;
    }

    public SupportPopupWindow showAsDropDown(View anchor) {
        return showAsDropDown(anchor, 0, 0, 0x00);
    }


    public SupportPopupWindow showAsDropDown(View anchor, int xOffset, int yOffset, int gravity) {
        PopupWindowCompat.showAsDropDown(mWindow, anchor, xOffset, yOffset, gravity);
        return this;
    }
}
