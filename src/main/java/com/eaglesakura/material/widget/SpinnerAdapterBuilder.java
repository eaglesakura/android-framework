package com.eaglesakura.material.widget;

import com.eaglesakura.lambda.Action1;
import com.eaglesakura.lambda.Action2;
import com.eaglesakura.lambda.Matcher1;
import com.eaglesakura.lambda.ResultAction1;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.List;

/**
 * シンプルなスピナーを組み立てる
 */
public class SpinnerAdapterBuilder<T> {

    Spinner mSpinner;

    List<? extends T> mItems;

    ResultAction1<T, String> mTitleConverter;

    Action2<T, View> mDropdownViewConverter;

    Action2<T, View> mSelectionViewConvert;

    Action1<T> mSelectedAction;

    int mSelected;

    public SpinnerAdapterBuilder(Spinner spinner) {
        mSpinner = spinner;
    }

    public SpinnerAdapterBuilder<T> items(List<? extends T> items, ResultAction1<T, String> titleConverter) {
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
        mDropdownViewConverter = dropdownViewConvert;
        return this;
    }

    public SpinnerAdapterBuilder<T> selectionView(Action2<T, View> selectionViewConvert) {
        mSelectionViewConvert = selectionViewConvert;
        return this;
    }

    public SpinnerAdapterBuilder<T> selected(Action1<T> action) {
        mSelectedAction = action;
        return this;
    }

    public SpinnerAdapterBuilder<T> build() {
        ArrayAdapter<String> adapter = new ArrayAdapter(mSpinner.getContext(), android.R.layout.simple_spinner_item) {

            ArrayAdapter init() {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                return this;
            }

            // DropDown Viewに変形を加える
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View result = super.getDropDownView(position, convertView, parent);
                if (mDropdownViewConverter != null) {
                    try {
                        mDropdownViewConverter.action(mItems.get(position), result);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
                return result;
            }


            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View result = super.getView(position, convertView, parent);

                if (mSelectionViewConvert != null) {
                    try {
                        mSelectionViewConvert.action(mItems.get(position), result);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
                return result;
            }
        }.init();

        try {
            for (T item : mItems) {
                adapter.add(mTitleConverter.action(item));
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        mSpinner.setAdapter(adapter);
        mSpinner.setSelection(mSelected);

        if (mSelectedAction != null) {
            mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                    try {
                        mSelectedAction.action((T) mItems.get(position));
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }
        return this;
    }

    public static <T> SpinnerAdapterBuilder<T> from(Spinner spinner, Class<T> clazz) {
        SpinnerAdapterBuilder<T> builder = new SpinnerAdapterBuilder(spinner);
        return builder;
    }
}
