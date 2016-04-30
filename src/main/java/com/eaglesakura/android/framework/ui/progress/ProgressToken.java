package com.eaglesakura.android.framework.ui.progress;

import com.eaglesakura.util.RandomUtil;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

public abstract class ProgressToken {
    /**
     * 処理タグ
     */
    @NonNull
    final String mTag;

    /**
     * 優先度
     */
    float mPriority;

    public ProgressToken() {
        this(RandomUtil.randShortString());
    }

    public ProgressToken(final String tag) {
        mTag = tag;
    }

    public ProgressToken priority(float newPriority) {
        mPriority = newPriority;
        return this;
    }

    public float getPriority() {
        return mPriority;
    }

    @NonNull
    public String getTag() {
        return mTag;
    }

    @NonNull
    public abstract String getMessage();

    /**
     * メッセージからトークンを生成する
     */
    public static ProgressToken fromMessage(Context context, @StringRes int stringRes) {
        return fromMessage(context.getString(stringRes));
    }

    /**
     * メッセージからトークンを生成する
     */
    public static ProgressToken fromMessage(String message) {
        return new ProgressToken() {
            @NonNull
            @Override
            public String getMessage() {
                return message;
            }
        };
    }
}
