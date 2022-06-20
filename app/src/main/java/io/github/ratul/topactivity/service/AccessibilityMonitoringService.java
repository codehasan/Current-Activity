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
package io.github.ratul.topactivity.service;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;
import io.github.ratul.topactivity.utils.WindowUtil;
import io.github.ratul.topactivity.utils.DatabaseUtil;
import io.github.ratul.topactivity.model.NotificationMonitor;
import android.content.pm.PackageManager;
import java.util.List;
import android.content.pm.ResolveInfo;
import android.content.Context;

/**
 * Created by Wen on 16/02/2017.
 * Refactored by Ratul on 04/05/2022.
 */
public class AccessibilityMonitoringService extends AccessibilityService {
	private static AccessibilityMonitoringService sInstance;

	public static AccessibilityMonitoringService getInstance() {
		return sInstance;
	}

	public boolean isPackageInstalled(String packageName) {
		final PackageManager packageManager = getPackageManager();
		Intent intent = packageManager.getLaunchIntentForPackage(packageName);
		if (intent == null) {
			return false;
		}
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

	public boolean isSystemClass(String className) {
		try {
			ClassLoader.getSystemClassLoader().loadClass(className);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		if (WindowUtil.viewAdded && DatabaseUtil.isShowWindow() && DatabaseUtil.hasAccess()) {
			String act1 = String.valueOf(event.getClassName());
			String act2 = String.valueOf(event.getPackageName());

			if (isSystemClass(act1))
				return;
			WindowUtil.show(this, act2, act1);
		}
	}

	@Override
	public void onInterrupt() {
		sInstance = null;
	}

	@Override
	protected void onServiceConnected() {
		sInstance = this;
		super.onServiceConnected();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		sInstance = null;
		WindowUtil.dismiss(this);
		NotificationMonitor.cancelNotification(this);
		sendBroadcast(new Intent(QuickSettingsService.ACTION_UPDATE_TITLE));
		return super.onUnbind(intent);
	}
}
