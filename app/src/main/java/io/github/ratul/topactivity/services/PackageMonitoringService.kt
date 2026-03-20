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

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import io.github.ratul.topactivity.repository.DataRepository

class PackageMonitoringService : Service() {

    private val binder = LocalBinder()
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var usageStats: UsageStatsManager

    private val observerTask = object : Runnable {
        override fun run() {
            val serviceState = DataRepository.appState.value

            if (!serviceState.running) {
                handler.removeCallbacks(this)
                stopSelf()
                return
            }

            val (pkg, cls) = getForegroundApp()

            if (!pkg.isNullOrEmpty() && !cls.isNullOrEmpty()) {
                DataRepository.updateData(pkg, cls)
            }
            handler.postDelayed(this, 500)
        }
    }

    inner class LocalBinder : Binder() {
        val service: PackageMonitoringService get() = this@PackageMonitoringService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        usageStats = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handler.removeCallbacks(observerTask)
        handler.post(observerTask)
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val intent = Intent(this, this::class.java)
        val pendingIntent = PendingIntent.getService(
            applicationContext, 264593, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmService = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmService.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 500,
            pendingIntent
        )
        super.onTaskRemoved(rootIntent)
    }

    private fun getForegroundApp(): Pair<String?, String?> {
        val currentTime = System.currentTimeMillis()
        val usageEvents = usageStats.queryEvents(currentTime - 10000, currentTime)
        var latestPackage: String? = null
        var latestClass: String? = null
        var latestTimestamp = 0L

        val event = UsageEvents.Event()
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED &&
                event.timeStamp > latestTimestamp
            ) {
                latestTimestamp = event.timeStamp
                latestPackage = event.packageName
                latestClass = event.className
            }
        }

        return Pair(latestPackage, latestClass)
    }
}
