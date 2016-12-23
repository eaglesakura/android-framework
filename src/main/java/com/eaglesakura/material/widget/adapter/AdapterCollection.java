package com.eaglesakura.material.widget.adapter;

import com.eaglesakura.lambda.Matcher1;
import com.eaglesakura.util.Util;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Adapter内部で使用することを前提としたUtilリスト
 */
public class AdapterCollection<T> {
    @NonNull
    private final List<T> mItems;

    @NonNull
    private Callback<T> mCallback = new Callback<T>() {
        @Override
        public void onItemInserted(int index, @Nullable T item) {

        }

        @Override
        public void onItemRemoved(int index) {

        }

        @Override
        public void onItemReloaded() {

        }

        @Override
        public void onItemReplaced(int index, @Nullable T item) {

        }
    };

    @NonNull
    private Comparator<T> mComparator = new Comparator<T>() {
        @Override
        public boolean isOverlap(@Nullable T a, @Nullable T b) {
            return Util.equals(a, b);
        }

        @Override
        public boolean isNotificationChanged(@Nullable T oldItem, @Nullable T newItem) {
            return true;
        }
    };

    public AdapterCollection() {
        mItems = new ArrayList<>();
    }

    public AdapterCollection(@NonNull List<T> items, @NonNull Callback<T> callback) {
        if (!items.isEmpty()) {
            throw new IllegalArgumentException();
        }
        mItems = items;
        mCallback = callback;
    }

    public AdapterCollection(@NonNull Callback<T> callback) {
        mCallback = callback;
        mItems = new ArrayList<>();
    }

    public AdapterCollection(@NonNull List<T> items) {
        mItems = items;
    }

    /**
     * コールバックを指定する
     */
    public void setCallback(@NonNull Callback<T> callback) {
        mCallback = callback;
    }

    /**
     * 比較チェックオブジェクトを指定する
     */
    public void setComparator(@NonNull Comparator<T> comparator) {
        mComparator = comparator;
    }

    /**
     * Matcherに一致するアイテムを削除する
     */
    public void remove(Matcher1<T> matcher) {
        try {
            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < size(); ++i) {
                if (matcher.match(mItems.get(i))) {
                    indices.add(i);
                }
            }

            if (indices.isEmpty()) {
                return;
            }

            // 指定したインデックスのアイテムを排除する
            for (int i = (indices.size() - 1); i >= 0; --i) {
                remove(i);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 指定した条件に一致するアイテムを検索する
     */
    @Nullable
    public T find(Matcher1<T> matcher) {
        try {
            for (T item : getItems()) {
                if (matcher.match(item)) {
                    return item;
                }
            }
            return null;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 指定したアイテムが含まれていればtrue
     */
    public boolean contains(@Nullable T item) {
        return indexOf(item) >= 0;
    }

    public boolean isEmpty() {
        return mItems.isEmpty();
    }

    /**
     * アイテム数を取得する
     */
    public int size() {
        return mItems.size();
    }

    @NonNull
    public Iterator<T> iterator() {
        return mItems.iterator();
    }

    @NonNull
    public List<T> getItems() {
        return mItems;
    }

    @Nullable
    public T get(@IntRange(from = 0) int index) {
        return mItems.get(index);
    }

    /**
     * カード用のアイテムを追加する
     */
    public void add(T item) {
        add(mItems.size(), item);
    }

    /**
     * カード用のアイテムを追加する
     */
    public void add(@IntRange(from = 0) int index, T item) {
        mItems.add(index, item);
        mCallback.onItemInserted(index, item);
    }

    /**
     * カード用のアイテムをまとめて追加する
     */
    public void addAll(@NonNull Collection<T> items) {
        mItems.addAll(items);
        mCallback.onItemReloaded();
    }

    /**
     * カード用のアイテムを1個ずつ追加する
     */
    public void addAllAnimated(@NonNull Collection<T> items) {
        for (T it : items) {
            add(it);
        }
    }

    /**
     * 全てのカード用アイテムを削除する
     */
    public void clearAnimated() {
        while (mItems.isEmpty()) {
            mItems.remove(0);
            mCallback.onItemRemoved(0);
        }
    }

    /**
     * 全てのカード用アイテムをすぐさま削除する
     */
    public void clear() {
        mItems.clear();
        mCallback.onItemReloaded();
    }

    /**
     * アイテムを削除する
     */
    public boolean remove(T item) {
        int index = mItems.indexOf(item);
        if (index < 0) {
            return false;
        }

        remove(index);
        return true;
    }

    /**
     * 指定した位置のアイテムを削除する
     */
    public void remove(@IntRange(from = 0) int index) {
        mItems.remove(index);
        mCallback.onItemRemoved(index);
    }

    public void insertOrReplaceAll(Collection<T> items) {
        for (T item : items) {
            insertOrReplace(item);
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
        return insertOrReplace(mItems.size(), item);
    }

    /**
     * アイテムの番号を取得する
     *
     * @param item 確認するアイテム
     * @return 見つかった場合はインデックス、見つからない場合は負の値
     */
    public int indexOf(T item) {
        int index = 0;
        for (T it : mItems) {
            if (mComparator.isOverlap(it, item)) {
                return index;
            }
            ++index;
        }
        return -1;
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
    public int insertOrReplace(@IntRange(from = 0) int index, @Nullable T item) {
        int oldIndex = indexOf(item);
        if (oldIndex < 0) {
            // 既存アイテムが無いので挿入する
            add(index, item);
            return mItems.size() - 1;
        } else {
            T oldItem = mItems.get(oldIndex);
            if (mComparator.isNotificationChanged(oldItem, item)) {
                // データに変動があったため交換する
                mItems.set(oldIndex, item);
                mCallback.onItemReplaced(oldIndex, item);
            }
            return oldIndex;
        }
    }

    public interface Callback<T> {
        /**
         * アイテムが追加された
         *
         * @param index 追加された位置
         * @param item  追加されたアイテム
         */
        void onItemInserted(int index, @Nullable T item);

        /**
         * アイテムが更新された
         *
         * @param index 更新された位置
         * @param item  更新されたアイテム
         */
        void onItemReplaced(int index, @Nullable T item);

        /**
         * アイテムが削除された
         */
        void onItemRemoved(int index);

        /**
         * アイテムに何らかの大きな改変があった
         *
         * 全追加、全削除等
         */
        void onItemReloaded();
    }

    public interface Comparator<T> {
        /**
         * 表示列で同一オブジェクトかどうかを確認する
         *
         * @return trueを返却した場合、同一オブジェクトとみなして位置の上書きを行う
         */
        boolean isOverlap(@Nullable T a, @Nullable T b);

        /**
         * 表示が更新された場合に、コールバックを伴う場合はtrue
         *
         * 変更通知によってView更新が発生する場合はそれを抑制する効果がある
         *
         * @return コールバックを呼び出す場合はtrue
         */
        boolean isNotificationChanged(@Nullable T oldItem, @Nullable T newItem);
    }
}
