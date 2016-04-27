package com.eaglesakura.android.framework.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

public abstract class FragmentHolder<T extends Fragment> {
    @Nullable
    T mFragment;

    @NonNull
    String mFragmentTag;

    @NonNull
    final Object mParent;

    @IdRes
    final int mHolderId;

    public FragmentHolder(@NonNull Activity parent, @IdRes int holderId, @NonNull String tag) {
        mParent = parent;
        mHolderId = holderId;
        mFragmentTag = tag;
    }

    public FragmentHolder(@NonNull Fragment parent, @IdRes int holderId, @NonNull String tag) {
        mParent = parent;
        mHolderId = holderId;
        mFragmentTag = tag;
    }

    private FragmentManager getFragmentManager() {
        if (mParent instanceof AppCompatActivity) {
            return ((AppCompatActivity) mParent).getSupportFragmentManager();
        } else if (mParent instanceof Fragment) {
            return ((Fragment) mParent).getChildFragmentManager();
        }
        throw new IllegalStateException();
    }

    @NonNull
    protected abstract T newFragmentInstance(@Nullable Bundle savedInstanceState) throws Exception;

    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            try {
                mFragment = newFragmentInstance(savedInstanceState);
                if (mFragment == null) {
                    throw new IllegalStateException();
                }
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            transaction.add(mHolderId, mFragment, mFragmentTag).commit();
        }
    }

    public void onResume() {
        mFragment = (T) getFragmentManager().findFragmentByTag(mFragmentTag);
    }

    /**
     * 生成済みである場合はtrue
     */
    public boolean isCreated() {
        return mFragment != null;
    }

    /**
     * コンテンツを切り替える
     */
    public <T2 extends T> void replace(@NonNull T2 fragment) {
        getFragmentManager()
                .beginTransaction()
                .replace(mHolderId, fragment, mFragmentTag)
                .commit();
        mFragment = fragment;
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
        getFragmentManager()
                .beginTransaction()
                .remove(get())
                .commit();

        mFragment = null;
    }

    public static <T extends Fragment> FragmentHolder<T> newInstance(@NonNull Fragment parent, @NonNull Class<? extends T> aClass, @IdRes int holderId) {
        return new FragmentHolder<T>(parent, holderId, aClass.getName()) {
            @NonNull
            @Override
            protected T newFragmentInstance(@Nullable Bundle savedInstanceState) throws Exception {
                return aClass.newInstance();
            }
        };
    }


    public static <T extends Fragment> FragmentHolder<T> newInstance(@NonNull Activity parent, @NonNull Class<? extends T> aClass, @IdRes int holderId) {
        return new FragmentHolder<T>(parent, holderId, aClass.getName()) {
            @NonNull
            @Override
            protected T newFragmentInstance(@Nullable Bundle savedInstanceState) throws Exception {
                return aClass.newInstance();
            }
        };
    }


}
