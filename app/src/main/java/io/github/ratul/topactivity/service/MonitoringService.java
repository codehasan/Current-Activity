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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;

import io.github.ratul.topactivity.utils.DatabaseUtil;
import io.github.ratul.topactivity.utils.WindowUtil;

/**
 * Created by Wen on 16/02/2017.
 * Refactored by Ratul on 04/05/2022.
 */
public class MonitoringService extends Service {
	public boolean serviceAlive = false;
	private boolean firstRun = true;
	public static MonitoringService INSTANCE;
	private UsageStatsManager usageStats;
	public Handler mHandler = new Handler();
	private String text;
	private String text1;

	@Override
	public void onCreate() {
		super.onCreate();
		INSTANCE = this;

		serviceAlive = true;
		usageStats = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
	}

	@Override
	public void onDestroy() {
		serviceAlive = false;
		super.onDestroy();
	}

	public void getActivityInfo() {
		long currentTimeMillis = System.currentTimeMillis();
		UsageEvents queryEvents = usageStats.queryEvents(currentTimeMillis - (firstRun ? 600000 : 60000),
				currentTimeMillis);
		while (queryEvents.hasNextEvent()) {
			UsageEvents.Event event = new UsageEvents.Event();
			queryEvents.getNextEvent(event);
			int type = event.getEventType();
			if (type == UsageEvents.Event.MOVE_TO_FOREGROUND) {
				text = event.getPackageName();
				text1 = event.getClassName();
			} else if (type == UsageEvents.Event.MOVE_TO_BACKGROUND) {
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
				if (!DatabaseUtil.isShowWindow()) {
					MonitoringService.INSTANCE.mHandler.removeCallbacks(this);
					MonitoringService.INSTANCE.stopSelf();
				}

				String preText = MonitoringService.INSTANCE.text;
				String preText1 = MonitoringService.INSTANCE.text1;
				getActivityInfo();
				if (MonitoringService.INSTANCE.text == null)
					return;
				if (preText != null && preText.equals(MonitoringService.INSTANCE.text)
						&& preText1 != null && preText1.equals(MonitoringService.INSTANCE.text1)) {
					// not change, return
					return;
				}

				MonitoringService.INSTANCE.firstRun = false;
				if (DatabaseUtil.isShowWindow()) {
					WindowUtil.show(MonitoringService.INSTANCE, MonitoringService.INSTANCE.text,
							MonitoringService.INSTANCE.text1);
				} else {
					MonitoringService.INSTANCE.stopSelf();
				}
				mHandler.postDelayed(this, 500);
			}
		};

		mHandler.postDelayed(runner, 500);
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onTaskRemoved(Intent rootIntent) {
		Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
		restartServiceIntent.setPackage(getPackageName());

		PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1,
				restartServiceIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
		AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 500,
				restartServicePendingIntent);

		super.onTaskRemoved(rootIntent);
	}
}
