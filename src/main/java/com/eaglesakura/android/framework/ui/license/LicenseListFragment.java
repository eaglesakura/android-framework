package com.eaglesakura.android.framework.ui.license;

import com.eaglesakura.android.framework.FwLog;
import com.eaglesakura.android.framework.R;
import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentDelegate;
import com.eaglesakura.android.framework.ui.support.SupportFragment;
import com.eaglesakura.android.framework.ui.support.annotation.BindInterface;
import com.eaglesakura.android.framework.ui.support.annotation.FragmentLayout;
import com.eaglesakura.android.framework.util.AppSupportUtil;
import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.android.rx.BackgroundTask;
import com.eaglesakura.android.rx.error.TaskCanceledException;
import com.eaglesakura.collection.DataCollection;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.material.widget.adapter.CardAdapter;
import com.eaglesakura.util.CollectionUtil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.UiThread;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * ライセンス一覧表記を行うFragment
 */
@FragmentLayout(resName = "esm_license_list")
public class LicenseListFragment extends SupportFragment {

    @Bind(resName = "eglibrary.Content.List")
    RecyclerView mCardList;

    @BindInterface
    Callback mCallback;

    @Override
    public void onAfterViews(SupportFragmentDelegate self, int flags) {
        mCardList.setLayoutManager(new LinearLayoutManager(getContext()));
        mCardList.setItemAnimator(new DefaultItemAnimator());
        mCardList.setHasFixedSize(false);
        mCardList.setAdapter(mAdapter);
    }

    @Override
    public void onAfterBindMenu(SupportFragmentDelegate self, Menu menu) {

    }

    @Override
    public void onAfterInjection(SupportFragmentDelegate self) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLicenses();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        getActionBar().setTitle(R.string.EsMaterial_Widget_License_Title);
    }

    @UiThread
    void loadLicenses() {
        asyncUI((BackgroundTask<DataCollection<LicenseItem>> task) -> {
            return listLicenses(AppSupportUtil.asCancelCallback(task));
        }).completed((result, task) -> {
            FwLog.system("Loaded %d items", result.size());
            mAdapter.getCollection().addAllAnimated(result.list());
        }).failed((error, task) -> {
            error.printStackTrace();
        }).start();
    }

    @SuppressLint("NewApi")
    DataCollection<LicenseItem> listLicenses(CancelCallback cancelCallback) throws IOException, TaskCanceledException {
        Set<String> ignoreFiles = CollectionUtil.asOtherSet(CollectionUtil.asList(getResources().getStringArray(R.array.EsMaterial_Widget_Licenses_IgnoreFiles)), it -> it);
        List<LicenseItem> result = new ArrayList<>();
        String LICENSE_PATH = "license";

        String[] files = getContext().getAssets().list(LICENSE_PATH);
        for (String file : files) {
            AppSupportUtil.assertNotCanceled(cancelCallback);

            // 拡張子が一致して、かつignoreリストに含まれていなければ登録する
            if (file.endsWith(".license") && !ignoreFiles.contains(file)) {
                FwLog.widget("load license(%s)", file);
                // １行目にOSSの表示名が格納されている
                String path = LICENSE_PATH + "/" + file;
                try (InputStream is = getContext().getAssets().open(path)) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    String title = reader.readLine();
                    FwLog.widget("OSS(%s)", title);

                    StringBuilder lineBuffer = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        lineBuffer.append(line).append("\n");
                    }

                    result.add(new LicenseItem(title, lineBuffer.toString(), path));
                }
            }
        }

        return new DataCollection<>(result);
    }


    CardAdapter<LicenseItem> mAdapter = new CardAdapter<LicenseItem>() {
        @Override
        protected View onCreateCard(ViewGroup parent, int viewType) {
            return LayoutInflater.from(getContext()).inflate(R.layout.esm_license_card, parent, false);
        }

        @Override
        protected void onBindCard(CardBind<LicenseItem> bind, int position) {
            LicenseItem item = bind.getItem();

            bind.query()
                    .id(R.id.eglibrary_Button_Item).clicked(view -> mCallback.requestDetail(LicenseListFragment.this, item))
                    .id(R.id.eglibrary_Item_Name).text(item.title);
        }
    };

    public static class LicenseItem {
        final String title;

        final String path;

        final String text;

        public LicenseItem(String title, String text, String path) {
            this.title = title;
            this.text = text;
            this.path = path;
        }

        public String getTitle() {
            return title;
        }

        public String getPath() {
            return path;
        }

        public String getText() {
            return text;
        }
    }

    public interface Callback {
        void requestDetail(LicenseListFragment self, LicenseItem item);
    }
}
