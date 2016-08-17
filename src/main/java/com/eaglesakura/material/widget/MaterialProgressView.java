package com.eaglesakura.material.widget;

import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.framework.R;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

public class MaterialProgressView extends FrameLayout {

    TextView mProgressText;

    public MaterialProgressView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public MaterialProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public MaterialProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    void init(Context context, AttributeSet attrs, int defStyleAttr) {
        View view = LayoutInflater.from(context).inflate(R.layout.esm_view_material_progress, null);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, new int[]{
                R.attr.esmText,
        });

        AQuery q = new AQuery(view)
                .id(R.id.EsMaterial_Progress_Text).text(typedArray.getText(0));
        mProgressText = q.id(R.id.EsMaterial_Progress_Text).getTextView();

        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addView(view, params);
    }

    /**
     * プログレス表示テキストを表示する
     */
    public void setText(CharSequence text) {
        mProgressText.setText(text);
    }

    private void setVisibilityImpl(int visibility) {
        super.setVisibility(visibility);
    }

    @Override
    public void setVisibility(int visibility) {
//        super.setVisibility(visibility);
        if (getVisibility() == visibility) {
            // 同じ表示のため何もしない
            return;
        }

        canAnimate();

        if (visibility == VISIBLE) {
            // 表示を開始する
            Animator animator = AnimatorInflater.loadAnimator(getContext(), R.animator.esm_progress_fade_in);
            animator.setTarget(this);
            animator.start();

            setVisibilityImpl(View.VISIBLE);
        } else {
            // 表示を終了する
            Animator animator = AnimatorInflater.loadAnimator(getContext(), R.animator.esm_progress_fade_out);
            animator.setTarget(this);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    setVisibilityImpl(visibility);
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            animator.start();
        }
    }
}
