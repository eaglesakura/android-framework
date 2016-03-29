package com.eaglesakura.material.widget.support;

import com.eaglesakura.android.framework.FwLog;
import com.eaglesakura.android.framework.R;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.StringUtil;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * List Support
 */
public class SupportRecyclerView extends FrameLayout {
    RecyclerView recyclerView;

    FrameLayout emptyViewRoot;

    View progress;

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

        View view = View.inflate(context, R.layout.esm_support_recyclerview, null);

        recyclerView = (RecyclerView) view.findViewById(R.id.EsMaterial_SupportRecyclerView_Content);
        {
            // RecyclerViewにデフォルト状態を指定する
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setHasFixedSize(false);
        }
        emptyViewRoot = (FrameLayout) view.findViewById(R.id.EsMaterial_SupportRecyclerView_Empty);
        progress = view.findViewById(R.id.EsMaterial_SupportRecyclerView_Loading);

        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addView(view, layoutParams);

        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SupportRecyclerView);
            String emptyText = typedArray.getString(R.styleable.SupportRecyclerView_emptyText);
            FwLog.widget("SupportRecyclerView_emptyText(%s)", emptyText);

            if (!StringUtil.isEmpty(emptyText)) {
                // empty
                TextView tv = new TextView(context, null, R.style.EsMaterial_Font_Normal);
                tv.setText(emptyText);
                tv.setGravity(Gravity.CENTER);
                setEmptyView(tv);
            }
        }
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public void setEmptyView(@LayoutRes int layoutId) {
        setEmptyView(View.inflate(getContext(), layoutId, null));
    }

    public void setEmptyView(View view) {
        if (emptyViewRoot == null) {
            return;
        }

        if (emptyViewRoot.getChildCount() != 0) {
            // 子を殺す
            emptyViewRoot.removeAllViews();
        }

        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;
        emptyViewRoot.addView(view, layoutParams);
    }

    /**
     * 空Viewを取得する
     */
    public <T extends View> T getEmptyView(Class<T> clazz) {
        if (emptyViewRoot.getChildCount() == 0) {
            return null;
        }
        return (T) emptyViewRoot.getChildAt(0);
    }

    /**
     * アダプタを指定する
     */
    public void setAdapter(RecyclerView.Adapter adapter, boolean viewSizeFixed) {
        recyclerView.setAdapter(adapter);
        setProgressVisibly(adapter.getItemCount() == 0, adapter.getItemCount());
    }

    /**
     * プログレスバーの可視状態を設定する
     */
    public void setProgressVisibly(boolean visible, int recyclerViewItemNum) {
        if (visible) {
            progress.setVisibility(VISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);
            emptyViewRoot.setVisibility(View.INVISIBLE);
        } else {
            progress.setVisibility(INVISIBLE);
            recyclerView.setVisibility(VISIBLE);
            if (recyclerViewItemNum > 0) {
                emptyViewRoot.setVisibility(INVISIBLE);
            } else {
                emptyViewRoot.setVisibility(VISIBLE);
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
