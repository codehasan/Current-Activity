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
package io.github.ratul.topactivity.model;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import io.github.ratul.topactivity.R;
import io.github.ratul.topactivity.service.QuickSettingsTileService;
import io.github.ratul.topactivity.ui.MainActivity;
import io.github.ratul.topactivity.utils.DatabaseUtil;
import io.github.ratul.topactivity.utils.WindowUtil;

/**
 * Created by Ratul on 04/05/2022.
 */
public class NotificationMonitor extends BroadcastReceiver {
	public static final int NOTIFICATION_ID = 696969691;
	private static final int ACTION_STOP = 2;
	private static final String EXTRA_NOTIFICATION_ACTION = "command";
	public static Notification.Builder builder;
	public static NotificationManager notifManager;

	public static void showNotification(Context context, boolean isPaused) {
		if (!DatabaseUtil.isNotificationToggleEnabled()) {
			return;
		}
		notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		Intent intent = new Intent(context, MainActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		stackBuilder.addParentStack(MainActivity.class);
		stackBuilder.addNextIntent(intent);
		int flag;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			flag = PendingIntent.FLAG_IMMUTABLE;
		} else {
			flag = PendingIntent.FLAG_UPDATE_CURRENT;
		}
		PendingIntent pIntent = stackBuilder.getPendingIntent(0, flag);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			String CHANNEL_ID = context.getPackageName() + "_channel_007";
			CharSequence name = "Activity Info";

			NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
			mChannel.setDescription("Shows current activity info");
			mChannel.enableLights(false);
			mChannel.enableVibration(false);
			mChannel.setShowBadge(false);
			notifManager.createNotificationChannel(mChannel);

			builder = new Notification.Builder(context, CHANNEL_ID);
		} else {
			builder = new Notification.Builder(context);
		}

		builder.setContentTitle(context.getString(R.string.is_running, context.getString(R.string.app_name)))
			.setSmallIcon(R.drawable.ic_shortcut)
			.setContentText(context.getString(R.string.touch_to_open))
			.setColor(context.getColor(R.color.layerColor))
			.setVisibility(Notification.VISIBILITY_SECRET)
			.setOngoing(!isPaused)
			.setAutoCancel(true)
			.setContentIntent(pIntent);

//		builder.addAction(R.drawable.ic_launcher_foreground, context.getString(R.string.noti_action_stop),
//						  getPendingIntent(context, ACTION_STOP)).setContentIntent(pIntent);

		notifManager.notify(NOTIFICATION_ID, builder.build());
	}

	public static PendingIntent getPendingIntent(Context context, int command) {
		Intent intent = new Intent("io.github.ratul.topactivity.ACTION_NOTIFICATION_RECEIVER");
		intent.putExtra(EXTRA_NOTIFICATION_ACTION, command);
		return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
	}

	public static void cancelNotification(Context context) {
		if (notifManager != null)
			notifManager.cancel(NOTIFICATION_ID);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		int command = intent.getIntExtra(EXTRA_NOTIFICATION_ACTION, -1);
		if (command == ACTION_STOP) {
			WindowUtil.dismiss(context);
			DatabaseUtil.setIsShowWindow(false);
			cancelNotification(context);
			context.sendBroadcast(new Intent(MainActivity.ACTION_STATE_CHANGED));
		}
		context.sendBroadcast(new Intent(QuickSettingsTileService.ACTION_UPDATE_TITLE));
	}
}
