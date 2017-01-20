package com.eaglesakura.android.framework.ui.support;

import com.eaglesakura.android.framework.R;

import android.animation.Animator;
import android.content.Context;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

/**
 * Viewのアニメーション切り替えを行う
 */
public class SupportViewAnimationBuilder {

    @NonNull
    Context mContext;

    @NonNull
    View mView;

    @NonNull
    ViewPropertyAnimator mAnimate;

    SupportViewAnimationBuilder(@NonNull View view) {
        mContext = view.getContext();
        mView = view;
        mAnimate = view.animate();
        mAnimate.cancel();
        mAnimate.setListener(null);

        // デフォルト値を指定する
        this
                .duration(mContext.getResources().getInteger(R.integer.EsMaterial_Animate_Duration))
                .interpolatorFastOutSlowIn();
    }

    ViewGroup getParent() {
        return ((ViewGroup) mView.getParent());
    }

    /**
     * Layoutの上側に遷移させる
     */
    public SupportViewAnimationBuilder toLayoutUpper() {
        mAnimate.y(-mView.getHeight());
        return this;
    }

    /**
     * 現在のView1個分下げる
     */
    public SupportViewAnimationBuilder toViewLower() {
        mAnimate.y(mView.getHeight());
        return this;
    }

    public SupportViewAnimationBuilder toViewLower(float mult) {
        mAnimate.y(mView.getHeight() * mult);
        return this;
    }

    /**
     * Layoutの指定位置から遷移させる
     */
    public SupportViewAnimationBuilder fromLayoutUpper() {
        return fromLayoutY(-mView.getHeight());
    }

    public SupportViewAnimationBuilder toLayoutTop() {
        return toLayoutY(0);
    }

    public SupportViewAnimationBuilder fromLayoutTop() {
        return fromLayoutY(0);
    }

    /**
     * Layoutの指定位置に遷移させる
     */
    public SupportViewAnimationBuilder toLayoutY(int y) {
        mAnimate.y(y);
        return this;
    }

    public SupportViewAnimationBuilder fromLayoutY(int y) {
        mAnimate.yBy(y);
        mView.setY(y);
        return this;
    }

    /**
     * Layoutの下、画面内に遷移させる
     */
    public SupportViewAnimationBuilder toLayoutBottom() {
        mAnimate.y(getParent().getHeight() - mView.getHeight());
        return this;
    }

    public SupportViewAnimationBuilder fromLayoutBottom() {
        return fromLayoutY(getParent().getHeight() - mView.getHeight());
    }

    /**
     * Layoutの下、画面外に遷移させる
     */
    public SupportViewAnimationBuilder toLayoutLower() {
        mAnimate.y(getParent().getHeight());
        return this;
    }

    public SupportViewAnimationBuilder fromLayoutLower() {
        return fromLayoutY(getParent().getHeight());
    }

    public SupportViewAnimationBuilder fromAlpha(@FloatRange(from = 0, to = 1) float alpha) {
        mView.setAlpha(alpha);
        mAnimate.alphaBy(alpha);
        return this;
    }

    public SupportViewAnimationBuilder toAlpha(@FloatRange(from = 0, to = 1) float alpha) {
        mAnimate.alpha(alpha);
        return this;
    }

    public SupportViewAnimationBuilder fromScale(float scale) {
        mAnimate.scaleXBy(scale);
        mAnimate.scaleYBy(scale);
        mView.setScaleX(scale);
        mView.setScaleY(scale);
        return this;
    }

    public SupportViewAnimationBuilder toScale(float scale) {
        mAnimate.scaleX(scale);
        mAnimate.scaleY(scale);
        return this;
    }


    public SupportViewAnimationBuilder duration(int ms) {
        mAnimate.setDuration(ms);
        return this;
    }

    public SupportViewAnimationBuilder interpolatorFastOutSlowIn() {
        return interpolator(new FastOutSlowInInterpolator());
    }

    public SupportViewAnimationBuilder interpolatorLinear() {
        return interpolator(new LinearInterpolator());
    }

    public SupportViewAnimationBuilder interpolator(Interpolator interpolator) {
        mAnimate.setInterpolator(interpolator);
        return this;
    }

    /**
     * Viewをアニメーション付きで表示する
     */
    public SupportViewAnimationBuilder show() {
        if (mView.getVisibility() == View.VISIBLE) {
            // すでに表示されている
            return this;
        }
        start();
        mView.setVisibility(View.VISIBLE);
        return this;
    }

    /**
     * Viewをアニメーション付きで非表示にする
     */
    public SupportViewAnimationBuilder hide() {
        if (mView.getVisibility() != View.VISIBLE) {
            // すでに隠れている
            return this;
        }
        mAnimate.setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mView.setClickable(false);
        start();
        return this;
    }

    public SupportViewAnimationBuilder start() {
        mAnimate.start();
        return this;
    }

    public static SupportViewAnimationBuilder from(View view) {
        return new SupportViewAnimationBuilder(view);
    }

}
