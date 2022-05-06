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
package com.willme.topactivity;

import android.annotation.*;
import android.app.*;
import android.app.ActivityManager.*;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import android.util.*;

import java.util.*;
import android.app.usage.*;
import android.widget.Toast;

/**
 * Created by Wen on 16/02/2017.
 * Refactored by Ratul on 04/05/2022.
 */
public class WatchingService extends Service {
    public static boolean serviceAlive = false;
    private boolean firstRun = true;
    public static WatchingService INSTANCE;
    private UsageStatsManager usageStats;
    public static Handler mHandler = new Handler();
    private ActivityManager mActivityManager;
    private String text;
    private String text1;

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;

        serviceAlive = true;
        mActivityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= 21) {
            usageStats = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        }
    }

    @Override
    public void onDestroy() {
        serviceAlive = false;
        super.onDestroy();
    }

    public void getActivityInfo() {
        if (Build.VERSION.SDK_INT < 21) {
            List<ActivityManager.RunningTaskInfo> runningTasks = mActivityManager.getRunningTasks(1);
            text = runningTasks.get(0).topActivity.getPackageName();
            text1 = runningTasks.get(0).topActivity.getClassName();
            return;
        }
        long currentTimeMillis = System.currentTimeMillis();
        UsageEvents queryEvents = usageStats.queryEvents(currentTimeMillis - (firstRun ? 600000 : 60000), currentTimeMillis);
        while (queryEvents.hasNextEvent()) {
            UsageEvents.Event event = new UsageEvents.Event();
            queryEvents.getNextEvent(event);
            switch (event.getEventType()) {
                case UsageEvents.Event.MOVE_TO_FOREGROUND:
                    text = event.getPackageName();
                    text1 = event.getClassName();
                    break;
                case UsageEvents.Event.MOVE_TO_BACKGROUND:
                    if (event.getPackageName().equals(text)) {
                        text = null;
                        text1 = null;
                    }
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        INSTANCE = this;
        Runnable runner = new Runnable() {
            @Override
            public void run() {
                if (!SPHelper.isShowWindow(WatchingService.INSTANCE)) {
                    WatchingService.mHandler.removeCallbacks(this);
                    WatchingService.INSTANCE.stopSelf();
                }

                getActivityInfo();
                if (WatchingService.INSTANCE.text == null)
                    return;

                WatchingService.INSTANCE.firstRun = false;
                if (SPHelper.isShowWindow(WatchingService.INSTANCE)) {
                    WatchingService.mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                TasksWindow.show(WatchingService.INSTANCE, WatchingService.INSTANCE.text, WatchingService.INSTANCE.text1);
                            }
                        });
                } else {
                    WatchingService.INSTANCE.stopSelf();
                }
                mHandler.postDelayed(this, 500);
            }
        };
        
        mHandler.postDelayed(runner, 500);
        return super.onStartCommand(intent, flags, startId);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceIntent = new Intent(getApplicationContext(),
                                                 this.getClass());
        restartServiceIntent.setPackage(getPackageName());

        PendingIntent restartServicePendingIntent = PendingIntent.getService(
            getApplicationContext(), 1, restartServiceIntent,
            PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext()
            .getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME,
                         SystemClock.elapsedRealtime() + 500,
                         restartServicePendingIntent);

        super.onTaskRemoved(rootIntent);
    }
}
