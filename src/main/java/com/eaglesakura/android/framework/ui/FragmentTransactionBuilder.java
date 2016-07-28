package com.eaglesakura.android.framework.ui;

import com.eaglesakura.android.framework.R;
import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentDelegate;
import com.eaglesakura.android.framework.ui.support.SupportFragment;
import com.eaglesakura.util.StringUtil;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment Transactionを制御する
 */
public class FragmentTransactionBuilder {
    public enum AnimationType {
        /**
         * 新しいFragmentが右から下に移動する
         */
        TranslateHorizontal,

        /**
         * 新しいFragmentが下から上にせり上がる
         */
        TranslateVerticalUp,

        /**
         *
         */
        TranslateVerticalDown,

        /**
         * 通常のフェード
         */
        Fade,

        /**
         * カスタマイズされたフェード
         *
         * MEMO: 通常のフェードはクロスフェードのため、背景が透ける場合がある。
         * それを避けたい場合、こちらのフェードを使用する
         */
        CustomFade,

        /**
         * アニメーションなし
         */
        None,
    }

    protected final Activity activity;

    protected final Fragment fragment;

    protected final FragmentTransaction transaction;

    protected final FragmentManager fragmentManager;

    /**
     * バックスタック数を数え、不要であればanimationを行わない
     */
    protected boolean checkBackstackCount = true;

    /**
     * コミット対象のFragments
     */
    protected List<Fragment> fragments = new ArrayList<Fragment>();

    public FragmentTransactionBuilder(Fragment currentFragment, FragmentManager fragmentManager) {
        this.fragment = currentFragment;
        this.activity = fragment.getActivity();
        this.fragmentManager = fragmentManager;
        this.transaction = fragmentManager.beginTransaction();
    }

    public FragmentTransactionBuilder(Activity activity, FragmentManager fragmentManager) {
        this.fragment = null;
        this.activity = activity;
        this.fragmentManager = fragmentManager;
        this.transaction = fragmentManager.beginTransaction();
    }

    public FragmentTransactionBuilder checkBackstackCount(boolean checkBackstackCount) {
        this.checkBackstackCount = checkBackstackCount;
        return this;
    }

    public FragmentTransactionBuilder animation(int enter, int exit) {
        animation(enter, exit, 0, 0);
        return this;
    }

    public FragmentTransactionBuilder animation(int enter, int exit,
                                                int popEnter, int popExit) {
        if (checkBackstackCount) {
            if (fragmentManager.getBackStackEntryCount() == 0) {
                return this;
            }
        }

        if (popEnter == 0 || popExit == 0) {
            transaction.setCustomAnimations(enter, exit);
        } else {
            transaction.setCustomAnimations(enter, exit, popEnter, popExit);
        }
        return this;
    }

    public FragmentTransactionBuilder animation(AnimationType type) {
        return animation(type, type);
    }

    /**
     * setup animation
     */
    public FragmentTransactionBuilder animation(AnimationType enterAnimation, AnimationType popAnimation) {
        int enter = 0;
        int exit = 0;
        int popEnter = 0;
        int popExit = 0;

        switch (enterAnimation) {
            case Fade:
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                return this;
            case CustomFade:
                enter = R.anim.fragment_fade_enter;
                exit = R.anim.fragment_fade_exit;
                break;
            case TranslateHorizontal:
                enter = R.anim.fragment_horizontal_enter;
                exit = R.anim.fragment_horizontal_exit;
                break;
            case TranslateVerticalUp:
                // TODO XML追加
                throw new IllegalStateException();
        }

        if (popAnimation != null) {
            switch (popAnimation) {
                case CustomFade:
                    popEnter = R.anim.fragment_fade_popenter;
                    popExit = R.anim.fragment_fade_popexit;
                    break;
                case TranslateHorizontal:
                    popEnter = R.anim.fragment_horizontal_popenter;
                    popExit = R.anim.fragment_horizontal_popexit;
                    break;
                case TranslateVerticalUp:
                    // TODO XML追加
                    throw new IllegalStateException();
            }
        }

        return animation(enter, exit, popEnter, popExit);
    }

    public FragmentTransactionBuilder add(Fragment fragment) {
        return add(fragment, null);
    }

    public FragmentTransactionBuilder add(Fragment fragment, String tag) {
        if (StringUtil.isEmpty(tag)) {
            tag = fragment.getClass().getName() + "/" + fragment.hashCode();
        }

        transaction.add(fragment, tag);
        return this;
    }

    public FragmentTransactionBuilder add(int container, Fragment fragment, String tag) {
        if (StringUtil.isEmpty(tag)) {
            transaction.add(container, fragment);
        } else {
            transaction.add(container, fragment, tag);
        }

        fragments.add(fragment);
        return this;
    }

    /**
     * 指定したコンテナに移動する
     */
    public FragmentTransactionBuilder replace(int container, Fragment fragment) {
        return replace(container, fragment, null);
    }

    public FragmentTransactionBuilder replace(int container, Fragment fragment, long tag) {
        return replace(container, fragment, String.valueOf(tag));
    }

    public FragmentTransactionBuilder replace(int container, Fragment fragment, String tag) {
        if (StringUtil.isEmpty(tag)) {
            transaction.replace(container, fragment);
        } else {
            transaction.replace(container, fragment, tag);
        }

        fragments.add(fragment);
        return this;
    }

    public FragmentTransactionBuilder addToBackStack() {
        transaction.addToBackStack(null);
        return this;
    }

    /**
     * 内容のコミットを行う
     */
    public void commit() {
        int backStackId = transaction.commit();
        for (Fragment frag : fragments) {
            if (frag instanceof SupportFragmentDelegate.SupportFragmentCompat) {
                ((SupportFragment) frag).setBackStackIndex(backStackId);
            }
        }
    }
}
