package com.eaglesakura.android.framework.delegate.fragment;

import android.support.annotation.IdRes;
import android.support.annotation.IntRange;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class SupportFragmentPager {

    @NonNull
    protected final List<FragmentCreator> mFragments = new ArrayList<>();

    @IdRes
    protected final int mContainerId;

    public SupportFragmentPager(@IdRes int pagerId) {
        mContainerId = pagerId;
    }

    /**
     * Fragment生成器を追加する
     */
    public void addFragment(FragmentCreator creator) {
        mFragments.add(creator);
    }

    /**
     * Fragmentを取得する
     */
    public Fragment getFragment(FragmentManager fragmentManager, int index) {

        // 既存のFragmentを検索する
        String tag = getPagerFragmentTag(index);
        Fragment managedFragment = fragmentManager.findFragmentByTag(tag);
        if (managedFragment != null) {
            return managedFragment;
        } else {
            return mFragments.get(index).newInstance(this);
        }
    }

    public CharSequence getFragmentTitle(FragmentManager fragmentManager, int index) {
        Fragment fragment = getFragment(fragmentManager, index);
        if (fragment instanceof IFragmentPagerTitle) {
            return ((IFragmentPagerTitle) fragment).getTitle();
        } else {
            return null;
        }
    }

    /**
     * Fragment数を取得する
     */
    @IntRange(from = 0)
    public int size() {
        return mFragments.size();
    }

    /**
     * Adapterを生成する
     */
    @NonNull
    public FragmentPagerAdapter newAdapter(FragmentManager fragmentManager) {
        return new FragmentPagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                return getFragment(fragmentManager, position);
            }

            @Override
            public int getCount() {
                return size();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return getFragmentTitle(fragmentManager, position);
            }
        };
    }

    public static FragmentCreator newFragmentCreator(Class<? extends Fragment> clazz) {
        return (self) -> {
            try {
                return clazz.newInstance();
            } catch (Throwable e) {
                e.printStackTrace();
                throw new IllegalStateException(e);
            }
        };
    }

    private String getPagerFragmentTag(int index) {
        return "android:switcher:" + mContainerId + ":" + index;
    }

    public interface FragmentCreator {
        /**
         * Fragmentを新たに生成する
         */
        @NonNull
        Fragment newInstance(SupportFragmentPager self);
    }
}
