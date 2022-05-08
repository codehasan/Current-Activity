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

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.text.style.BackgroundColorSpan;
import io.github.ratul.topactivity.utils.DatabaseUtil;
import io.github.ratul.topactivity.ui.MainActivity;
import io.github.ratul.topactivity.utils.WindowUtil;
import io.github.ratul.topactivity.model.NotificationMonitor;
import io.github.ratul.topactivity.ui.BackgroundActivity;

/**
 * Created by Wen on 5/3/16.
 * Refactored by Ratul on 04/05/2022.
 */
@TargetApi(Build.VERSION_CODES.N)
public class QuickSettingsService extends TileService {
    public static final String ACTION_UPDATE_TITLE = "io.github.ratul.topactivity.ACTION.UPDATE_TITLE";
    private UpdateTileReceiver mReceiver;

    public static void updateTile(Context context) {
        TileService.requestListeningState(context, new ComponentName(context, QuickSettingsService.class));
        context.sendBroadcast(new Intent(QuickSettingsService.ACTION_UPDATE_TITLE));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mReceiver = new UpdateTileReceiver();
    }

    @Override
    public void onTileAdded() {
        DatabaseUtil.setQSTileAdded(true);
        sendBroadcast(new Intent(MainActivity.ACTION_STATE_CHANGED));
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
        DatabaseUtil.setQSTileAdded(false);
        sendBroadcast(new Intent(MainActivity.ACTION_STATE_CHANGED));
    }

    @Override
    public void onStartListening() {
        registerReceiver(mReceiver, new IntentFilter(ACTION_UPDATE_TITLE));
        super.onStartListening();
        updateTile();
    }

    @Override
    public void onStopListening() {
        unregisterReceiver(mReceiver);
        super.onStopListening();
    }

    @Override
    public void onClick() {
        if (DatabaseUtil.isShowWindow())
            return;
        if (!MainActivity.usageStats(this) || !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(MainActivity.EXTRA_FROM_QS_TILE, true);
            startActivityAndCollapse(intent);
        } else {
            if (DatabaseUtil.hasAccess() && AccessibilityMonitoringService.getInstance() == null)
                startService(new Intent().setClass(this, AccessibilityMonitoringService.class));
            DatabaseUtil.setIsShowWindow(!DatabaseUtil.isShowWindow());
            if (DatabaseUtil.isShowWindow()) {
                if (WindowUtil.sWindowManager == null)
                    WindowUtil.init(this);
                NotificationMonitor.showNotification(this, false);
                startService(new Intent(this, MonitoringService.class));
            } else {
                WindowUtil.dismiss(this);
                NotificationMonitor.showNotification(this, true);
            }
            sendBroadcast(new Intent(MainActivity.ACTION_STATE_CHANGED));
        }
    }

    private void updateTile() {
        getQsTile().setState(DatabaseUtil.isShowWindow() ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        getQsTile().updateTile();
    }

    class UpdateTileReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateTile();
        }
    }
}
