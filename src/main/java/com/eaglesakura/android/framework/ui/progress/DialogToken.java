package com.eaglesakura.android.framework.ui.progress;

import com.eaglesakura.lambda.CancelCallback;

import java.io.Closeable;

/**
 * ダイアログ表記とキャンセルチェックを同期するトークン
 * try-with-resourceかつ非同期処理で利用する。
 */
public interface DialogToken extends Closeable, CancelCallback {
}
