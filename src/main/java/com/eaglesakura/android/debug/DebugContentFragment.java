package com.eaglesakura.android.debug;

import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.framework.FrameworkCentral;
import com.eaglesakura.android.framework.FwLog;
import com.eaglesakura.android.framework.R;
import com.eaglesakura.android.framework.ui.BaseFragment;
import com.eaglesakura.android.margarine.OnCheckedChanged;
import com.eaglesakura.android.margarine.OnClick;
import com.eaglesakura.android.util.ContextUtil;
import com.eaglesakura.util.LogUtil;

/**
 * デバッグ機能を集約したFragment
 */
public class DebugContentFragment extends BaseFragment {

    public DebugContentFragment() {
        requestInjection(R.layout.esm_fragment_debugmenu);
    }

    @Override
    protected void onAfterViews() {
        super.onAfterViews();
        AQuery q = new AQuery(getView());
        q.id(R.id.EsDebug_Info_PackageName).text(getContext().getPackageName());
        q.id(R.id.EsDebug_Info_AppVersionName).text("VersionName=" + ContextUtil.getVersionName(getContext()));
        q.id(R.id.EsDebug_Info_AppVersionCode).text("VersionCode=" + ContextUtil.getVersionCode(getContext()));
    }

    @OnClick(resName = "EsDebug.Dump.Local")
    void clickDumpLocal() {
        FwLog.system("clickDumpLocal()");
    }

    @OnCheckedChanged(resName = "EsDebug.DebugMode.Switch")
    void changeDebugModeEnable(boolean checked) {
        FwLog.system("changeDebugModeEnable(%s)", String.valueOf(checked));
    }
}
