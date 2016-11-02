package com.eaglesakura.material.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Animation動作を行うためのFrameLayout
 */
public class AnimationFrameLayout extends FrameLayout {
    public AnimationFrameLayout(Context context) {
        super(context);
    }

    public AnimationFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AnimationFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressLint("all")
    public AnimationFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @SuppressLint("all")
    public float getXFraction() {
        final int width = getWidth();
        if (width != 0) return getX() / getWidth();
        else return getX();
    }

    @SuppressLint("all")
    public void setXFraction(float xFraction) {
        final int width = getWidth();
        //noinspection ResourceType
        setX((width > 0) ? (xFraction * width) : -9999);
    }
}
