package com.eaglesakura.freezer;

import com.eaglesakura.util.ReflectionUtil;
import com.eaglesakura.util.StringUtil;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BundleFreezer {

    private final Object mTarget;

    private final Bundle mState;

    private final Map<Class, Freezer> mFreezers;

    private String mTag;

    BundleFreezer(Object target, Bundle state, String keyTag, Map<Class, Freezer> freezers) {
        mTarget = target;
        mState = state;
        mFreezers = freezers;
        mTag = keyTag;

        if (mTarget == null || mState == null || mFreezers == null) {
            throw new IllegalStateException();
        }
    }

    @NonNull
    String getKey(@NonNull Field field) {
        String result = mTarget.getClass().toString() + "@" + field.getName();

        if (StringUtil.isEmpty(mTag)) {
            return result;
        } else {
            return mTag + "@" + result;
        }
    }

    static boolean instanceOf(Class checkType, Class clazz) {
        try {
            return checkType.asSubclass(clazz) != null;
        } catch (Exception e) {
        }
        return false;
    }

    @NonNull
    Freezer getFreezer(@NonNull Field field) {
        Class<?> type = field.getType();
        Freezer result = mFreezers.get(type);
        if (result == null) {
            if (instanceOf(type, Parcelable.class)) {
                result = mFreezers.get(Parcelable.class);
            } else if (instanceOf(type, Serializable.class)) {
                result = mFreezers.get(Serializable.class);
            }
        }

        if (result == null) {
            throw new IllegalStateException("Field : " + field.getName());
        }

        return result;
    }

    /**
     * ステートの保存を行う
     */
    void save() {
        List<Field> fields = ReflectionUtil.listAnnotationFields(mTarget.getClass(), BundleState.class);
        for (Field field : fields) {
            field.setAccessible(true);
            String key = getKey(field);
            Freezer freezer = getFreezer(field);
            if (freezer == null) {
                throw new IllegalStateException("freeze failed :: " + key);
            }

            try {
                freezer.onSaveInstance(mState, key, mTarget, field);
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        }
    }

    /***
     * ステートのレストアを行う
     */
    void restore() {
        List<Field> fields = ReflectionUtil.listAnnotationFields(mTarget.getClass(), BundleState.class);
        for (Field field : fields) {
            field.setAccessible(true);
            String key = getKey(field);
            Freezer freezer = getFreezer(field);
            if (freezer == null) {
                throw new IllegalStateException("freeze failed :: " + key);
            }

            try {
                freezer.onRestoreInstance(mState, key, mTarget, field);
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public static Builder create(Bundle state) {
        return new Builder().state(state);
    }

    public static class Builder {
        private Object mTargetObject;

        private Bundle mState;

        private Map<Class, Freezer> mFreezerMap = new HashMap<>();

        private String mTag;

        Builder() {
            // デフォルトのフリーザを指定
            {
                PrimitiveFreezer primitiveFreezer = new PrimitiveFreezer();
                mFreezerMap.put(boolean.class, primitiveFreezer);
                mFreezerMap.put(byte.class, primitiveFreezer);
                mFreezerMap.put(short.class, primitiveFreezer);
                mFreezerMap.put(int.class, primitiveFreezer);
                mFreezerMap.put(long.class, primitiveFreezer);
                mFreezerMap.put(float.class, primitiveFreezer);
                mFreezerMap.put(double.class, primitiveFreezer);
                mFreezerMap.put(String.class, primitiveFreezer);
                mFreezerMap.put(Serializable.class, primitiveFreezer);
                mFreezerMap.put(Parcelable.class, primitiveFreezer);
            }
            {
                ArrayFreezer arrayFreezer = new ArrayFreezer();
                mFreezerMap.put(boolean[].class, arrayFreezer);
                mFreezerMap.put(byte[].class, arrayFreezer);
                mFreezerMap.put(short[].class, arrayFreezer);
                mFreezerMap.put(int[].class, arrayFreezer);
                mFreezerMap.put(long[].class, arrayFreezer);
                mFreezerMap.put(float[].class, arrayFreezer);
                mFreezerMap.put(double[].class, arrayFreezer);
                mFreezerMap.put(String[].class, arrayFreezer);
                mFreezerMap.put(Parcelable[].class, arrayFreezer);
                mFreezerMap.put(ArrayList.class, arrayFreezer);
            }
        }

        private Builder state(Bundle bundle) {
            mState = bundle;
            return this;
        }

        public Builder target(Object target) {
            mTargetObject = target;
            if (target instanceof Fragment) {
                tag(((Fragment) target).getTag());
            }
            return this;
        }

        public Builder freezer(Class clazz, Freezer freezer) {
            mFreezerMap.put(clazz, freezer);
            return this;
        }

        /**
         * 同じClassが複数ある場合、タグを指定することで保存・復旧時に競合が発生しないようにする
         */
        public Builder tag(String keyTag) {
            mTag = keyTag;
            return this;
        }

        public Builder save() {
            new BundleFreezer(mTargetObject, mState, mTag, mFreezerMap).save();
            return this;
        }

        /**
         * restoreを行う。
         * Bundleがnullである場合は何もしない
         */
        public Builder restore() {
            if (mState != null) {
                new BundleFreezer(mTargetObject, mState, mTag, mFreezerMap).restore();
            }
            return this;
        }
    }
}