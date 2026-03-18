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

import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import androidx.core.content.ContextCompat;

import io.github.ratul.topactivity.receivers.NotificationReceiver;
import io.github.ratul.topactivity.ui.MainActivity;
import io.github.ratul.topactivity.utils.DatabaseUtil;
import io.github.ratul.topactivity.managers.PopupManager;

/**
 * Created by Wen on 5/3/16.
 * Refactored by Ratul on 04/05/2022.
 */
public class QuickSettingsTileService extends TileService {
    private static final String ACTION_UPDATE_TITLE = "io.github.ratul.topactivity.ACTION_UPDATE_TILE";
    private UpdateTileReceiver mReceiver;

    public static void updateTile(Context context) {
        TileService.requestListeningState(context.getApplicationContext(),
                new ComponentName(context, QuickSettingsTileService.class));
        context.sendBroadcast(new Intent(QuickSettingsTileService.ACTION_UPDATE_TITLE));
    }

    public void updateTile() {
        getQsTile().setState(DatabaseUtil.isShowingWindow() ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        getQsTile().updateTile();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mReceiver = new UpdateTileReceiver();
    }

    @Override
    public void onTileAdded() {
        sendBroadcast(new Intent(MainActivity.ACTION_STATE_CHANGED));
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
        sendBroadcast(new Intent(MainActivity.ACTION_STATE_CHANGED));
    }

    @Override
    public void onStartListening() {
        ContextCompat.registerReceiver(getApplicationContext(), mReceiver,
                new IntentFilter(ACTION_UPDATE_TITLE), ContextCompat.RECEIVER_EXPORTED);
        updateTile();
        super.onStartListening();
    }

    @Override
    public void onStopListening() {
        getApplicationContext().unregisterReceiver(mReceiver);
        super.onStopListening();
    }

    @SuppressLint("StartActivityAndCollapseDeprecated")
    @Override
    public void onClick() {
        if (DatabaseUtil.isShowingWindow() && PopupManager.isViewVisible()) {
            DatabaseUtil.setShowingWindow(false);
            NotificationReceiver.cancelNotification();
            PopupManager.dismiss(this);
            sendBroadcast(new Intent(MainActivity.ACTION_STATE_CHANGED));
            updateTile();
            return;
        }

        Intent intent = new Intent(this, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(MainActivity.EXTRA_FROM_QS_TILE, true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startActivityAndCollapse(PendingIntent.getActivity(this,
                    7456435, intent, FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE));
        } else {
            startActivityAndCollapse(intent);
        }
    }

    class UpdateTileReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateTile();
        }
    }
}
