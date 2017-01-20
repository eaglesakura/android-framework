package com.eaglesakura.android.framework.ui.license;

import com.eaglesakura.android.framework.R;
import com.eaglesakura.android.framework.ui.support.SupportActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

/**
 * ライセンスの詳細表記用Activity
 */
public class LicenseDetailActivity extends SupportActivity {
    static final String TAG_LICENSE_CONTENT = "TAG_LICENSE_CONTENT";

    static final String EXTRA_LICENSE_TITLE = "EXTRA_LICENSE_TITLE";

    static final String EXTRA_LICENSE_TEXT = "EXTRA_LICENSE_TEXT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.esm_license_activity);
        setSupportActionBar(((Toolbar) findViewById(R.id.EsMaterial_Toolbar)));

        if (savedInstanceState == null) {
            LicenseDetailFragment fragment = new LicenseDetailFragment();
            fragment.setLicense(
                    getIntent().getStringExtra(EXTRA_LICENSE_TITLE),
                    getIntent().getStringExtra(EXTRA_LICENSE_TEXT)
            );

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.Content_Holder_Root, fragment, TAG_LICENSE_CONTENT)
                    .commit();
        }
    }

    /**
     * 表示を開始する
     */
    public static void startContent(Context context, String title, String text) {
        context.startActivity(
                new Intent(context, LicenseDetailActivity.class)
                        .putExtra(EXTRA_LICENSE_TITLE, title)
                        .putExtra(EXTRA_LICENSE_TEXT, text)
        );
    }
}
