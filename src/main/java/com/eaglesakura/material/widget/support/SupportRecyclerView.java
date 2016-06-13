package com.eaglesakura.material.widget.support;

import com.eaglesakura.android.framework.FwLog;
import com.eaglesakura.android.framework.R;
import com.eaglesakura.util.StringUtil;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * List Support
 */
public class SupportRecyclerView extends FrameLayout {
    RecyclerView mRecyclerView;

    FrameLayout mEmptyViewRoot;

    View mProgress;

    public SupportRecyclerView(Context context) {
        super(context);
        initialize(context, null, 0, 0);
    }

    public SupportRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0, 0);
    }

    public SupportRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(21)
    public SupportRecyclerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        if (isInEditMode()) {
            return;
        }

        LayoutInflater inflater;
        if (context instanceof Activity) {
            inflater = ((Activity) context).getLayoutInflater();
        } else {
            inflater = LayoutInflater.from(context);
        }
        View view = inflater.inflate(R.layout.esm_support_recyclerview, null);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.EsMaterial_SupportRecyclerView_Content);
        {
            // RecyclerViewにデフォルト状態を指定する
            mRecyclerView.setTag(R.id.SupportRecyclerView_RecyclerView, this);
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        }
        mEmptyViewRoot = (FrameLayout) view.findViewById(R.id.EsMaterial_SupportRecyclerView_Empty);
        mProgress = view.findViewById(R.id.EsMaterial_SupportRecyclerView_Loading);

        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addView(view, layoutParams);

        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SupportRecyclerView);
            String emptyText = typedArray.getString(R.styleable.SupportRecyclerView_emptyText);
            FwLog.widget("SupportRecyclerView_emptyText(%s)", emptyText);

            if (!StringUtil.isEmpty(emptyText)) {
                // empty
                AppCompatTextView tv = new AppCompatTextView(context, attrs, defStyleAttr);
                tv.setText(emptyText);
                tv.setGravity(Gravity.CENTER);
                setEmptyView(tv);
            }
        }
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    /**
     * オブジェクトがカラになった場合のViewを追加する
     */
    public View setEmptyView(@LayoutRes int layoutId) {
        View view = View.inflate(getContext(), layoutId, null);
        setEmptyView(view);
        return view;
    }

    public void setEmptyView(View view) {
        if (mEmptyViewRoot == null) {
            return;
        }

        if (mEmptyViewRoot.getChildCount() != 0) {
            // 子を殺す
            mEmptyViewRoot.removeAllViews();
        }

        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;
        mEmptyViewRoot.addView(view, layoutParams);
    }

    /**
     * 空Viewを取得する
     */
    public <T extends View> T getEmptyView(Class<T> clazz) {
        if (mEmptyViewRoot.getChildCount() == 0) {
            return null;
        }
        return (T) mEmptyViewRoot.getChildAt(0);
    }

    /**
     * アダプタを指定する
     */
    public void setAdapter(RecyclerView.Adapter adapter, boolean viewSizeFixed) {
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setHasFixedSize(viewSizeFixed);
        setProgressVisibly(adapter.getItemCount() == 0, adapter.getItemCount());
    }

    /**
     * プログレスバーの可視状態を設定する
     */
    public void setProgressVisibly(boolean visible, int recyclerViewItemNum) {
        if (visible) {
            mProgress.setVisibility(VISIBLE);
            mRecyclerView.setVisibility(View.INVISIBLE);
            mEmptyViewRoot.setVisibility(View.INVISIBLE);
        } else {
            mProgress.setVisibility(INVISIBLE);
            mRecyclerView.setVisibility(VISIBLE);
            if (recyclerViewItemNum > 0) {
                mEmptyViewRoot.setVisibility(INVISIBLE);
            } else {
                mEmptyViewRoot.setVisibility(VISIBLE);
            }
        }
    }

    /**
     * Adapterの選択位置を取得する
     */
    public static int getSelectedAdapterPosition(RecyclerView view) {
        if (view == null || view.getChildCount() <= 0) {
            return -1;
        }

        return view.getChildAdapterPosition(view.getChildAt(0));
    }
}
