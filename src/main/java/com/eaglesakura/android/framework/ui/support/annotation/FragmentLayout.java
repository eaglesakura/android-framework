package com.eaglesakura.android.framework.ui.support.annotation;

import android.support.annotation.LayoutRes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * FragmentのAnnotationとして設定することで、LayoutIdを指定する
 * valueを優先し、valueが0の場合はresNameを取得する。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface FragmentLayout {
    /**
     * R.layout.**
     */
    @LayoutRes int value() default 0;

    /**
     * R.id."resName"
     */
    String resName() default "";
}
