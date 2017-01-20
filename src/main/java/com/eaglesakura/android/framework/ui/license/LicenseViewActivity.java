package com.eaglesakura.android.framework.ui.license;

import com.eaglesakura.android.framework.R;
import com.eaglesakura.android.framework.ui.support.SupportActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

/**
 * 各種ライブラリのLicenseを自動で表示するためのActivity
 */
public class LicenseViewActivity extends SupportActivity implements LicenseListFragment.Callback {
    static final String TAG_LICENSE_CONTENT = "TAG_LICENSE_CONTENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.esm_license_activity);
        setSupportActionBar(((Toolbar) findViewById(R.id.EsMaterial_Toolbar)));

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.Content_Holder_Root, new LicenseListFragment(), TAG_LICENSE_CONTENT)
                    .commit();
        }
    }

    /**
     * 詳細画面を開く
     */
    @Override
    public void requestDetail(LicenseListFragment self, LicenseListFragment.LicenseItem item) {
        LicenseDetailActivity.startContent(this, item.getTitle(), item.getText());
    }

    /**
     * 表示を開始する
     */
    public static void startContent(Context context) {
        context.startActivity(new Intent(context, LicenseViewActivity.class));
    }
}
