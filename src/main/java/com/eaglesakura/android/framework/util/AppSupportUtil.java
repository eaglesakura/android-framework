package com.eaglesakura.android.framework.util;

import com.eaglesakura.android.framework.ui.message.LocalMessageReceiver;
import com.eaglesakura.android.rx.RxTask;
import com.eaglesakura.android.util.PermissionUtil;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.util.CollectionUtil;
import com.eaglesakura.util.Timer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * アプリ開発でよく用いるUtil
 */
public class AppSupportUtil {

    @TargetApi(Build.VERSION_CODES.M)
    public static boolean requestRuntimePermissions(Activity activity, String[] permissions) {
        if (!PermissionUtil.isRuntimePermissionGranted(activity, permissions)) {
            activity.requestPermissions(permissions, LocalMessageReceiver.REQUEST_RUNTIMEPERMISSION_UPDATE);
            return true;
        } else {
            return false;
        }
    }

    public static void onRequestPermissionsResult(Activity activity, int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == LocalMessageReceiver.REQUEST_RUNTIMEPERMISSION_UPDATE) {
            Intent intent = new Intent();
            intent.setAction(LocalMessageReceiver.ACTION_RUNTIMEPERMISSION_UPDATE);
            List<String> granted = new ArrayList<>();
            List<String> denied = new ArrayList<>();

            for (int i = 0; i < permissions.length; ++i) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    granted.add(permissions[i]);
                } else {
                    denied.add(permissions[i]);
                }
            }

            intent.putExtra(LocalMessageReceiver.EXTRA_RUNTIMEPERMISSION_GRANTED_LIST, CollectionUtil.asArray(granted, new String[granted.size()]));
            intent.putExtra(LocalMessageReceiver.EXTRA_RUNTIMEPERMISSION_DENIED_LIST, CollectionUtil.asArray(denied, new String[denied.size()]));

            LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
        }
    }

    public static CancelCallback asCancelCallback(RxTask task) {
        return () -> task.isCanceled();
    }

    /**
     * タイムアウト時間を指定してキャンセルコールバックを生成する
     */
    public static CancelCallback asCancelCallback(RxTask task, long time, TimeUnit unit) {
        final long timeout = unit.toMillis(time);
        return new CancelCallback() {
            Timer mTimer = new Timer();

            @Override
            public boolean isCanceled() throws Throwable {
                return task.isCanceled() || (mTimer.end() > timeout);
            }
        };
    }

    /**
     * タイムアウト時間を指定してキャンセルコールバックを生成する
     */
    public static CancelCallback asCancelCallback(long time, TimeUnit unit) {
        final long timeout = unit.toMillis(time);
        return new CancelCallback() {
            Timer mTimer = new Timer();

            @Override
            public boolean isCanceled() throws Throwable {
                return (mTimer.end() > timeout);
            }
        };
    }

    public static <T extends View> T findViewByChildAt(View root, int... indices) {
        for (int index : indices) {
            root = ((ViewGroup) root).getChildAt(index);
        }

        return (T) root;
    }
}
