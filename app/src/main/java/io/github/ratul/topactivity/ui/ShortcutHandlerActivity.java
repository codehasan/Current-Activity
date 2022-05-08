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
package io.github.ratul.topactivity.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import io.github.ratul.topactivity.utils.DatabaseUtil;
import io.github.ratul.topactivity.utils.WindowUtil;
import io.github.ratul.topactivity.model.NotificationMonitor;
import io.github.ratul.topactivity.service.MonitoringService;
import io.github.ratul.topactivity.service.AccessibilityMonitoringService;

/**
 * Created by Wen on 16/02/2017.
 * Refactored by Ratul on 04/05/2022.
 */
@TargetApi(Build.VERSION_CODES.N)
public class ShortcutHandlerActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!MainActivity.usageStats(this) || !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(MainActivity.EXTRA_FROM_QS_TILE, true);
            startActivity(intent);
            finish();
        } else if (AccessibilityMonitoringService.getInstance() == null && DatabaseUtil.hasAccess())
            startService(new Intent().setClass(this, AccessibilityMonitoringService.class));

        boolean isShow = !DatabaseUtil.isShowWindow();
        DatabaseUtil.setIsShowWindow(isShow);
        if (!isShow) {
            WindowUtil.dismiss(this);
            NotificationMonitor.showNotification(this, true);
        } else {
            WindowUtil.init(this);
            NotificationMonitor.showNotification(this, false);
            startService(new Intent(this, MonitoringService.class));
        }
        sendBroadcast(new Intent(MainActivity.ACTION_STATE_CHANGED));
        finish();
    }
}
