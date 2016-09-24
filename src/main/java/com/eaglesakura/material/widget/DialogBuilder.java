package com.eaglesakura.material.widget;

import com.eaglesakura.android.framework.R;
import com.eaglesakura.android.framework.delegate.lifecycle.UiLifecycleDelegate;
import com.eaglesakura.android.util.ContextUtil;
import com.eaglesakura.lambda.Action0;
import com.eaglesakura.lambda.Action2;
import com.eaglesakura.lambda.ResultAction1;
import com.eaglesakura.util.CollectionUtil;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.view.View;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * アプリ用の共通ダイアログを生成する
 */
public class DialogBuilder<T> {
    protected AlertDialog.Builder mBuilder;

    /**
     * 選択対象のアイテム
     */
    protected List<?> mSelections;

    /**
     * セレクタアクション
     */
    protected Action2<Integer, T> mSelector;

    protected Integer mLayoutWidth;

    protected Integer mLayoutHeight;

    public DialogBuilder(AlertDialog.Builder builder) {
        mBuilder = builder;
    }

    public DialogBuilder title(@StringRes int titleId) {
        mBuilder.setTitle(titleId);
        return this;
    }

    public DialogBuilder layout(int width, int height) {
        mLayoutWidth = width;
        mLayoutHeight = height;
        return this;
    }

    /**
     * ダイアログの表示を開始する
     */
    public Dialog show(@Nullable UiLifecycleDelegate delegate) {
        AlertDialog dialog = mBuilder.show();

        if (delegate != null) {
            delegate.addAutoDismiss(dialog);
        }

        if (mBuilder.getContext() instanceof Activity) {
            ContextUtil.closeIME((Activity) mBuilder.getContext());
        }

        if (CollectionUtil.allNotNull(mLayoutWidth, mLayoutHeight)) {
            dialog.getWindow().setLayout(mLayoutWidth, mLayoutHeight);
        }

        return dialog;
    }

    /**
     * ダイアログの表示を開始する
     */
    public Dialog show(@Nullable UiLifecycleDelegate delegate, Object tag) {
        AlertDialog dialog = mBuilder.show();

        if (delegate != null) {
            delegate.addAutoDismiss(dialog, tag);
        }
        return dialog;
    }

    /**
     * インターフェースをラップする
     */
    protected DialogInterface.OnClickListener wrap(@Nullable Action0 action) {
        if (action == null) {
            return null;
        }
        return (dlg, which) -> {
            try {
                action.action();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * 表示用のViewを指定する
     */
    public DialogBuilder view(@LayoutRes int resId) {
        mBuilder.setView(resId);
        return this;
    }

    /**
     * 表示用のViewを指定する
     */
    public DialogBuilder view(View contentView) {
        mBuilder.setView(contentView);
        return this;
    }

    /**
     * 選択時のアクションを設定する
     */
    public DialogBuilder selected(Action2<Integer, T> action) {
        mSelector = action;
        return this;
    }

    public DialogBuilder positiveButton(@StringRes int textId, Action0 action) {
        mBuilder.setPositiveButton(textId, wrap(action));
        return this;
    }

    public DialogBuilder negativeButton(@StringRes int textId, Action0 action) {
        mBuilder.setNegativeButton(textId, wrap(action));
        return this;
    }

    public DialogBuilder neutralButton(@StringRes int textId, Action0 action) {
        mBuilder.setNeutralButton(textId, wrap(action));
        return this;
    }

    public DialogBuilder cancelable(boolean cancel) {
        mBuilder.setCancelable(cancel);
        return this;
    }

    /**
     * 独自のコンテンツを用いてBuilderを生成する
     */
    public static DialogBuilder newCustomContent(Context context, String title, View contentView) {
        DialogBuilder builder = new DialogBuilder(new AlertDialog.Builder(context));
        builder.mBuilder.setTitle(title);
        return builder.view(contentView);
    }

    public static DialogBuilder newInformation(Context context, String message) {
        DialogBuilder builder = new DialogBuilder(new AlertDialog.Builder(context));
        builder.mBuilder.setTitle(R.string.EsMaterial_Title_Common_Information);
        builder.mBuilder.setMessage(message);
        return builder;
    }

    public static DialogBuilder newInformation(Context context, @StringRes int messageId) {
        DialogBuilder builder = new DialogBuilder(new AlertDialog.Builder(context));
        builder.mBuilder.setTitle(R.string.EsMaterial_Title_Common_Information);
        builder.mBuilder.setMessage(messageId);
        return builder;
    }

    public static DialogBuilder newAlert(Context context, @StringRes int messageId) {
        DialogBuilder builder = new DialogBuilder(new AlertDialog.Builder(context));
        builder.mBuilder.setTitle(R.string.EsMaterial_Title_Common_Alert);
        builder.mBuilder.setMessage(messageId);
        return builder;
    }

    public static DialogBuilder newAlert(Context context, @NonNull String message) {
        DialogBuilder builder = new DialogBuilder(new AlertDialog.Builder(context));
        builder.mBuilder.setTitle(R.string.EsMaterial_Title_Common_Alert);
        builder.mBuilder.setMessage(message);
        return builder;
    }

    /**
     * 選択ダイアログを取得
     */
    public static <T> DialogBuilder<T> newSelections(Context context, Collection<T> objects, ResultAction1<T, String> selectorTextConverter) {
        DialogBuilder builder = new DialogBuilder(new AlertDialog.Builder(context));
        List<String> selections;
        try {
            selections = CollectionUtil.asOtherList(objects, selectorTextConverter);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        builder.mSelections = new ArrayList<>(objects);
        builder.mBuilder.setItems(CollectionUtil.asArray(selections, new String[selections.size()]), (dlg, which) -> {
            if (builder.mSelector != null) {
                try {
                    builder.mSelector.action(which, builder.mSelections.get(which));
                } catch (Throwable e) {
                    throw new IllegalStateException(e);
                }
            }
        });
        return builder;
    }

}
