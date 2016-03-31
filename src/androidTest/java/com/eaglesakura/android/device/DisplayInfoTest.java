package com.eaglesakura.android.device;

import com.eaglesakura.android.device.display.DisplayInfo;
import com.eaglesakura.android.devicetest.ModuleTestCase;
import com.eaglesakura.util.LogUtil;

public class DisplayInfoTest extends ModuleTestCase {

    public void test_DP設定が取得できることを確認する() throws Throwable {
        DisplayInfo info = new DisplayInfo(getContext());
        assertNotNull(info.getDeviceType());

        LogUtil.out(LOG_TAG, "Pixel[%d x %d]", info.getWidthPixel(), info.getHeightPixel());
        LogUtil.out(LOG_TAG, "DP [%.1f x %.1f]", info.getWidthDp(), info.getHeightDp());
        LogUtil.out(LOG_TAG, "Inch [%.1f x %.1f] = %.3f inch -> %s inch",
                info.getWidthInch(),
                info.getHeightInch(),
                info.getDiagonalInch(),
                info.getDiagonalInchRound().toString()
        );
        LogUtil.out(LOG_TAG, "Type [%s]", info.getDeviceType().name());
    }
}
