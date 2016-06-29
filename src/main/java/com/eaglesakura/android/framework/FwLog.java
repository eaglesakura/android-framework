package com.eaglesakura.android.framework;

import com.eaglesakura.util.EnvironmentUtil;
import com.eaglesakura.util.LogUtil;

import android.util.Log;

public class FwLog {
    private static final LogUtil.Logger sAppLogger;

    static {
        if (EnvironmentUtil.isRunningRobolectric()) {
            sAppLogger = ((level, tag, msg) -> {
                switch (level) {
                    case LogUtil.LOGGER_LEVEL_INFO:
                        tag = "I/" + tag;
                        break;
                    case LogUtil.LOGGER_LEVEL_ERROR:
                        tag = "E/" + tag;
                        break;
                    default:
                        tag = "D/" + tag;
                        break;
                }

                try {
                    StackTraceElement[] trace = new Exception().getStackTrace();
                    StackTraceElement elem = trace[Math.min(trace.length - 1, 3)];
                    System.out.println(String.format("%s | %s[%d] : %s", tag, elem.getFileName(), elem.getLineNumber(), msg));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } else {
            sAppLogger = new LogUtil.AndroidLogger(Log.class) {
                @Override
                protected int getStackDepth() {
                    return 4;
                }
            }.setStackInfo(BuildConfig.DEBUG);
        }
    }

    public static void widget(String fmt, Object... args) {
        String tag = "Fw.Widget";

        LogUtil.setLogger(tag, sAppLogger);
        LogUtil.out(tag, fmt, args);
    }

    public static void system(String fmt, Object... args) {
        String tag = "Fw.System";

        LogUtil.setLogger(tag, sAppLogger);
        LogUtil.out(tag, fmt, args);
    }

    public static void debug(String fmt, Object... args) {
        String tag = "Fw.Debug";

        LogUtil.setLogger(tag, sAppLogger);
        LogUtil.out(tag, fmt, args);
    }

    public static void google(String fmt, Object... args) {
        String tag = "Fw.Google";

        LogUtil.setLogger(tag, sAppLogger);
        LogUtil.out(tag, fmt, args);
    }
}
