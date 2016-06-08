package com.eaglesakura.freezer;

import android.os.Bundle;

import java.lang.reflect.Field;

public interface Freezer {
    void onSaveInstance(Bundle state, String key, Object srcObject, Field srcField) throws Throwable;

    void onRestoreInstance(Bundle state, String key, Object dstObject, Field dstField) throws Throwable;
}
