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
package io.github.ratul.topactivity;

import static io.github.ratul.topactivity.receivers.NotificationReceiver.CHANNEL_ID;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.app.NotificationManagerCompat;

import io.github.ratul.topactivity.ui.CopyToClipboardActivity;

public class App extends Application {
    public static final String REPO_URL = "https://github.com/codehasan/Current-Activity";
    public static final String API_URL = "https://api.github.com/repos/codehasan/Current-Activity";
    private static App instance;
    private ClipboardManager clipboardManager;
    private SharedPreferences sharedPreferences;
    private NotificationManagerCompat notificationManager;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        instance = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        sharedPreferences = getSharedPreferences(getPackageName(), 0);
        notificationManager = NotificationManagerCompat.from(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(getString(R.string.notification_channel_description));
            notificationManager.createNotificationChannel(channel);
        }
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public NotificationManagerCompat getNotificationManager() {
        return notificationManager;
    }

    public ClipboardManager getClipboardManager() {
        return clipboardManager;
    }

    public static App getInstance() {
        return instance;
    }

    public static void copyString(Context context, String str, String msg) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            ClipData clip = ClipData.newPlainText(context.getString(R.string.app_name), str);
            getInstance().getClipboardManager().setPrimaryClip(clip);
        } else {
            Intent copyActivity = new Intent(context, CopyToClipboardActivity.class)
                    .putExtra(Intent.EXTRA_TEXT, str)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(copyActivity);
        }
        showToast(context, msg);
    }

    public static void showToast(@NonNull Context context, @StringRes int message) {
        showToast(context, context.getString(message));
    }

    public static void showToast(@NonNull Context context, @NonNull String message) {
        try {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        } catch (Exception ignored) {
            try {
                Toast.makeText(instance, message, Toast.LENGTH_SHORT).show();
            } catch (Exception ignored2) {
            }
        }
    }
}
