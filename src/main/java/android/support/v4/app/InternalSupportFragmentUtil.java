package android.support.v4.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class InternalSupportFragmentUtil {
    public static void startActivityForResult(@NonNull Fragment fragment, int requestCode, @NonNull Intent intent, @Nullable Bundle options) {
        fragment.mHost.onStartActivityFromFragment(fragment, intent, requestCode, options);
    }

    /**
     * 25.xのFragmentでChildFragmentManagerのNoHostエラーに対して措置を行なう。
     * これは一時的な処理であるため、将来的に内部処理は削除されるのが望ましい。
     */
    public static void onCreate(@NonNull Fragment fragment, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            fragment.mChildFragmentManager.mHost = fragment.mHost;
        }
    }

    /**
     * 25.xのFragmentでChildFragmentManagerのNoHostエラーに対して措置を行なう。
     * これは一時的な処理であるため、将来的に内部処理は削除されるのが望ましい。
     */
    public static void onDetach(@NonNull Fragment fragment) {
        fragment.mChildFragmentManager = null;
    }
}
