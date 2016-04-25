package com.eaglesakura.android.framework.ui;


import com.eaglesakura.android.framework.UnitTestCase;

import org.junit.Test;

import android.os.Parcel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class BackStackManagerTest extends UnitTestCase {

    @Test
    public void 正常にpushとpopが行える() throws Throwable {
        BackStackManager manager = new BackStackManager();
        manager.push("TEST_TAG");
        assertEquals(manager.mStackTags.size(), 1);
        manager.pop();
        assertEquals(manager.mStackTags.size(), 0);
    }

    @Test
    public void Bundleへの保存が行える() throws Throwable {
        Parcel parcel = Parcel.obtain();
        BackStackManager manager;
        BackStackManager manager2;

        {
            manager = new BackStackManager();
            manager.push("LEVEL1");
            manager.push("LEVEL2");
            assertEquals(manager.mStackTags.get(0), "LEVEL2");
            assertEquals(manager.mStackTags.get(1), "LEVEL1");

            manager.writeToParcel(parcel, 0);
        }

        parcel.setDataPosition(0);
        {
            manager2 = BackStackManager.CREATOR.createFromParcel(parcel);
            assertFalse(manager == manager2);   // 違う参照を指している

            assertEquals(manager2.mStackTags.size(), manager.mStackTags.size());
            assertEquals(manager2.mStackTags.get(0), "LEVEL2");
            assertEquals(manager2.mStackTags.get(1), "LEVEL1");
        }
    }
}
