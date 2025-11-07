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

import static io.github.ratul.topactivity.utils.NullSafety.isNullOrEmpty;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.util.Pair;

import io.github.ratul.topactivity.BuildConfig;
import io.github.ratul.topactivity.utils.DatabaseUtil;
import io.github.ratul.topactivity.utils.WindowUtil;

/**
 * Created by Wen on 16/02/2017.
 * Refactored by Ratul on 04/05/2022.
 */
public class PackageMonitoringService extends Service {
    private final IBinder binder = new LocalBinder();
    private final Handler handler = new Handler();
    private Runnable observerTask;
    private UsageStatsManager usageStats;

    public class LocalBinder extends Binder {
        public PackageMonitoringService getService() {
            return PackageMonitoringService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        usageStats = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        observerTask = () -> {
            if (!DatabaseUtil.isShowingWindow()) {
                handler.removeCallbacks(observerTask);
                stopSelf();
                return;
            }

            Pair<String, String> currentApp = getForegroundApp();
            String currentPkgName = currentApp.first;
            String currentClassName = currentApp.second;

            if (!isNullOrEmpty(currentPkgName) && !isNullOrEmpty(currentClassName)) {
                if (BuildConfig.DEBUG) {
                    Log.d("PackageMonitoring", "Pkg: " + currentPkgName + ", Class: " + currentClassName);
                }
                if (WindowUtil.isViewVisible()) {
                    WindowUtil.show(this, currentPkgName, currentClassName);
                }
            }
            handler.postDelayed(observerTask, 500);
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.removeCallbacks(observerTask);
        handler.post(observerTask);
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent intent = new Intent(this, getClass());
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(),
                264593, intent, FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE);
        AlarmManager alarmService = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 500, pendingIntent);
        super.onTaskRemoved(rootIntent);
    }

    private Pair<String, String> getForegroundApp() {
        long currentTime = System.currentTimeMillis();
        // Query events in the last 10 seconds
        UsageEvents usageEvents = usageStats.queryEvents(currentTime - 10000, currentTime);
        String latestPackage = null;
        String latestClass = null;
        long latestTimestamp = 0;

        UsageEvents.Event event = new UsageEvents.Event();
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event);

            if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                if (event.getTimeStamp() > latestTimestamp) {
                    latestTimestamp = event.getTimeStamp();
                    latestPackage = event.getPackageName();
                    latestClass = event.getClassName();
                }
            }
        }

        return new Pair<>(latestPackage, latestClass);
    }
}
