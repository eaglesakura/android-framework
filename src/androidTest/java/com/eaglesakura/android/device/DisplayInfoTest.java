package com.eaglesakura.android.device;

import com.eaglesakura.android.device.display.DisplayInfo;
import com.eaglesakura.android.devicetest.DeviceTestCase;
import com.eaglesakura.android.framework.FwLog;
import com.eaglesakura.util.LogUtil;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class DisplayInfoTest extends DeviceTestCase {

    @Test
    public void DP設定が取得できることを確認する() throws Throwable {
        DisplayInfo info = new DisplayInfo(getContext());
        assertNotNull(info.getDeviceType());

        FwLog.debug("Pixel[%d x %d]", info.getWidthPixel(), info.getHeightPixel());
        FwLog.debug("DP [%.1f x %.1f]", info.getWidthDp(), info.getHeightDp());
        FwLog.debug("Inch [%.1f x %.1f] = %.3f inch -> %s inch",
                info.getWidthInch(),
                info.getHeightInch(),
                info.getDiagonalInch(),
                info.getDiagonalInchRound().toString()
        );
        FwLog.debug("Type [%s]", info.getDeviceType().name());
    }
}
