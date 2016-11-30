package com.eaglesakura.material.widget;

import com.eaglesakura.lambda.Action1;
import com.eaglesakura.lambda.Action2;
import com.eaglesakura.lambda.Action3;
import com.eaglesakura.lambda.Matcher1;
import com.eaglesakura.lambda.ResultAction1;
import com.eaglesakura.lambda.ResultAction2;
import com.eaglesakura.util.CollectionUtil;

import android.content.Context;
import android.support.annotation.ArrayRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import java.util.List;

/**
 * シンプルなスピナーを組み立てる
 */
public class SpinnerAdapterBuilder<T> {

    Spinner mSpinner;

    List<? extends T> mItems;

    ResultAction2<Integer, T, String> mTitleConverter;

    Action3<Integer, T, View> mDropdownViewConverter;

    Action3<Integer, T, View> mSelectionViewConvert;

    Action2<Integer, T> mSelectedAction = (index, item) -> {

    };

    int mSelected;

    Context mContext;

    public SpinnerAdapterBuilder(Context context, Spinner spinner) {
        if (context == null) {
            throw new NullPointerException("Context == null");
        }
        if (spinner == null) {
            throw new NullPointerException("Spinner == null");
        }
        mContext = context;
        mSpinner = spinner;
    }

    public SpinnerAdapterBuilder<T> title(ResultAction2<Integer, T, String> titleConverter) {
        mTitleConverter = titleConverter;
        return this;
    }

    public SpinnerAdapterBuilder<T> items(List<? extends T> items) {
        mItems = items;
        return this;
    }

    public SpinnerAdapterBuilder<T> items(List<? extends T> items, ResultAction1<T, String> titleConverter) {
        return items(items, (index, item) -> titleConverter.action(item));
    }

    public SpinnerAdapterBuilder<T> items(List<? extends T> items, ResultAction2<Integer, T, String> titleConverter) {
        mItems = items;
        mTitleConverter = titleConverter;
        return this;
    }

    public SpinnerAdapterBuilder<T> selection(Matcher1<T> matcher) {
        mSelected = 0;
        try {
            for (T item : mItems) {
                if (matcher.match(item)) {
                    return this;
                } else {
                    ++mSelected;
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        // マッチしなかった
        mSelected = 0;
        return this;
    }

    public SpinnerAdapterBuilder<T> selection(T obj) {
        mSelected = Math.max(mItems.indexOf(obj), 0);
        return this;
    }

    public SpinnerAdapterBuilder<T> dropdownView(Action2<T, View> dropdownViewConvert) {
        return dropdownView((index, item, view) -> dropdownViewConvert.action(item, view));
    }

    public SpinnerAdapterBuilder<T> dropdownView(Action3<Integer, T, View> dropdownViewConvert) {
        mDropdownViewConverter = dropdownViewConvert;
        return this;
    }

    public SpinnerAdapterBuilder<T> selectionView(Action2<T, View> selectionViewConvert) {
        return selectionView((index, item, view) -> selectionViewConvert.action(item, view));
    }

    public SpinnerAdapterBuilder<T> selectionView(Action3<Integer, T, View> selectionViewConvert) {
        mSelectionViewConvert = selectionViewConvert;
        return this;
    }

    public SpinnerAdapterBuilder<T> selected(Action1<T> action) {
        return selected((index, item) -> action.action(item));
    }


    public SpinnerAdapterBuilder<T> selected(Action2<Integer, T> action) {
        mSelectedAction = action;
        return this;
    }

    /**
     * Adapterのみを生成する
     */
    public SupportArrayAdapter<T> buildAdapter() {
        SupportArrayAdapter<T> adapter = new SupportArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, android.R.layout.simple_spinner_dropdown_item);
        if (mDropdownViewConverter != null) {
            adapter.setDropdownViewConverter(mDropdownViewConverter);
        }
        if (mSelectionViewConvert != null) {
            adapter.setSelectionViewConvert(mSelectionViewConvert);
        }
        if (mTitleConverter != null) {
            adapter.setTitleConverter(mTitleConverter);
        }

        if (!CollectionUtil.isEmpty(mItems)) {
            for (T item : mItems) {
                adapter.add(item);
            }
        }
        return adapter;
    }

    public SpinnerAdapterBuilder<T> build() {
        mSpinner.setAdapter(buildAdapter());
        mSpinner.setSelection(mSelected);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                try {
                    mSelectedAction.action(position, (T) mSpinner.getAdapter().getItem(position));
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        return this;
    }

    public static <T> SpinnerAdapterBuilder<T> from(@NonNull Spinner spinner, Class<T> clazz) {
        return from(spinner.getContext(), spinner, clazz);
    }

    public static <T> SpinnerAdapterBuilder<T> from(@NonNull Context context, @NonNull Spinner spinner, Class<T> clazz) {
        SpinnerAdapterBuilder<T> builder = new SpinnerAdapterBuilder(context, spinner);
        return builder;
    }


    public static SpinnerAdapterBuilder<String> fromStringArray(@NonNull Spinner spinner, @NonNull Context context, @ArrayRes int resId) {
        SpinnerAdapterBuilder<String> builder = new SpinnerAdapterBuilder(context, spinner);
        builder.items(CollectionUtil.asList(context.getResources().getStringArray(resId)), it -> it);
        return builder;
    }
}
