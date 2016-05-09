package com.eaglesakura.android.framework.ui;

import com.eaglesakura.android.framework.FwLog;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
@Deprecated
public final class FragmentChooser implements Parcelable {
    private List<FragmentCache> mFragmentCaches = new ArrayList<>();

    public enum ReferenceType {
        /**
         * 強参照リクエスト
         */
        Strong,

        /**
         * 弱参照リクエスト
         */
        Weak,
    }

    public class FragmentCache {
        /**
         * 検索用タグ
         */
        private String tag;

        /**
         * リクエストされる参照タイプ
         */
        private ReferenceType referenceType = ReferenceType.Strong;

        /**
         * 強参照
         */
        private Fragment fragment;

        /**
         * 弱参照オブジェクト
         */
        private WeakReference<Fragment> weak;

        FragmentCache(ReferenceType type, Fragment fragment, String tag) {
            this.referenceType = type;
            this.tag = tag;
            set(fragment);
        }

        FragmentCache(String refType, String tag) {
            this.referenceType = ReferenceType.valueOf(refType);
            this.tag = tag;
        }


        /**
         * 選択対象のFragmentを取得する
         */
        public Fragment get() {
            if (fragment != null) {
                return fragment;
            } else if (weak != null) {
                return weak.get();
            } else {
                return null;
            }
        }

        /**
         * 値の設定を適切に行う
         */
        private void set(Fragment fragment) {
            switch (referenceType) {
                case Weak:
                    weak = new WeakReference<Fragment>(fragment);
                    break;
                default:
                    this.fragment = fragment;
                    break;
            }
        }
    }

    protected Callback mCallback = null;

    public FragmentChooser() {
    }

    public FragmentChooser(Callback callback) {
        setCallback(callback);
    }

    private FragmentChooser(Parcel in) {
//        LogUtil.log("decode FragmentChooser :: %s", getClass().getName());
        // 復元を行う
        List<String> tags = new ArrayList<>();
        List<String> refTypes = new ArrayList<>();

        in.readStringList(tags);
        in.readStringList(refTypes);

        if (tags.size() != refTypes.size()) {
            throw new IllegalStateException("tags.size() != refTypes.size()");
        }

        for (int i = 0; i < tags.size(); ++i) {
            String tag = tags.get(i);
            String refType = refTypes.get(i);

            mFragmentCaches.add(new FragmentCache(refType, tag));
        }

        FwLog.system("restore fragmentChooser(%d)", mFragmentCaches.size());
    }

    /**
     * 不要なキャッシュを排除する
     */
    public void compact() {
        Iterator<FragmentCache> iterator = mFragmentCaches.iterator();
        while (iterator.hasNext()) {
            FragmentCache cache = iterator.next();
            if (cache.get() == null) {
                iterator.remove();
            }
        }
    }

    private List<String> listReferenceTypes() {
        List<String> result = new ArrayList<String>();

        Iterator<FragmentCache> iterator = mFragmentCaches.iterator();
        while (iterator.hasNext()) {
            FragmentCache cache = iterator.next();

            result.add(cache.referenceType.name());
        }

        return result;
    }

    /**
     * Fragment一覧を生成する。
     * <br>
     * 参照切れのFragmentは登録しない
     */
    public List<Fragment> listExistFragments() {
        compact();
        List<Fragment> result = new ArrayList<Fragment>();
        for (FragmentCache cache : mFragmentCaches) {
            Fragment fragment = cache.get();
            if (fragment != null) {
                if (mCallback == null) {
                    result.add(fragment);
                } else if (mCallback.isFragmentExist(this, fragment)) {
                    result.add(fragment);
                }
            }
        }
        return result;
    }

    /**
     * 管理しているFragment数を取得する
     */
    public int getFragmentNum() {
        return mFragmentCaches.size();
    }

    /**
     * Fragmentを取得する
     */
    public Fragment getFragment(int index) {
        FragmentCache cache = mFragmentCaches.get(index);
        FragmentManager fragmentManager = getFragmentManager();

        Fragment result = fragmentManager.findFragmentByTag(cache.tag);
        if (result == null) {
            // ローカルキャッシュを得る
            return cache.get();
        } else {
            // FragmentManagerがもつものが正である
            cache.set(result);
            return result;
        }
    }

