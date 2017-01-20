package com.eaglesakura.android.framework.ui.license;

import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentDelegate;
import com.eaglesakura.android.framework.ui.support.SupportFragment;
import com.eaglesakura.android.framework.ui.support.annotation.FragmentLayout;
import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.android.saver.BundleState;

import android.content.Context;
import android.view.Menu;
import android.widget.TextView;

/**
 * ライセンス詳細テキストを表示するFragment
 */
@FragmentLayout(resName = "esm_license_detail")
public class LicenseDetailFragment extends SupportFragment {

    @Bind(resName = "eglibrary.Item.Value")
    TextView mDetailView;

    @BundleState
    String mDetail;
    private String mTitle;

    @Override
    public void onAfterViews(SupportFragmentDelegate self, int flags) {
        mDetailView.setText(mDetail);
    }

    @Override
    public void onAfterBindMenu(SupportFragmentDelegate self, Menu menu) {

    }

    @Override
    public void onAfterInjection(SupportFragmentDelegate self) {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        getActionBar().setTitle(mTitle);
    }

    public void setLicense(String title, String detailText) {
        mTitle = title;
        mDetail = detailText;
    }
}
