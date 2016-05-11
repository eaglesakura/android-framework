package com.eaglesakura.android.framework.ui;

import com.eaglesakura.util.CollectionUtil;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class BackStackManager implements Parcelable {

    List<String> mStackTags = new LinkedList<>();

    public BackStackManager() {
    }

    public BackStackManager(Parcel in) {
        in.readStringList(mStackTags);
    }

    /**
     * ハンドリング対象を先頭に追加する
     */
    @UiThread
    public void push(Fragment fragment) {
        if (fragment instanceof BackStackFragment) {
            push(fragment.getTag());
        } else {
            throw new IllegalArgumentException("!(fragment instanceof BackStackFragment)");
        }
    }

    void push(String tag) {
        mStackTags.add(0, tag);
    }

    /**
     * ハンドリング対象を一つ取り出す
     */
    @UiThread
    public void pop(Fragment fragment) {
        if (fragment instanceof BackStackFragment) {
            pop(fragment.getTag());
        } else {
            throw new IllegalArgumentException("!(fragment instanceof BackStackFragment)");
        }
    }

    void pop(String tag) {
        if (mStackTags.isEmpty()) {
            throw new IllegalStateException();
        }

        Iterator<String> iterator = mStackTags.iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            if (next.equals(tag)) {
                iterator.remove();
                return;
            }
        }
    }

    protected Fragment findFragmentByTag(FragmentManager fragmentManager, String tag) {
        List<Fragment> fragments = fragmentManager.getFragments();
        if (CollectionUtil.isEmpty(fragments)) {
            // 管理対象がない
            return null;
        }

        for (Fragment fragment : fragments) {
            if (fragment == null) {
                continue;
            }
            if (tag.equals(fragment.getTag())) {
                // tagが一致した
                return fragment;
            } else {
                // 子を調べる
                Fragment child = findFragmentByTag(fragment.getChildFragmentManager(), tag);
                if (child != null) {
                    // ハンドリング対象の子を見つけた
                    return child;
                }
            }
        }

        // 発見できなかった
        return null;
    }

    /**
     * 戻るボタンが押された
     */
    @UiThread
    public boolean onBackPressed(FragmentManager fragmentManager, KeyEvent event) {
        if (mStackTags.isEmpty()) {
            return false;
        }

        Iterator<String> iterator = mStackTags.iterator();
        while (iterator.hasNext()) {
            String tag = iterator.next();
            Fragment handleTarget = findFragmentByTag(fragmentManager, tag);
            if (handleTarget instanceof BackStackFragment) {
                if (((BackStackFragment) handleTarget).onBackPressed(event)) {
                    // ハンドリングしたので何もしない
                    return true;
                }
            } else {
                // 上階層を廃棄して、次を探す
                iterator.remove();
            }
        }

        // ハンドリングできなかった
        return false;
    }

    public static final Creator<BackStackManager> CREATOR
            = new Creator<BackStackManager>() {
        public BackStackManager createFromParcel(Parcel in) {
            return new BackStackManager(in);
        }

        public BackStackManager[] newArray(int size) {
            return new BackStackManager[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(mStackTags);
    }

    public interface BackStackFragment {
        /**
         * バックキーが押された。
         * Fragmentを残す場合はtrueを返却し、それ以外はfalseを返却する
         */
        boolean onBackPressed(KeyEvent event);
    }
}
