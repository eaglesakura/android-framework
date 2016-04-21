package com.eaglesakura.material.widget.adapter;

import com.eaglesakura.android.framework.R;
import com.eaglesakura.android.framework.ui.support.SupportAQuery;
import com.eaglesakura.android.util.ViewUtil;
import com.eaglesakura.material.widget.support.SupportRecyclerView;
import com.eaglesakura.util.Util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * RecyclerViewのシンプルなカードを実現する
 */
public abstract class CardAdapter<T> extends RecyclerView.Adapter<CardAdapter.CardItem> {

    @NonNull
    private AdapterCollection<T> mCollection;

    SupportRecyclerView mSupportRecyclerView;

    public CardAdapter(@NonNull AdapterCollection<T> collection) {
        mCollection = collection;
    }

    public CardAdapter() {
        this(new AdapterCollection<>());
    }

    @NonNull
    public AdapterCollection<T> getCollection() {
        return mCollection;
    }

    @Override
    public CardItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View card = onCreateCard(parent, viewType);
        return new CardItem(card);
    }

    @Override
    public void onBindViewHolder(CardItem holder, int position) {
        ViewUtil.matchCardWidth(holder.itemView);
        onBindCard(new CardBind<>(holder.itemView, mCollection.get(position)), position);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        mCollection.setCallback(newAdapterCallback());
        mCollection.setComparator(newAdapterComparator());

        Object tag = recyclerView.getTag(R.id.SupportRecyclerView_RecyclerView);
        if (tag instanceof SupportRecyclerView) {
            mSupportRecyclerView = (SupportRecyclerView) tag;
        }
    }

    @Override
    public int getItemCount() {
        return mCollection.size();
    }

    /**
     * カードを連続挿入する場合に挿入する遅延時間（ミリ秒）
     */
    public static int getItemInsertDuration(Context context) {
        return context.getResources().getInteger(R.integer.CardAdapter_Insert_Duration);
    }

    /**
     * 表示用のViewを生成する
     */
    protected abstract View onCreateCard(ViewGroup parent, int viewType);

    /**
     * 表示用のViewを構築する
     */
    protected abstract void onBindCard(CardBind<T> bind, int position);

    public static class CardBind<T> {
        @NonNull
        private final View mCard;

        @NonNull
        private final T mItem;

        CardBind(@NonNull View card, @NonNull T item) {
            mCard = card;
            mItem = item;

            mCard.setTag(R.id.CardAdapter_Item, item);
        }

        /**
         * 正しくバインドされたままであればtrueを返却する
         */
        public boolean isBinded() {
            return mCard.getTag(R.id.CardAdapter_Item) == mItem;
        }

        @NonNull
        public View getCard() {
            if (!isBinded()) {
                throw new IllegalStateException();
            }

            return mCard;
        }

        @NonNull
        public T getItem() {
            return mItem;
        }

        @UiThread
        @NonNull
        public SupportAQuery query() {
            if (!isBinded()) {
                throw new IllegalStateException();
            }

            return new SupportAQuery(mCard);
        }
    }

    /**
     * Adapter用のコールバックを設定する
     */
    protected AdapterCollection.Callback<T> newAdapterCallback() {
        return new AdapterCollection.Callback<T>() {
            @Override
            public void onItemInserted(int index, @Nullable T item) {
                if (mSupportRecyclerView != null) {
                    mSupportRecyclerView.setProgressVisibly(false, mCollection.size());
                }
                notifyItemInserted(index);
            }

            @Override
            public void onItemReplaced(int index, @Nullable T item) {
                notifyItemChanged(index);
            }

            @Override
            public void onItemRemoved(int index) {
                notifyItemRemoved(index);
                if (mSupportRecyclerView != null && mCollection.isEmpty()) {
                    mSupportRecyclerView.setProgressVisibly(false, 0);
                }
            }

            @Override
            public void onItemReloaded() {
                notifyDataSetChanged();
                if (mSupportRecyclerView != null) {
                    mSupportRecyclerView.setProgressVisibly(false, mCollection.size());
                }
            }
        };
    }

    /**
     * Adapter用の比較クラスを生成する
     */
    protected AdapterCollection.Comparator<T> newAdapterComparator() {
        return new AdapterCollection.Comparator<T>() {
            @Override
            public boolean isOverlap(@Nullable T a, @Nullable T b) {
                return Util.equals(a, b);
            }

            @Override
            public boolean isNotificationChanged(@Nullable T oldItem, @Nullable T newItem) {
                return true;
            }
        };
    }

    static class CardItem extends RecyclerView.ViewHolder {
        public CardItem(View itemView) {
            super(itemView);
        }
    }
}
