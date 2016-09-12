package com.eaglesakura.material.widget;

import com.eaglesakura.android.framework.R;

import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * アプリのToolbar(ActionBar）を構築する
 *
 * Toolbarは"@+id/EsMaterial_Toolbar"をIDに設定しておく
 */
public class ToolbarBuilder {
    protected Toolbar mToolbar;

    protected AppCompatActivity mRoot;

    protected String mTitle;

    public ToolbarBuilder(Toolbar toolbar, AppCompatActivity root) {
        mToolbar = toolbar;
        mRoot = root;
    }

    public ToolbarBuilder title(String title) {
        mTitle = title;
        return this;
    }

    public ToolbarBuilder title(@StringRes int resId) {
        return title(mRoot.getString(resId));
    }

    public ToolbarBuilder build() {
        mRoot.setSupportActionBar(mToolbar);
        mRoot.setTitle(mTitle);
        return this;
    }

    public static ToolbarBuilder from(AppCompatActivity activity) {
        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.EsMaterial_Toolbar);
        return new ToolbarBuilder(toolbar, activity);
    }

    public static ToolbarBuilder from(Fragment fragment) {
        return from((AppCompatActivity) fragment.getActivity());
    }
}
