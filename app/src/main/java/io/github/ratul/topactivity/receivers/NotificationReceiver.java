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
package io.github.ratul.topactivity.receivers;

import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static androidx.core.app.NotificationCompat.Action;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import io.github.ratul.topactivity.App;
import io.github.ratul.topactivity.R;
import io.github.ratul.topactivity.ui.MainActivity;
import io.github.ratul.topactivity.utils.DatabaseUtil;
import io.github.ratul.topactivity.managers.PopupManager;

public class NotificationReceiver extends BroadcastReceiver {
    public static final int NOTIFICATION_ID = 62345;
    public static final String CHANNEL_ID = "activity_info";
    public static final int ACTION_COPY = 1;
    public static final int ACTION_STOP = 2;
    public static final String EXTRA_NOTIFICATION_ACTION = "command";

    public static void showNotification(
            @NonNull Context context, @NonNull String title, String message) {
        if (!DatabaseUtil.isShowNotification()) {
            return;
        }
        NotificationManagerCompat notificationManager = App.getInstance().getNotificationManager();
        if (notificationManager.areNotificationsEnabled()) {
            Notification notification = buildNotification(context, title, message);
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    public static void cancelNotification() {
        App.getInstance().getNotificationManager()
                .cancel(NOTIFICATION_ID);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int command = intent.getIntExtra(EXTRA_NOTIFICATION_ACTION, -1);

        switch (command) {
            case ACTION_COPY:
                App.copyString(context,
                        intent.getStringExtra(Intent.EXTRA_TEXT),
                        intent.getStringExtra(Intent.EXTRA_ASSIST_CONTEXT));
                break;
            case ACTION_STOP:
                DatabaseUtil.setShowingWindow(false);
                NotificationReceiver.cancelNotification();
                PopupManager.dismiss(context);
                context.sendBroadcast(new Intent(MainActivity.ACTION_STATE_CHANGED));
                break;
        }
    }

    private static Notification buildNotification(Context context, String pkg, String cls) {
        Action copyPkg = new Action(R.drawable.ic_package, context.getString(R.string.package_label),
                getCopyPendingIntent(context, 3429872, pkg, R.string.package_copied));
        Action copyClass = new Action(R.drawable.ic_class, context.getString(R.string.class_label),
                getCopyPendingIntent(context, 3429873, cls, R.string.class_copied));
        Action stop = new Action(R.drawable.ic_cancel, context.getString(R.string.stop),
                getStopPendingIntent(context));

        return new NotificationCompat.Builder(context.getApplicationContext(), CHANNEL_ID)
                .setContentTitle(pkg)
                .setSmallIcon(R.drawable.ic_logo)
                .setContentText(cls)
                .setAutoCancel(false)
                .setOngoing(true)
                .addAction(copyPkg)
                .addAction(copyClass)
                .addAction(stop)
                .build();
    }

    private static PendingIntent getCopyPendingIntent(
            Context context, int requestCode, String text, @StringRes int message) {
        Intent intent = new Intent(context, NotificationReceiver.class)
                .putExtra(EXTRA_NOTIFICATION_ACTION, ACTION_COPY)
                .putExtra(Intent.EXTRA_TEXT, text)
                .putExtra(Intent.EXTRA_ASSIST_CONTEXT, context.getString(message));
        return PendingIntent.getBroadcast(context,
                requestCode, intent, FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE);
    }

    private static PendingIntent getStopPendingIntent(Context context) {
        Intent intent = new Intent(context, NotificationReceiver.class)
                .putExtra(EXTRA_NOTIFICATION_ACTION, ACTION_STOP);
        return PendingIntent.getBroadcast(context,
                908435, intent, FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE);
    }
}
