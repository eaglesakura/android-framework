package com.eaglesakura.android.framework.delegate.activity;

import com.eaglesakura.android.framework.R;
import com.eaglesakura.android.framework.delegate.lifecycle.ActivityLifecycleDelegate;
import com.eaglesakura.android.framework.delegate.lifecycle.LifecycleDelegate;
import com.eaglesakura.android.framework.ui.ChildFragmentHolder;
import com.eaglesakura.android.framework.ui.support.SupportFragment;
import com.eaglesakura.android.rx.LifecycleState;
import com.eaglesakura.android.rx.event.OnCreateEvent;
import com.eaglesakura.util.StringUtil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;

public class ContentHolderActivityDelegate {
    static final String EXTRA_ACTIVITY_LAYOUT = "EXTRA_ACTIVITY_LAYOUT";

    /**
     * 遷移対象のFragment Class
     */
    private static final String EXTRA_CONTENT_FRAGMENT_CLASS = "EXTRA_CONTENT_FRAGMENT_CLASS";

    /**
     * 遷移対象のArgment
     */
    private static final String EXTRA_CONTENT_FRAGMENT_ARGMENTS = "EXTRA_CONTENT_FRAGMENT_ARGMENTS";

    public static final String TAG_CONTENT_FRAGMENT_MAIN = "fw.TAG_CONTENT_FRAGMENT_MAIN";

    private ChildFragmentHolder<Fragment> mFragmentMain;

    @NonNull
    private ContentHolderActivityCompat mHolderCompat;

    public interface ContentHolderActivityCompat {
        @LayoutRes
        int getDefaultLayoutId(@NonNull ContentHolderActivityDelegate self);

        void setSupportActionBar(Toolbar toolbar);

        /**
         * 表示するコンテンツが指定されない場合のデフォルトコンテンツを開く
         */
        @NonNull
        Fragment newDefaultContentFragment(@NonNull ContentHolderActivityDelegate self);

        @NonNull
        Activity getActivity(@NonNull ContentHolderActivityDelegate self);

        @NonNull
        FragmentManager getFragmentManager(@NonNull ContentHolderActivityDelegate self);
    }

    public ContentHolderActivityDelegate(@NonNull ContentHolderActivityCompat compat, @NonNull ActivityLifecycleDelegate lifecycle) {
        mHolderCompat = compat;

        mFragmentMain = new ChildFragmentHolder<Fragment>(compat.getActivity(this), Fragment.class, R.id.Content_Holder_Root, TAG_CONTENT_FRAGMENT_MAIN) {
            @NonNull
            @Override
            protected Fragment newFragmentInstance(@Nullable Bundle savedInstanceState) throws Exception {
                Activity activity = mHolderCompat.getActivity(ContentHolderActivityDelegate.this);

                String className = activity.getIntent().getStringExtra(EXTRA_CONTENT_FRAGMENT_CLASS);
                if (!StringUtil.isEmpty(className)) {
                    Fragment result = (Fragment) Class.forName(className).newInstance();
                    Bundle argments = activity.getIntent().getBundleExtra(EXTRA_CONTENT_FRAGMENT_ARGMENTS);
                    if (argments != null) {
                        result.setArguments(argments);
                    }
                    return result;
                } else {
                    return mHolderCompat.newDefaultContentFragment(ContentHolderActivityDelegate.this);
                }
            }

            @NonNull
            @Override
            public Fragment get() {
                return mHolderCompat.getFragmentManager(ContentHolderActivityDelegate.this).findFragmentByTag(TAG_CONTENT_FRAGMENT_MAIN);
            }
        };

        lifecycle.getSubscription().getObservable().subscribe(it -> {
            LifecycleState state = it.getState();
            switch (state) {
                case OnCreated:
                    onCreate((OnCreateEvent) it);
                    break;
                case OnResumed:
                    mFragmentMain.onResume();
                    break;
            }
        });
    }

    void onCreate(OnCreateEvent event) {
        Activity activity = mHolderCompat.getActivity(this);

        @LayoutRes
        int layoutId = activity.getIntent().getIntExtra(EXTRA_ACTIVITY_LAYOUT, mHolderCompat.getDefaultLayoutId(this));
        if (layoutId == 0) {
            layoutId = R.layout.activity_content_holder;
        }

        activity.setContentView(layoutId);

        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.EsMaterial_Toolbar);
        if (toolbar != null) {
            mHolderCompat.setSupportActionBar(toolbar);
        }

        mFragmentMain.onCreate(event.getBundle());
    }


    /**
     * コンテンツコントロール用のFragmentを取得する
     */
    public Fragment getContentFragment() {
        return mFragmentMain.get();
    }

    /**
     * コンテンツ表示用Intentを生成する
     */
    public static Intent createIntent(Context context,
                                      Class<? extends Activity> activityClass, int activityLayoutId,
                                      Class<? extends SupportFragment> contentFragment, Bundle argments) {
        Intent intent = new Intent(context, activityClass);
        intent.putExtra(EXTRA_CONTENT_FRAGMENT_CLASS, contentFragment.getName());
        if (activityLayoutId != 0) {
            intent.putExtra(EXTRA_ACTIVITY_LAYOUT, activityLayoutId);
        } else {

        }
        if (argments != null) {
            intent.putExtra(EXTRA_CONTENT_FRAGMENT_ARGMENTS, argments);
        }
        return intent;
    }
}
