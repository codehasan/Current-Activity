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
package com.ratul.topactivity;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import java.util.List;
import javax.crypto.NullCipher;

/**
 * Created by Wen on 4/18/15.
 * Refactored by Ratul on 04/05/2022.
 */
public class NotificationMonitor extends BroadcastReceiver {
    public static final int NOTIFICATION_ID = 1;
    public static final String ACTION_NOTIFICATION_RECEIVER = "com.ratul.topactivity.ACTION_NOTIFICATION_RECEIVER";
    public static final int ACTION_STOP = 2;
    public static final String EXTRA_NOTIFICATION_ACTION = "command";
    public static NotificationCompat.Builder builder;
    public static NotificationManager notifManager;

    public static void showNotification(Context context, boolean isPaused) {
        if (!SharedPrefsUtil.isNotificationToggleEnabled(context)) {
            return;
        }
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);
        builder = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.is_running,
                        context.getString(R.string.app_name)))
                .setSmallIcon(R.drawable.ic_shortcut)
                .setContentText(context.getString(R.string.touch_to_open))
                .setColor(context.getColor(R.color.layerColor))
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .setOngoing(!isPaused);
                
        builder.addAction(R.drawable.ic_shortcut,
                context.getString(R.string.noti_action_stop),
                getPendingIntent(context, ACTION_STOP))
                .setContentIntent(pIntent);

        notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.notify(NOTIFICATION_ID, builder.build());
    }

    public static PendingIntent getPendingIntent(Context context, int command) {
        Intent intent = new Intent(ACTION_NOTIFICATION_RECEIVER);
        intent.putExtra(EXTRA_NOTIFICATION_ACTION, command);
        return PendingIntent.getBroadcast(context, command, intent, 0);
    }

    public static void cancelNotification(Context context) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(NOTIFICATION_ID);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int command = intent.getIntExtra(EXTRA_NOTIFICATION_ACTION, -1);
        switch (command) {
            case ACTION_STOP:
                WindowUtility.dismiss(context);
                SharedPrefsUtil.setIsShowWindow(context, false);
                cancelNotification(context);
                context.sendBroadcast(new Intent(MainActivity.ACTION_STATE_CHANGED));
                break;
        }
        context.sendBroadcast(new Intent(QuickSettingsService.ACTION_UPDATE_TITLE));
    }
}
