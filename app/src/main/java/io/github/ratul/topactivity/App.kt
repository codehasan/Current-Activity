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
package io.github.ratul.topactivity

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import io.github.ratul.topactivity.managers.PopupManager
import io.github.ratul.topactivity.receivers.NotificationReceiver
import io.github.ratul.topactivity.ui.CopyToClipboardActivity

class App : Application() {

    lateinit var sharedPreferences: SharedPreferences
        private set

    lateinit var notificationManager: NotificationManagerCompat
        private set

    lateinit var clipboardManager: ClipboardManager
        private set

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        sharedPreferences = getSharedPreferences(packageName, 0)
        notificationManager = NotificationManagerCompat.from(this)

        PopupManager.addListener(NotificationReceiver.listener)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NotificationReceiver.CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_description)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val REPO_URL = "https://github.com/codehasan/Current-Activity"
        const val API_URL = "https://api.github.com/repos/codehasan/Current-Activity"

        lateinit var instance: App
            private set

        fun copyString(context: Context, str: String, msg: String) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                val clip = ClipData.newPlainText(context.getString(R.string.app_name), str)
                instance.clipboardManager.setPrimaryClip(clip)
            } else {
                val copyActivity = Intent(context, CopyToClipboardActivity::class.java)
                    .putExtra(Intent.EXTRA_TEXT, str)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(copyActivity)
            }
            showToast(context, msg)
        }

        fun showToast(context: Context, message: Int) {
            showToast(context, context.getString(message))
        }

        fun showToast(context: Context, message: String) {
            try {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            } catch (_: Exception) {
                try {
                    Toast.makeText(instance, message, Toast.LENGTH_SHORT).show()
                } catch (_: Exception) {
                }
            }
        }
    }
}
