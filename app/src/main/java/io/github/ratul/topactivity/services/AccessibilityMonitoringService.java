/*
 *   Copyright (C) 2022 Ratul Hasan
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package io.github.ratul.topactivity.services;

import static io.github.ratul.topactivity.utils.NullSafety.isNullOrEmpty;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import io.github.ratul.topactivity.BuildConfig;
import io.github.ratul.topactivity.utils.DatabaseUtil;
import io.github.ratul.topactivity.utils.WindowUtil;

/**
 * Created by Wen on 16/02/2017.
 * Refactored by Ratul on 04/05/2022.
 */
@SuppressLint("AccessibilityPolicy")
public class AccessibilityMonitoringService extends AccessibilityService {
    private static AccessibilityMonitoringService instance;

    public static AccessibilityMonitoringService getInstance() {
        return instance;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (WindowUtil.isViewVisible() && DatabaseUtil.isShowingWindow()) {
            CharSequence pkgName = event.getPackageName();
            CharSequence className = event.getClassName();

            if (!isNullOrEmpty(pkgName) &&
                    !isNullOrEmpty(className) &&
                    !isSystemClass(className.toString())) {
                if (BuildConfig.DEBUG) {
                    Log.d("AccessibilityService", "Pkg: " + pkgName + ", Class: " + className);
                }
                WindowUtil.show(this, pkgName.toString(), className.toString());
            }
        }
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    protected void onServiceConnected() {
        instance = this;
        super.onServiceConnected();
    }

    @Override
    public void onRebind(Intent intent) {
        instance = this;
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        instance = null;
        return true;
    }

    private boolean isSystemClass(String className) {
        try {
            return ClassLoader.getSystemClassLoader().loadClass(className) != null;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
