package com.eaglesakura.material.widget.support;

import com.eaglesakura.android.framework.UnitTestCase;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.util.Util;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class SupportCancelCallbackBuilderTest extends UnitTestCase {

    @Test
    public void 初期のキャンセルチェックが行える() throws Throwable {
        validate(SupportCancelCallbackBuilder.from(() -> false).build())
                .check(obj -> assertFalse(obj.isCanceled()));

        validate(SupportCancelCallbackBuilder.from((CancelCallback) null).build())
                .check(obj -> assertFalse(obj.isCanceled()));
    }

    @Test
    public void OR条件を満たせる() throws Throwable {
        // false || trueならキャンセル可能
        validate(SupportCancelCallbackBuilder.from(() -> false).or(() -> true).build())
                .check(obj -> assertTrue(obj.isCanceled()));
        validate(SupportCancelCallbackBuilder.from(() -> true).or(() -> false).build())
                .check(obj -> assertTrue(obj.isCanceled()));

        // false || falseならキャンセルではない
        validate(SupportCancelCallbackBuilder.from(() -> false).or(() -> false).build())
                .check(obj -> assertFalse(obj.isCanceled()));

        // false || false || trueならキャンセル
        validate(SupportCancelCallbackBuilder.from(() -> false).or(() -> false).or(() -> true).build())
                .check(obj -> assertTrue(obj.isCanceled()));
    }

    @Test
    public void AND条件を満たせる() throws Throwable {
        // false && trueではNG
        validate(SupportCancelCallbackBuilder.from(() -> false).and(() -> true).build())
                .check(obj -> assertFalse(obj.isCanceled()));
        validate(SupportCancelCallbackBuilder.from(() -> true).and(() -> false).build())
                .check(obj -> assertFalse(obj.isCanceled()));

        // true && trueならキャンセル
        validate(SupportCancelCallbackBuilder.from(() -> true).and(() -> true).build())
                .check(obj -> assertTrue(obj.isCanceled()));

        // true && true && trueならキャンセル
        validate(SupportCancelCallbackBuilder.from(() -> true).and(() -> true).and(() -> true).build())
                .check(obj -> assertTrue(obj.isCanceled()));
    }

    @Test
    public void OR_ANDの組み合わせを満たせる() throws Throwable {
        // (true && false) || trueであるため、trueが正しい
        validate(SupportCancelCallbackBuilder.from(() -> true).and(() -> false).or(() -> true).build())
                .check(obj -> assertTrue(obj.isCanceled()));

        // (false || true) && false であるため、falseが正しい
        validate(SupportCancelCallbackBuilder.from(() -> false).or(() -> true).and(() -> false).build())
                .check(obj -> assertFalse(obj.isCanceled()));
    }

    @Test
    public void タイムアウトチェックを行える() throws Throwable {
        SupportCancelCallbackBuilder.CancelChecker cancel = SupportCancelCallbackBuilder.from(() -> false).orTimeout(500, TimeUnit.MILLISECONDS).build();
        assertFalse(cancel.isCanceled());   // タイムアウト時間を満たしていない
        Util.sleep(1000);
        assertTrue(cancel.isCanceled());    // タイムアウト時間を満たした
    }
}