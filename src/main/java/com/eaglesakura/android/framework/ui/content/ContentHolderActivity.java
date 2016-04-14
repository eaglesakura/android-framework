package com.eaglesakura.android.framework.ui.content;

import com.eaglesakura.android.framework.R;
import com.eaglesakura.android.framework.ui.BaseActivity;
import com.eaglesakura.android.framework.ui.BaseFragment;
import com.eaglesakura.android.framework.ui.ChildFragmentHolder;
import com.eaglesakura.util.ReflectionUtil;
import com.eaglesakura.util.StringUtil;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;

/**
 * 親となるFragmentを指定して起動するActivityの雛形
 * <p/>
 * メインコンテンツは @+id/Content.Holder.Root を持たなければならない。
 * Toolbarは @+id/EsMaterial.Toolbar を自動的に検索し、存在するならToolbarとして自動設定する。
 */
public abstract class ContentHolderActivity extends BaseActivity {
    static final String EXTRA_ACTIVITY_LAYOUT = "EXTRA_ACTIVITY_LAYOUT";

    /**
     * 遷移対象のFragment Class
     */
    private static final String EXTRA_CONTENT_FRAGMENT_CLASS = "EXTRA_CONTENT_FRAGMENT_CLASS";

    /**
     * 遷移対象のArgment
     */
    private static final String EXTRA_CONTENT_FRAGMENT_ARGMENTS = "EXTRA_CONTENT_FRAGMENT_ARGMENTS";

    protected static final String TAG_CONTENT_FRAGMENT_MAIN = "fw.TAG_CONTENT_FRAGMENT_MAIN";

    private ChildFragmentHolder<Fragment> mFragmentMain = new ChildFragmentHolder<Fragment>(this, Fragment.class, R.id.Content_Holder_Root, TAG_CONTENT_FRAGMENT_MAIN) {
        @NonNull
        @Override
        protected Fragment newFragmentInstance(@Nullable Bundle savedInstanceState) throws Exception {
            String className = getIntent().getStringExtra(EXTRA_CONTENT_FRAGMENT_CLASS);
            if (!StringUtil.isEmpty(className)) {
                Fragment result = (Fragment) Class.forName(className).newInstance();
                Bundle argments = getIntent().getBundleExtra(EXTRA_CONTENT_FRAGMENT_ARGMENTS);
                if (argments != null) {
                    result.setArguments(argments);
                }
                return result;
            } else {
                return newDefaultContentFragment();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final int layoutId = getIntent().getIntExtra(EXTRA_ACTIVITY_LAYOUT, getDefaultLayoutId());
        if (layoutId == 0) {
            throw new IllegalStateException();
        }

        requestInjection(layoutId);
        Toolbar toolbar = findViewById(Toolbar.class, R.id.EsMaterial_Toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        mFragmentMain.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFragmentMain.onResume();
    }

    /**
     * デフォルトで使用されるレイアウトIDを取得する
     */
    @LayoutRes
    protected int getDefaultLayoutId() {
        return R.layout.activity_content_holder;
    }

    /**
     * 表示するコンテンツが指定されない場合のデフォルトコンテンツを開く
     */
    @NonNull
    protected abstract Fragment newDefaultContentFragment();

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
                                      Class<? extends ContentHolderActivity> activityClass, int activityLayoutId,
                                      Class<? extends BaseFragment> contentFragment, Bundle argments) {
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
