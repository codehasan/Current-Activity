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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.ratul.topactivity.App
import io.github.ratul.topactivity.repository.DataRepository

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.getIntExtra(EXTRA_NOTIFICATION_ACTION, -1)) {
            ACTION_COPY -> {
                val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return
                val msg = intent.getStringExtra(Intent.EXTRA_ASSIST_CONTEXT) ?: return
                App.copyString(context, text, msg)
            }

            ACTION_STOP -> DataRepository.updateStatus(false)
        }
    }

    companion object {
        const val ACTION_COPY = 1
        const val ACTION_STOP = 2
        const val EXTRA_NOTIFICATION_ACTION = "command"
    }
}
