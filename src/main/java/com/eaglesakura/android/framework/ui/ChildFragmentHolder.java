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

public class ChildFragmentHolder<T extends Fragment> {
    @Nullable
    T mFragment;

    @NonNull
    String mFragmentTag;

    @NonNull
    final Class<? extends T> mClass;

    @NonNull
    final Object mParent;

    @IdRes
    final int mHolderId;

    public ChildFragmentHolder(@NonNull Fragment parent, @NonNull Class<? extends T> aClass, @IdRes int holderId) {
        this(parent, aClass, holderId, aClass.getName());
    }

    public ChildFragmentHolder(@NonNull Activity parent, @NonNull Class<? extends T> aClass, @IdRes int holderId, @NonNull String tag) {
        mParent = parent;
        mClass = aClass;
        mHolderId = holderId;
        mFragmentTag = tag;
    }

    public ChildFragmentHolder(@NonNull Fragment parent, @NonNull Class<? extends T> aClass, @IdRes int holderId, @NonNull String tag) {
        mParent = parent;
        mClass = aClass;
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
    protected T newFragmentInstance(@Nullable Bundle savedInstanceState) throws Exception {
        return mClass.newInstance();
    }

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
     * コンテンツを切り替える
     */
    public <T2 extends T> void replace(@NonNull T2 fragment) {
        get(); // assert

        getFragmentManager()
                .beginTransaction()
                .replace(mHolderId, fragment, mFragmentTag)
                .commit();
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
}
