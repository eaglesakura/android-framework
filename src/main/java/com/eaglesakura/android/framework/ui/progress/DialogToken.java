package com.eaglesakura.android.framework.ui.progress;

import com.eaglesakura.lambda.Action1Throwable;
import com.eaglesakura.lambda.CancelCallback;

import android.view.View;

import java.io.Closeable;

/**
 * ダイアログ表記とキャンセルチェックを同期するトークン
 * try-with-resourceかつ非同期処理で利用する。
 */
public interface DialogToken extends Closeable, CancelCallback {
    /**
     * コンテンツを更新する
     *
     * UIスレッドでアクションをコールバックする。
     * 読み出しが完了するまでロックする
     */
    void updateContent(Action1Throwable<View, Throwable> action);
}
