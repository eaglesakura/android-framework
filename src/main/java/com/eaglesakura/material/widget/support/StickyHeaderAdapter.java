package com.eaglesakura.material.widget.support;

import com.eaglesakura.material.widget.adapter.CardAdapter;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Sticky Header Util
 */
public abstract class StickyHeaderAdapter<T> implements StickyRecyclerHeadersAdapter {

    private final CardAdapter<T> mItemAdapter;

    public StickyHeaderAdapter(CardAdapter<T> itemAdapter) {
        mItemAdapter = itemAdapter;
    }

    @Override
    public final long getHeaderId(int position) {
        // IDは負の値の場合ヘッダ無しとなる。
        // 標準では下位32bitのみを使用することで必ずヘッダを入れ込むようにする
        return ((long) getHeaderId(position, mItemAdapter.getCollection().get(position))) & 0x00000000FFFFFFFFL;
    }

    @Override
    public final RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        return new HeaderItem(onCreateHeader(parent));
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
        onBindHeader(holder.itemView, position, mItemAdapter.getCollection().get(position));
    }

    @Override
    public int getItemCount() {
        return mItemAdapter.getItemCount();
    }

    /**
     * 表示するヘッダグルーピングIDを取得する
     */
    protected abstract int getHeaderId(int position, T item);


    /**
     * 表示用のViewを生成する
     */
    protected abstract View onCreateHeader(ViewGroup parent);

    /**
     * 表示用のViewを構築する
     */
    protected abstract void onBindHeader(View header, int position, T item);

    static class HeaderItem extends RecyclerView.ViewHolder {
        public HeaderItem(View itemView) {
            super(itemView);
        }
    }
}
