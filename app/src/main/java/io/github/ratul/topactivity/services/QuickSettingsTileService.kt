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
package io.github.ratul.topactivity.services

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.core.content.ContextCompat
import io.github.ratul.topactivity.managers.PopupManager
import io.github.ratul.topactivity.managers.PopupStateListener
import io.github.ratul.topactivity.ui.MainActivity

class QuickSettingsTileService : TileService() {

    private val tileUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            syncTileState()
        }
    }

    private val popupListener = object : PopupStateListener {
        override fun onPopupShown() = syncTileState()
        override fun onPopupDismissed() = syncTileState()
    }

    override fun onCreate() {
        super.onCreate()
        PopupManager.addListener(popupListener)
    }

    override fun onDestroy() {
        PopupManager.removeListener(popupListener)
        super.onDestroy()
    }

    override fun onTileAdded() {
        syncTileState()
    }

    override fun onStartListening() {
        ContextCompat.registerReceiver(
            applicationContext, tileUpdateReceiver,
            IntentFilter(ACTION_UPDATE_TILE), ContextCompat.RECEIVER_EXPORTED
        )
        syncTileState()
        super.onStartListening()
    }

    override fun onStopListening() {
        applicationContext.unregisterReceiver(tileUpdateReceiver)
        super.onStopListening()
    }

    @SuppressLint("StartActivityAndCollapseDeprecated")
    override fun onClick() {
        if (PopupManager.isActive) {
            PopupManager.dismiss()
            return
        }

        val intent = Intent(this, MainActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra(MainActivity.EXTRA_FROM_QS_TILE, true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startActivityAndCollapse(
                PendingIntent.getActivity(
                    this, 7456435, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }

    private fun syncTileState() {
        val tile = qsTile ?: return
        tile.state = if (PopupManager.isActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.updateTile()
    }

    companion object {
        private const val ACTION_UPDATE_TILE = "io.github.ratul.topactivity.ACTION_UPDATE_TILE"

        fun requestUpdate(context: Context) {
            TileService.requestListeningState(
                context.applicationContext,
                ComponentName(context, QuickSettingsTileService::class.java)
            )
            context.sendBroadcast(Intent(ACTION_UPDATE_TILE))
        }
    }
}
