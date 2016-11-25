package com.eaglesakura.material.widget;

import com.eaglesakura.android.framework.R;
import com.eaglesakura.util.StringUtil;

import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
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

    protected DrawerLayout mDrawerLayout;
    private int mOpenDrawerContentDescRes;
    private int mCloseDrawerContentDescRes;

    /**
     * 生成されるDrawer Toggle
     */
    protected ActionBarDrawerToggle mDrawerToggle;

    public ToolbarBuilder(Toolbar toolbar, AppCompatActivity root) {
        mToolbar = toolbar;
        mRoot = root;
    }

    /**
     * Navigation Drawerを指定する
     */
    public ToolbarBuilder drawer(@IdRes int id, @StringRes int openDrawerContentDescRes, @StringRes int closeDrawerContentDescRes) {
        return drawer((DrawerLayout) mRoot.findViewById(id), openDrawerContentDescRes, closeDrawerContentDescRes);
    }

    /**
     * Navigation Drawerを指定する
     */
    public ToolbarBuilder drawer(DrawerLayout drawerLayout, @StringRes int openDrawerContentDescRes, @StringRes int closeDrawerContentDescRes) {
        mDrawerLayout = drawerLayout;
        mOpenDrawerContentDescRes = openDrawerContentDescRes;
        mCloseDrawerContentDescRes = closeDrawerContentDescRes;
        return this;
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

        ActionBar actionBar = mRoot.getSupportActionBar();

        if (!StringUtil.isEmpty(mTitle)) {
            actionBar.setTitle(mTitle);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        if (mDrawerLayout != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);

            mDrawerToggle = new ActionBarDrawerToggle(
                    mRoot, mDrawerLayout, mToolbar,
                    mOpenDrawerContentDescRes, mCloseDrawerContentDescRes
            );
            mDrawerLayout.addDrawerListener(mDrawerToggle);
            mDrawerToggle.syncState();
        }

        return this;
    }

    public ActionBarDrawerToggle getDrawerToggle() {
        return mDrawerToggle;
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

    public AppCompatActivity getRoot() {
        return mRoot;
    }

    public DrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }

    public static ToolbarBuilder from(AppCompatActivity activity, Toolbar toolbar) {
        return new ToolbarBuilder(toolbar, activity);
    }

    public static ToolbarBuilder from(AppCompatActivity activity) {
        return from(activity, (Toolbar) activity.findViewById(R.id.EsMaterial_Toolbar));
    }

    public static ToolbarBuilder from(Fragment fragment) {
        return from((AppCompatActivity) fragment.getActivity());
    }
}
