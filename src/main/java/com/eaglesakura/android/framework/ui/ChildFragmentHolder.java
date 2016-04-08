package com.eaglesakura.android.framework.ui;

import com.eaglesakura.util.ReflectionUtil;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class ChildFragmentHolder<T extends Fragment> {
    @Nullable
    T mFragment;

    @NonNull
    String mFragmentTag;

    @NonNull
    FragmentManager mFragmentManager;

    public void onCreate(@Nullable Bundle savedInstanceState, @NonNull FragmentManager fragmentManager,
                         @NonNull Class<? extends T> clazz, @IdRes int holderId, @NonNull String fragmentTag) {
        mFragmentManager = fragmentManager;
        mFragmentTag = fragmentTag;

        if (savedInstanceState == null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            mFragment = ReflectionUtil.newInstanceOrNull(clazz);
            transaction.add(holderId, mFragment, fragmentTag);
            transaction.commit();
        }
    }

    public void onResume() {
        mFragment = (T) mFragmentManager.findFragmentByTag(mFragmentTag);
    }

    @NonNull
    public T get() {
        if (mFragment == null) {
            throw new IllegalStateException();
        }

        return mFragment;
    }

    /**
     * Fragmentを削除する
     */
    public void remove() {
        mFragmentManager.beginTransaction().remove(get()).commit();
        mFragment = null;
    }
}
