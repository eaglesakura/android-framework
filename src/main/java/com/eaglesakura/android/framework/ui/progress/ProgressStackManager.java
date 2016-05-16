package com.eaglesakura.android.framework.ui.progress;

import com.eaglesakura.android.rx.ObserveTarget;
import com.eaglesakura.android.rx.SubscriptionController;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProgressStackManager {
    List<ProgressToken> mTokens = new ArrayList<>();

    @NonNull
    final SubscriptionController mSubscriptionController;

    @NonNull
    Listener mListener;

    public ProgressStackManager(@NonNull SubscriptionController subscriptionController) {
        mSubscriptionController = subscriptionController;
    }

    public void setListener(@NonNull Listener listener) {
        mListener = listener;
    }

    private void sort() {
        synchronized (mTokens) {
            Collections.sort(mTokens, (a, b) -> Float.compare(a.getPriority(), b.getPriority()));
        }
    }

    /**
     * 優先すべきトークンを取得する
     */
    @Nullable
    public ProgressToken getTopToken() {
        synchronized (mTokens) {
            if (mTokens.isEmpty()) {
                return null;
            } else {
                return mTokens.get(0);
            }
        }
    }

    public void push(@NonNull ProgressToken token) {
        synchronized (mTokens) {
            ProgressToken topToken = getTopToken();
            mTokens.add(token);
            sort();
            if (topToken == null) {
                // 処理が開始された
                mSubscriptionController.run(ObserveTarget.Foreground, () -> {
                    mListener.onProgressStarted(this, token);
                });
            } else {
                // 切り替わった？
                ProgressToken newTopToken = getTopToken();
                if (topToken != newTopToken) {
                    // 新しいトークンを発信する
                    mSubscriptionController.run(ObserveTarget.Foreground, () -> {
                        mListener.onProgressTopChanged(this, newTopToken);
                    });
                }
            }
        }
    }

    void pop(@NonNull ProgressToken token) {
        synchronized (mTokens) {
            ProgressToken topToken = getTopToken();

            mTokens.remove(token);
            sort();

            if (mTokens.isEmpty()) {
                // 処理が終了した
                mSubscriptionController.run(ObserveTarget.Foreground, () -> {
                    mListener.onProgressFinished(this);
                });
            } else {
                // トークンが切り替わった
                ProgressToken newTopToken = getTopToken();
                if (newTopToken != topToken) {
                    mSubscriptionController.run(ObserveTarget.Foreground, () -> {
                        mListener.onProgressTopChanged(this, newTopToken);
                    });
                }
            }
        }
    }

    /**
     * トークンが
     */
    public boolean hasTokens() {
        synchronized (mTokens) {
            return !mTokens.isEmpty();
        }
    }

    public interface Listener {

        /**
         * 処理が開始された
         */
        @UiThread
        void onProgressStarted(@NonNull ProgressStackManager self, @NonNull ProgressToken token);

        /**
         * 優先度のトップが切り替わった
         */
        @UiThread
        void onProgressTopChanged(@NonNull ProgressStackManager self, @NonNull ProgressToken topToken);

        /**
         * 全ての処理が完了した
         */
        @UiThread
        void onProgressFinished(@NonNull ProgressStackManager self);
    }

}
