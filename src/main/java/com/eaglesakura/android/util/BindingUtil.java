package com.eaglesakura.android.util;

import com.eaglesakura.android.framework.R;

import android.databinding.ViewDataBinding;
import android.view.View;

public class BindingUtil {
    public static <T extends ViewDataBinding> View bind(T binding) {
        // 自身をView自体に参照させる
        binding.getRoot().setTag(R.id.CardAdapter_DataBinding, binding);
        return binding.getRoot();
    }

    public static <T extends ViewDataBinding> T from(Class<T> clazz, View view) {
        return (T) view.getTag(R.id.CardAdapter_DataBinding);
    }
}
