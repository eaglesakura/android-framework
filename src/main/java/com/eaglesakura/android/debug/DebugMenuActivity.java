package com.eaglesakura.android.debug;

import com.eaglesakura.android.framework.R;
import com.eaglesakura.android.framework.ui.BaseFragment;
import com.eaglesakura.android.framework.ui.content.ContentHolderActivity;

/**
 * デフォルトのデバッグメニューを表示するActivity
 */
public class DebugMenuActivity extends ContentHolderActivity {

    @Override
    protected int getDefaultLayoutId() {
        return R.layout.esm_activity_debugmenu;
    }

    @Override
    protected BaseFragment newDefaultContentFragment() {
        return new DebugContentFragment();
    }
}
