package com.eaglesakura.android.framework.ui.support.annotation;

import android.support.annotation.Keep;
import android.support.annotation.MenuRes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * FragmentのAnnotationとして設定することで、MenuResを指定する
 * valueを優先し、valueが0の場合はresNameを取得する。
 */
@Keep
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface FragmentMenu {
    /**
     * R.menu.**
     */
    @MenuRes int value() default 0;

    /**
     * R.id."resName"
     */
    String resName() default "";
}
