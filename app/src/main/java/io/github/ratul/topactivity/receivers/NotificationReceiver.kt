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
package io.github.ratul.topactivity.receivers

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import io.github.ratul.topactivity.App
import io.github.ratul.topactivity.R
import io.github.ratul.topactivity.managers.PopupManager
import io.github.ratul.topactivity.managers.PopupStateListener
import io.github.ratul.topactivity.utils.DatabaseUtil

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.getIntExtra(EXTRA_NOTIFICATION_ACTION, -1)) {
            ACTION_COPY -> {
                val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return
                val msg = intent.getStringExtra(Intent.EXTRA_ASSIST_CONTEXT) ?: return
                App.copyString(context, text, msg)
            }
            ACTION_STOP -> PopupManager.dismiss()
        }
    }

    companion object {
        const val NOTIFICATION_ID = 62345
        const val CHANNEL_ID = "activity_info"
        private const val ACTION_COPY = 1
        private const val ACTION_STOP = 2
        private const val EXTRA_NOTIFICATION_ACTION = "command"

        val listener = object : PopupStateListener {
            override fun onPopupDismissed() = cancelNotification()
            override fun onActivityInfoChanged(packageName: String, className: String) {
                if (DatabaseUtil.showNotification) {
                    showNotification(App.instance, packageName, className)
                }
            }
        }

        fun showNotification(context: Context, title: String, message: String) {
            val notificationManager = App.instance.notificationManager
            if (!notificationManager.areNotificationsEnabled()) return

            val copyPkg = NotificationCompat.Action(
                R.drawable.ic_package, context.getString(R.string.package_label),
                getCopyPendingIntent(context, 3429872, title, R.string.package_copied)
            )
            val copyClass = NotificationCompat.Action(
                R.drawable.ic_class, context.getString(R.string.class_label),
                getCopyPendingIntent(context, 3429873, message, R.string.class_copied)
            )
            val stop = NotificationCompat.Action(
                R.drawable.ic_cancel, context.getString(R.string.stop),
                getStopPendingIntent(context)
            )

            val notification = NotificationCompat.Builder(context.applicationContext, CHANNEL_ID)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_logo)
                .setContentText(message)
                .setAutoCancel(false)
                .setOngoing(true)
                .addAction(copyPkg)
                .addAction(copyClass)
                .addAction(stop)
                .build()

            notificationManager.notify(NOTIFICATION_ID, notification)
        }

        fun cancelNotification() {
            App.instance.notificationManager.cancel(NOTIFICATION_ID)
        }

        private fun getCopyPendingIntent(
            context: Context, requestCode: Int, text: String, @StringRes message: Int
        ): PendingIntent {
            val intent = Intent(context, NotificationReceiver::class.java)
                .putExtra(EXTRA_NOTIFICATION_ACTION, ACTION_COPY)
                .putExtra(Intent.EXTRA_TEXT, text)
                .putExtra(Intent.EXTRA_ASSIST_CONTEXT, context.getString(message))
            return PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        private fun getStopPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, NotificationReceiver::class.java)
                .putExtra(EXTRA_NOTIFICATION_ACTION, ACTION_STOP)
            return PendingIntent.getBroadcast(
                context, 908435, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
