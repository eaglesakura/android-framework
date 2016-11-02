package com.eaglesakura.material.widget.support;

import com.eaglesakura.android.aquery.AQuery;
import com.eaglesakura.android.framework.R;
import com.eaglesakura.android.framework.delegate.fragment.SupportFragmentDelegate;
import com.eaglesakura.android.framework.ui.progress.ProgressStackManager;
import com.eaglesakura.android.framework.ui.progress.ProgressToken;
import com.eaglesakura.android.framework.ui.support.SupportFragment;
import com.eaglesakura.android.framework.ui.support.annotation.FragmentLayout;
import com.eaglesakura.material.widget.MaterialProgressView;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;

/**
 * 処理中のTapGuardViewを表示する
 */
@FragmentLayout(resName = "esm_progress")
public class SupportProgressFragment extends SupportFragment {

    public SupportProgressFragment() {
        mProgressStackManager.setListener(mStackManagerListener);
    }

    @Override
    public void onAfterViews(SupportFragmentDelegate self, int flags) {

    }

    @Override
    public void onAfterBindMenu(SupportFragmentDelegate self, Menu menu) {

    }

    @Override
    public void onAfterInjection(SupportFragmentDelegate self) {

    }

    @NonNull
    private final ProgressStackManager mProgressStackManager = new ProgressStackManager(mLifecycleDelegate.getCallbackQueue());

    public ProgressStackManager getProgressStackManager() {
        return mProgressStackManager;
    }

    private ProgressStackManager.Listener mStackManagerListener = new ProgressStackManager.Listener() {
        @Override
        public void onProgressStarted(@NonNull ProgressStackManager self, @NonNull ProgressToken token) {
            AQuery q = new AQuery(getView());
            q.id(R.id.Item_Progress)
                    .ifPresent(MaterialProgressView.class, it -> {
                        it.setText(token.getMessage());
                        it.setVisibility(View.VISIBLE);
                    });
        }

        @Override
        public void onProgressTopChanged(@NonNull ProgressStackManager self, @NonNull ProgressToken topToken) {
            AQuery q = new AQuery(getView());
            q.id(R.id.Item_Progress)
                    .ifPresent(MaterialProgressView.class, it -> {
                        it.setText(topToken.getMessage());
                        it.setVisibility(View.VISIBLE);
                    });
        }

        @Override
        public void onProgressFinished(@NonNull ProgressStackManager self) {
            AQuery q = new AQuery(getView());
            q.id(R.id.Item_Progress)
                    .ifPresent(MaterialProgressView.class, it -> {
                        it.setVisibility(View.INVISIBLE);
                    });
        }
    };

    /**
     * Fragmentを付与する
     */
    public static void attach(AppCompatActivity activity, @IdRes int containerId) {
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        transaction.add(containerId, new SupportProgressFragment(), SupportProgressFragment.class.getName());
        transaction.commit();
    }

    /**
     * 非同期処理のProgressを表示する
     */
    public static ProgressToken pushProgress(SupportFragment sender, String message) {
        ProgressStackManager progressStackManager = sender.findInterfaceOrThrow(SupportProgressFragment.class).getProgressStackManager();
        ProgressToken token = ProgressToken.fromMessage(progressStackManager, message);
        progressStackManager.push(token);

        return token;
    }
}
