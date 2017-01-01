package com.eaglesakura.android.framework.ui.progress;

import com.eaglesakura.util.RandomUtil;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import java.io.Closeable;
import java.io.IOException;

/**
 * 複数処理を並列で行い、かつProgress表記を行う場合に参照カウンタを利用して処理を表記する。
 * 参照カウンタが0になった時点でロックを解除する。
 */
public abstract class ProgressToken implements Closeable {
    /**
     * 処理タグ
     */
    @NonNull
    final String mTag;

    /**
     * 優先度
     */
    float mPriority;

    ProgressStackManager mStackManager;

    public ProgressToken(ProgressStackManager stackManager) {
        this(stackManager, RandomUtil.randShortString());
    }

    public ProgressToken(ProgressStackManager stackManager, final String tag) {
        mTag = tag;
        mStackManager = stackManager;
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

    @Override
    public void close() throws IOException {
        if (mStackManager == null) {
            throw new IOException();
        }

        mStackManager.pop(this);
        mStackManager = null;
    }

    /**
     * メッセージからトークンを生成する
     */
    public static ProgressToken fromMessage(ProgressStackManager stackManager, Context context, @StringRes int stringRes) {
        return fromMessage(stackManager, context.getString(stringRes));
    }

    /**
     * メッセージからトークンを生成する
     */
    public static ProgressToken fromMessage(ProgressStackManager stackManager, String message) {
        return new ProgressToken(stackManager) {
            @NonNull
            @Override
            public String getMessage() {
                return message;
            }
        };
    }
}