    /**
     * 管理しているFragmentのタグ一覧を取得する
     */
    private List<String> listTags() {
        List<String> result = new ArrayList<String>();

        Iterator<FragmentCache> iterator = mFragmentCaches.iterator();
        while (iterator.hasNext()) {
            FragmentCache cache = iterator.next();

            result.add(cache.tag);
        }

        return result;
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;

        restoreFragments();
    }

    /**
     * Fragment情報をレストアする
     */
    public void restoreFragments() {
        FragmentManager fragmentManager = getFragmentManager();

        Iterator<FragmentCache> iterator = mFragmentCaches.iterator();
        // Fragmentをそれぞれ探し、有効であれば登録する
        while (iterator.hasNext()) {
            FragmentCache cache = iterator.next();
            Fragment fragment = fragmentManager.findFragmentByTag(cache.tag);
            if (fragment != null && mCallback.isFragmentExist(this, fragment)) {
                cache.set(fragment);
            }
        }
    }

    private FragmentManager getFragmentManager() {
        return mCallback.getFragmentManager(this);
    }

    /**
     * Fragmentを追加する
     */
    public void addFragment(Fragment fragment, String tag) {
        addFragment(ReferenceType.Strong, fragment, tag, -1);
    }

    /**
     * Fragmentを条件付きで追加する
     */
    public void addFragment(ReferenceType type, Fragment fragment, String tag) {
        addFragment(type, fragment, tag, -1);
    }

    /**
     * Fragmentを条件指定で追加する
     */
    public void addFragment(ReferenceType type, Fragment fragment, String tag, int index) {
        FragmentCache cache = new FragmentCache(type, fragment, tag);
        if (index < 0) {
            mFragmentCaches.add(cache);
        } else {
            mFragmentCaches.add(index, cache);
        }
    }

    private FragmentCache findCache(String tag) {
        for (FragmentCache cache : mFragmentCaches) {
            if (cache.tag.equals(tag)) {
                // タグが一致したらコレ
                return cache;
            }
        }
        return null;
    }

    public Fragment find(String tag) {
        Fragment fragment = getFragmentManager().findFragmentByTag(tag);
        FragmentCache cache = findCache(tag);

        if (fragment != null && !mCallback.isFragmentExist(this, fragment)) {
            // Fragmentが無効であればnull
            fragment = null;
        }

        // キャッシュから得る
        if (fragment == null && cache != null) {
            fragment = cache.get();
        }

        // fragmentが無ければ生成リクエストを送る
        if (fragment == null) {
            fragment = mCallback.newFragment(this, tag);
        }

        if (fragment != null) {
            // Fragmentが存在するならばキャッシュ登録する
            if (cache == null) {
                cache = new FragmentCache(ReferenceType.Strong, fragment, tag);
                mFragmentCaches.add(cache);
            }
            cache.set(fragment);
        }

        return fragment;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // 書き込み前に容量を減らす
        compact();

        List<String> tags = listTags();
        List<String> refTypes = listReferenceTypes();

        dest.writeStringList(tags);
        dest.writeStringList(refTypes);
    }

    public static final Creator<FragmentChooser> CREATOR
            = new Creator<FragmentChooser>() {
        public FragmentChooser createFromParcel(Parcel in) {
            return new FragmentChooser(in);
        }

        public FragmentChooser[] newArray(int size) {
            return new FragmentChooser[size];
        }
    };

    /**
     * mCallback
     */
    public interface Callback {
        /**
         * 検索対象のFragmentManagerを取得する
         */
        @NonNull
        FragmentManager getFragmentManager(@NonNull FragmentChooser self);

        /**
         * このFragmentが有効であればtrue
         */
        boolean isFragmentExist(@NonNull FragmentChooser self, @NonNull Fragment fragment);

        /**
         * Fragmentの生成を行わせる
         */
        @NonNull
        Fragment newFragment(@NonNull FragmentChooser self, @NonNull String requestTag);
    }
}
