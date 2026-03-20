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
package io.github.ratul.topactivity.manager

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.github.ratul.topactivity.R
import io.github.ratul.topactivity.receivers.NotificationActionReceiver
import io.github.ratul.topactivity.repository.DataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class NotificationUiManager(private val context: Context) {

    private val notificationManager = NotificationManagerCompat.from(context)
    private val popupScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val packageLabel = context.getString(R.string.package_label)
    private val classLabel = context.getString(R.string.class_label)
    private val stopLabel = context.getString(R.string.stop)

    private val packageCopied = context.getString(R.string.package_copied)
    private val classCopied = context.getString(R.string.class_copied)

    fun show() {
        if (!notificationManager.areNotificationsEnabled()) return
        if (popupScope.isActive) return

        val serviceState = DataRepository.appState.value
        updateNotification(serviceState.pkg, serviceState.cls)

        popupScope.launch {
            DataRepository.appState.collectLatest { state ->
                if (!state.running) {
                    hide()
                    return@collectLatest
                }

                if (state.pkg.isNotEmpty() && state.cls.isNotEmpty()) {
                    updateNotification(state.pkg, state.cls)
                }
            }
        }
    }

    private fun hide() {
        notificationManager.cancel(NOTIFICATION_ID)
        popupScope.cancel()
    }

    @SuppressLint("MissingPermission")
    private fun updateNotification(pkg: String, cls: String) {
        val copyPkg = NotificationCompat.Action(
            R.drawable.ic_package, packageLabel,
            actionCopyPendingIntent(
                PACKAGE_COPY_ACTION_ID, pkg, packageCopied
            )
        )
        val copyClass = NotificationCompat.Action(
            R.drawable.ic_class, classLabel,
            actionCopyPendingIntent(
                CLASS_COPY_ACTION_ID, cls, classCopied
            )
        )
        val stop =
            NotificationCompat.Action(R.drawable.ic_cancel, stopLabel, actionStopPendingIntent())

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(pkg)
            .setSmallIcon(R.drawable.ic_logo)
            .setContentText(cls)
            .setAutoCancel(false)
            .setOngoing(true)
            .addAction(copyPkg)
            .addAction(copyClass)
            .addAction(stop)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun actionCopyPendingIntent(
        requestCode: Int,
        text: String,
        message: String
    ): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            putExtra(
                NotificationActionReceiver.EXTRA_NOTIFICATION_ACTION,
                NotificationActionReceiver.ACTION_COPY
            )
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_ASSIST_CONTEXT, message)
        }
        return PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun actionStopPendingIntent(): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            putExtra(
                NotificationActionReceiver.EXTRA_NOTIFICATION_ACTION,
                NotificationActionReceiver.ACTION_STOP
            )
        }
        return PendingIntent.getBroadcast(
            context, STOP_ACTION_ID, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val NOTIFICATION_ID = 62345
        const val PACKAGE_COPY_ACTION_ID = 3429872
        const val CLASS_COPY_ACTION_ID = 3429873
        const val STOP_ACTION_ID = 908435
        const val CHANNEL_ID = "activity_info"
    }
}