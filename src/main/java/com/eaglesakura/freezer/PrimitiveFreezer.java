package com.eaglesakura.freezer;

import android.os.Bundle;
import android.os.Parcelable;

import java.io.Serializable;
import java.lang.reflect.Field;

class PrimitiveFreezer implements Freezer {
    @Override
    public void onSaveInstance(Bundle state, String key, Object srcObject, Field srcField) throws Throwable {
        Class<?> type = srcField.getType();

        if (boolean.class.equals(type)) {
            state.putBoolean(key, srcField.getBoolean(srcObject));
        } else if (byte.class.equals(type)) {
            state.putByte(key, srcField.getByte(srcObject));
        } else if (short.class.equals(type)) {
            state.putShort(key, srcField.getShort(srcObject));
        } else if (int.class.equals(type)) {
            state.putInt(key, srcField.getInt(srcObject));
        } else if (long.class.equals(type)) {
            state.putLong(key, srcField.getLong(srcObject));
        } else if (float.class.equals(type)) {
            state.putFloat(key, srcField.getFloat(srcObject));
        } else if (double.class.equals(type)) {
            state.putDouble(key, srcField.getDouble(srcObject));
        } else if (String.class.equals(type)) {
            state.putString(key, (String) srcField.get(srcObject));
        } else if (BundleFreezer.instanceOf(type, Parcelable.class)) {
            state.putParcelable(key, (Parcelable) srcField.get(srcObject));
        } else if (BundleFreezer.instanceOf(type, Serializable.class)) {
            state.putSerializable(key, (Serializable) srcField.get(srcObject));
        } else {
            throw new IllegalArgumentException("key : " + key);
        }
    }

    @Override
    public void onRestoreInstance(Bundle state, String key, Object dstObject, Field dstField) throws Throwable {
        Class<?> type = dstField.getType();

        if (boolean.class.equals(type)) {
            dstField.setBoolean(dstObject, state.getBoolean(key, dstField.getBoolean(dstObject)));
        } else if (byte.class.equals(type)) {
            dstField.setByte(dstObject, state.getByte(key, dstField.getByte(dstObject)));
        } else if (short.class.equals(type)) {
            dstField.setShort(dstObject, state.getShort(key, dstField.getShort(dstObject)));
        } else if (int.class.equals(type)) {
            dstField.setInt(dstObject, state.getInt(key, dstField.getInt(dstObject)));
        } else if (long.class.equals(type)) {
            dstField.setLong(dstObject, state.getLong(key, dstField.getLong(dstObject)));
        } else if (float.class.equals(type)) {
            dstField.setFloat(dstObject, state.getFloat(key, dstField.getFloat(dstObject)));
        } else if (double.class.equals(type)) {
            dstField.setDouble(dstObject, state.getDouble(key, dstField.getDouble(dstObject)));
        } else if (String.class.equals(type)) {
            dstField.set(dstObject, state.getString(key, (String) dstField.get(dstObject)));
        } else if (BundleFreezer.instanceOf(type, Parcelable.class)) {
            dstField.set(dstObject, state.getParcelable(key));
        } else if (BundleFreezer.instanceOf(type, Serializable.class)) {
            dstField.set(dstObject, state.getSerializable(key));
        } else {
            throw new IllegalArgumentException("key : " + key);
        }
    }
}
