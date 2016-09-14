package com.eaglesakura.android.framework.util;

import com.eaglesakura.lambda.Matcher1;
import com.eaglesakura.util.CollectionUtil;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class FragmentUtil {

    /**
     * 所属する全てのFragmentを列挙する
     */
    public static List<Fragment> listFragments(AppCompatActivity activity) {
        return listFragments(activity.getSupportFragmentManager(), fragment -> true);
    }

    /**
     * 指定した条件に合致するFragmentを全て列挙する。
     *
     * このメソッドはChildFragmentを再帰的に検索する
     */
    @NonNull
    public static List<Fragment> listFragments(FragmentManager fragmentManager, Matcher1<Fragment> matcher) {
        List<Fragment> result = new ArrayList<>();

        try {
            List<Fragment> fragments = fragmentManager.getFragments();
            if (!CollectionUtil.isEmpty(fragments)) {
                for (Fragment frag : fragments) {
                    if (frag != null && matcher.match(frag)) {
                        result.add(frag);
                    }

                    // 子Fragmentを足し込む
                    if (frag != null) {
                        result.addAll(listFragments(frag.getChildFragmentManager(), matcher));
                    }
                }
            }
            return result;
        } catch (RuntimeException e) {
            throw e;
        } catch (Error e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
