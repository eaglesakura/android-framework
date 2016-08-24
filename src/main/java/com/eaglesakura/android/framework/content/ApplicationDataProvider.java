package com.eaglesakura.android.framework.content;

import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * アプリ内で一意に保ちたいデータ（設定等）を
 */
public abstract class ApplicationDataProvider extends ContentProvider {

    List<UriHandler> mHandlers = new ArrayList<>();

    /**
     * ハンドラを追加する
     */
    public void addHandler(UriHandler handler) {
        mHandlers.add(handler);
    }

    @Override
    public void shutdown() {
        for (UriHandler handler : mHandlers) {
            handler.onShutdown();
        }
        mHandlers.clear();
        super.shutdown();
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        for (UriHandler handler : mHandlers) {
            if (handler.isSupport(uri)) {
                byte[] buffer = handler.query(uri, selection, selectionArgs);
                if (buffer != null) {
                    return new ByteArrayCursor(buffer);
                }
            }
        }
        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return "application/serialized-buffer";
    }

    public static final String CONTENT_KEY_COMMAND = "sys.COMMAND";

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        for (UriHandler handler : mHandlers) {
            if (handler.isSupport(uri)) {
                handler.insert(uri, contentValues.getAsString(CONTENT_KEY_COMMAND), contentValues);
                return uri;
            }
        }
        return uri;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }

    /**
     * 対応したContentProviderと接続し、byte配列を得る
     *
     * @param context  context
     * @param uri      target URI
     * @param command  実行コマンド
     * @param argments 実行引数
     */
    @SuppressLint("NewApi")
    @Nullable
    public static byte[] query(@NonNull Context context, @NonNull Uri uri, @NonNull String command, @Nullable String[] argments) {
        try (Cursor cursor = context.getContentResolver().query(uri, null, command, argments, null)) {
            if (cursor != null) {
                return ByteArrayCursor.toByteArray(cursor);
            } else {
                return null;
            }
        }
    }

    /**
     * 対応したContentProviderに命令を送る
     *
     * @param uri     target URI
     * @param command 実行コマンド
     * @param values  実行引数
     */
    public static void insert(@NonNull Context context, @NonNull Uri uri, @NonNull String command, @Nullable ContentValues values) {
        if (values == null) {
            values = new ContentValues();
        }
        values.put(CONTENT_KEY_COMMAND, command);
        context.getContentResolver().insert(uri, values);
    }
}
