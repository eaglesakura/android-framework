package com.eaglesakura.android.framework.provider;

import com.eaglesakura.android.framework.FrameworkCentral;
import com.eaglesakura.android.garnet.Depend;
import com.eaglesakura.android.garnet.Provide;
import com.eaglesakura.android.garnet.Provider;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Context依存を解決しやすくしたProvider
 */
public class ContextProvider implements Provider {
    private Context mContext;

    @Depend
    public void setContext(Context context) {
        mContext = context;
    }

    @Depend
    public void setApplication(Application application) {
        mContext = application;
    }

    @Override
    public void onDependsCompleted(Object inject) {

    }

    @Override
    public void onInjectCompleted(Object inject) {

    }

    /**
     * 依存解決用のContextを取得する。
     *
     * Contextがdependされている場合はそちらを優先し、それ以外ではFrameworkの持つContextを返却する
     */
    @NonNull
    protected Context getContext() {
        if (mContext != null) {
            return mContext;
        } else {
            return FrameworkCentral.getApplication();
        }
    }

    @NonNull
    protected Application getApplication() {
        return (Application) getContext().getApplicationContext();
    }

    @Provide
    public Context provideContext() {
        return getContext();
    }

    @Provide
    public Application provideApplication() {
        return (Application) getContext().getApplicationContext();
    }

}
