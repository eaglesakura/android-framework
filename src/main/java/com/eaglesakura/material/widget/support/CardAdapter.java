package com.eaglesakura.material.widget.support;

import com.eaglesakura.android.framework.R;
import com.eaglesakura.android.util.ViewUtil;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * RecyclerViewのシンプルなカードを実現する
 */
public abstract class CardAdapter<T> extends RecyclerView.Adapter<CardAdapter.CardItem> {

    /**
     * カード用アイテム
     */
    private List<T> mCardModels = new ArrayList<>(32);

    SupportRecyclerView mSupportRecyclerView;

    @Override
    public CardItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View card = onCreateCard(parent, viewType);
        return new CardItem(card);
    }

    @Override
    public void onBindViewHolder(CardItem holder, int position) {
        ViewUtil.matchCardWidth(holder.itemView);
        onBindCard(holder.itemView, position, mCardModels.get(position));
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        Object tag = recyclerView.getTag(R.id.SupportRecyclerView_RecyclerView);
        if (tag instanceof SupportRecyclerView) {
            mSupportRecyclerView = (SupportRecyclerView) tag;
        }
    }

    @Override
    public int getItemCount() {
        return mCardModels.size();
    }

    /**
     * カードを連続挿入する場合に挿入する遅延時間（ミリ秒）
     */
    public static int getItemInsertDuration(Context context) {
        return context.getResources().getInteger(R.integer.CardAdapter_Insert_Duration);
    }

    /**
     * 全てのカード用アイテムを削除する
     */
    public void clearAnimated() {
        while (mCardModels.isEmpty()) {
            mCardModels.remove(0);
            notifyItemRemoved(0);
        }
        if (mSupportRecyclerView != null) {
            mSupportRecyclerView.setProgressVisibly(true, 0);
        }
    }

    /**
     * 全てのカード用アイテムをすぐさま削除する
     */
    public void clear() {
        mCardModels.clear();
        notifyDataSetChanged();
        if (mSupportRecyclerView != null) {
            mSupportRecyclerView.setProgressVisibly(true, 0);
        }
    }

    /**
     * アイテムを削除する
     */
    public void remove(T item) {
        int index = mCardModels.indexOf(item);
        if (index < 0) {
            throw new IllegalArgumentException();
        }

        remove(index);
    }

    /**
     * 指定した位置のアイテムを削除する
     */
    public void remove(int index) {
        mCardModels.remove(index);
        notifyItemRemoved(index);
        if (mSupportRecyclerView != null && mCardModels.isEmpty()) {
            mSupportRecyclerView.setProgressVisibly(true, 0);
        }
    }

    /**
     * アイテムを更新するか、指定位置に追加する
     *
     * itemのhashCode/equalsをオーバーライドし、重複チェックが行える状態でなければならない。
     *
     * @param item 挿入するアイテム
     * @return アイテムの存在する位置
     */
    public int insertOrReplace(T item) {
        return insertOrReplace(mCardModels.size(), item);
    }

    /**
     * アイテムを更新するか、指定位置に追加する
     *
     * itemのhashCode/equalsをオーバーライドし、重複チェックが行える状態でなければならない。
     *
     * @param index 既存アイテムがない場合の挿入位置
     * @param item  挿入するアイテム
     * @return アイテムの存在する位置
     */
    public int insertOrReplace(int index, T item) {
        int oldIndex = mCardModels.indexOf(item);
        if (oldIndex < 0) {
            // 既存アイテムが無いので挿入する
            addItem(index, item);
            return mCardModels.size() - 1;
        } else {
            T oldItem = mCardModels.get(oldIndex);
            if (isNotifyItemChanged(oldItem, item)) {
                // データに変動があったため交換する
                mCardModels.set(oldIndex, item);
                notifyItemChanged(oldIndex);
            }
            return oldIndex;
        }
    }

    /**
     * 2つのデータに変更があったかを確認する。
     *
     * @param oldItem 古いデータ
     * @param newItem 新しいデータ
     */
    protected boolean isNotifyItemChanged(@Nullable T oldItem, @Nullable T newItem) {
        return true;
    }

    /**
     * カード用のアイテムを追加する
     */
    public void addItem(T item) {
        if (mSupportRecyclerView != null && mCardModels.isEmpty()) {
            mSupportRecyclerView.setProgressVisibly(false, 1);
        }

        mCardModels.add(item);
        notifyItemInserted(mCardModels.size() - 1);
    }

    /**
     * カード用のアイテムをまとめて追加する
     */
    public void addItems(Collection<T> item) {
        if (mSupportRecyclerView != null && mCardModels.isEmpty()) {
            mSupportRecyclerView.setProgressVisibly(false, 1);
        }

        for (T it : item) {
            addItem(it);
        }
    }

    /**
     * カード用のアイテムを挿入する
     */
    public void addItem(int index, T item) {
        if (mSupportRecyclerView != null && mCardModels.isEmpty()) {
            mSupportRecyclerView.setProgressVisibly(false, 1);
        }

        mCardModels.add(index, item);
        notifyItemInserted(index);
    }

    /**
     * 表示用のViewを生成する
     */
    protected abstract View onCreateCard(ViewGroup parent, int viewType);

    /**
     * 表示用のViewを構築する
     */
    protected abstract void onBindCard(View card, int position, T item);

    public static class CardItem extends RecyclerView.ViewHolder {
        public CardItem(View itemView) {
            super(itemView);
        }
    }
}
