package com.eaglesakura.android.playservice;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

import com.eaglesakura.android.framework.FrameworkCentral;
import com.eaglesakura.android.framework.FwLog;
import com.eaglesakura.android.framework.R;
import com.eaglesakura.android.framework.db.BasicSettings;
import com.eaglesakura.android.framework.ui.BaseActivity;
import com.eaglesakura.android.oari.OnActivityResult;
import com.eaglesakura.android.rx.LifecycleState;
import com.eaglesakura.android.util.ContextUtil;
import com.eaglesakura.material.widget.MaterialAlertDialog;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.Util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;


/**
 * Googleの認証を専門に行うActivity
 */
public abstract class GoogleAuthActivity extends BaseActivity implements GoogleApiFragment.Callback {

    static final int REQUEST_GOOGLE_CLIENT_AUTH = 0x1200;

    public static final String EXTRA_AUTH_ERROR_CODE = "EXTRA_AUTH_ERROR_CODE";

    final long DEFAULT_SLEEP_TIME = 3000;
    long mSleepTime = DEFAULT_SLEEP_TIME;

    final float BACKOFF_MULT = 1.25f;

    final int MAX_RETRY = 3;

    int mRetryRequest = -1;

    GoogleApiClientToken mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_auth);
        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            {
                GoogleApiFragment fragment = new GoogleApiFragment();
                transaction.add(fragment, fragment.createSimpleTag());
            }
            transaction.commit();
        }
        initialLogout();
    }

    @Override
    public GoogleApiClientToken newClientToken(GoogleApiFragment self) {
        GoogleApiClientToken result = new GoogleApiClientToken(newGoogleApiClient());
        result.setConnectSleepTime(1000);
        result.setDisconnectPendingTime(1);
        mGoogleApiClient = result;
        return result;
    }

    @Override
    public void onGooglePlayServiceRecoverCanceled(GoogleApiFragment self, int statusCode) {

    }

    /**
     * Google API Clientを生成する
     */
    protected abstract GoogleApiClient.Builder newGoogleApiClient();

    protected void initialLogout() {
        asyncUI(it -> {
            while (mGoogleApiClient == null) {
                Util.sleep(1);
                if (getLifecycleState() == LifecycleState.OnDestroyed) {
                    throw new IllegalStateException();
                }
            }

            mGoogleApiClient.executeGoogleApi(new GoogleApiTask<Object>() {
                @Override
                public Object executeTask(GoogleApiClient client) throws Exception {
                    try {
                        Plus.AccountApi.revokeAccessAndDisconnect(client).await();
                    } catch (Exception e) {

                    }

                    try {
                        client.clearDefaultAccountAndReconnect().await();
                    } catch (Exception e) {
                    }
                    Util.sleep(mSleepTime);
                    return null;
                }

                @Override
                public Object connectedFailed(GoogleApiClient client, ConnectionResult connectionResult) {
                    throw new IllegalStateException();
                }

                @Override
                public boolean isCanceled() {
                    return getLifecycleState() == LifecycleState.OnDestroyed;
                }
            });

            mGoogleApiClient.reconnect();
            loginOnBackground();
            return this;
        }).start();
    }

    /**
     * バックグラウンドでログインを行う
     */
    @SuppressLint("all")
    protected void loginOnBackground() {
        asyncUI(it -> {
            Util.sleep(mSleepTime);

            // ブロッキングログインを行う
            mGoogleApiClient.executeGoogleApi(new GoogleApiTask<Object>() {
                @Override
                public Object executeTask(GoogleApiClient client) throws Exception {
                    final BasicSettings basicSettings = FrameworkCentral.getSettings();
                    FwLog.google("login completed");
                    basicSettings.setLoginGoogleClientApi(true);
                    // Emailを保存する
                    try {
                        basicSettings.setLoginGoogleAccount(Plus.AccountApi.getAccountName(client));
                        FwLog.google("email connected success");
                    } catch (Exception e) {
                        FwLog.google("email connected fail");
                    }
                    basicSettings.commit();
                    onSuccess();
                    return null;
                }

                @Override
                public Object connectedFailed(GoogleApiClient client, ConnectionResult connectionResult) {
                    if (mRetryRequest >= 0) {
                        // MEMO ログイン直後は正常にログインできない端末があるので、リトライ機構とウェイトを設ける
                        --mRetryRequest;
                        if (mRetryRequest == 0) {
                            onFailed(connectionResult.getErrorCode());
                        } else {

                            FwLog.google("connect retry");
//                        getGoogleApiClientToken().reconnect();
                            mSleepTime *= BACKOFF_MULT;
                            loginOnBackground();
                        }
                    } else if (connectionResult.hasResolution()) {
                        FwLog.google("start auth dialog");
                        showLoginDialog(connectionResult);
                    } else {
                        onFailed(connectionResult.getErrorCode());
                    }
                    return null;
                }

                @Override
                public boolean isCanceled() {
                    return isFinishing();
                }
            });

            return null;
        }).start();
    }

    /**
     * Google Play Serviceにログインを行わせる
     */
    protected void showLoginDialog(final ConnectionResult connectionResult) {
        runOnUiThread(() -> {
            try {
                FwLog.google("Attempting to resolve failed connection");
                connectionResult.startResolutionForResult(GoogleAuthActivity.this, REQUEST_GOOGLE_CLIENT_AUTH);
            } catch (IntentSender.SendIntentException e) {
                FwLog.google("Exception while starting resolution activity", e);
            }
        });
    }

    protected void onSuccess() {
        runOnUiThread(() -> {
            setResult(RESULT_OK);
            finish();
        });
    }

    protected void onFailed(final int errorCode) {
        runOnUiThread(() -> {
            MaterialAlertDialog dialog = new MaterialAlertDialog(GoogleAuthActivity.this);
            dialog.setTitle(R.string.eglibrary_GoogleApi_Error_Title);
            dialog.setMessage(R.string.eglibrary_GoogleApi_Error_Message);
            dialog.setPositiveButton(R.string.eglibrary_GoogleApi_Error_Retry, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // ユーザーが一度操作しているから回数リセット
                    initialLogout();
                }
            });
            dialog.setNegativeButton(R.string.eglibrary_GoogleApi_Error_Cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent();
                    intent.putExtra(EXTRA_AUTH_ERROR_CODE, errorCode);
                    setResult(RESULT_CANCELED);
                    finish();
                }
            });
            dialog.setCancelable(false);
            dialog.show();
        });

    }

    /**
     * ログイン戻りの対応
     */
    @OnActivityResult(REQUEST_GOOGLE_CLIENT_AUTH)
    protected void resultGoogleClientAuth(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            // 再度ログイン処理
            mRetryRequest = MAX_RETRY;
            mSleepTime = DEFAULT_SLEEP_TIME;
            mGoogleApiClient.connect();
            loginOnBackground();
        } else {
            // キャンセルされた場合はログイン状態も解除しなければならない
            BasicSettings settings = FrameworkCentral.getSettings();
            settings.setLoginGoogleClientApi(false);
            settings.setLoginGoogleAccount("");
            settings.commit();
            onFailed(ConnectionResult.CANCELED);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (ContextUtil.isBackKeyEvent(event)) {
            return true;
        }

        return super.dispatchKeyEvent(event);
    }
}
