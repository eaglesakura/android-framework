package com.eaglesakura.android.framework.ui.support;

import com.eaglesakura.android.framework.delegate.activity.ContentHolderActivityDelegate;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

/**
 * 親となるFragmentを指定して起動するActivityの雛形
 * <p/>
 * メインコンテンツは @+id/Content_Holder_Root を持たなければならない。
 * Toolbarは @+id/EsMaterial_Toolbar を自動的に検索し、存在するならToolbarとして自動設定する。
 */
public abstract class ContentHolderActivity extends SupportActivity implements ContentHolderActivityDelegate.ContentHolderActivityCompat {
    protected final ContentHolderActivityDelegate mContentHolderDelegate = new ContentHolderActivityDelegate(this, mLifecycleDelegate);

    public ContentHolderActivity() {
    }

    public Fragment getContentFragment() {
        return mContentHolderDelegate.getContentFragment();
    }

    public <T extends Fragment> T getContentFragment(Class<T> clazz) {
        return (T) mContentHolderDelegate.getContentFragment();
    }

    @NonNull
    @Override
    public FragmentManager getFragmentManager(@NonNull ContentHolderActivityDelegate self) {
        return getSupportFragmentManager();
    }

    @NonNull
    @Override
    public Activity getActivity(@NonNull ContentHolderActivityDelegate self) {
        return this;
    }
}
