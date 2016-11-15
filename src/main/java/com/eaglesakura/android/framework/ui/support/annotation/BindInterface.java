package com.eaglesakura.android.framework.ui.support.annotation;

import android.support.annotation.Keep;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Fragmentからアクセス可能なFragmentManagerツリーの中にインターフェースを見つける
 */
@Keep
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface BindInterface {
    /**
     * 親インターフェースのみを探す
     */
    boolean parentOnly() default false;

    /**
     * 子供のみを探す
     */
    boolean childrenOnly() default false;

    /**
     * 見つからないことを許容する
     */
    boolean nullable() default false;
}
