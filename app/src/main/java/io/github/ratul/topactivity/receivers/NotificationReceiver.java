package io.github.ratul.topactivity.receivers;

import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import io.github.ratul.topactivity.App;
import io.github.ratul.topactivity.R;
import io.github.ratul.topactivity.ui.MainActivity;
import io.github.ratul.topactivity.utils.DatabaseUtil;
import io.github.ratul.topactivity.utils.WindowUtil;

public class NotificationReceiver extends BroadcastReceiver {
    public static final int NOTIFICATION_ID = 62345;
    public static final String CHANNEL_ID = "activity_info";
    public static final int ACTION_COPY = 1;
    public static final int ACTION_STOP = 2;
    public static final String EXTRA_NOTIFICATION_ACTION = "command";

    public static void createNotificationChannel(@NonNull NotificationManagerCompat notificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Activity Info", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Shows current activity info");
            notificationManager.createNotificationChannel(channel);
        }
    }

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
                WindowUtil.dismiss(context);
                context.sendBroadcast(new Intent(MainActivity.ACTION_STATE_CHANGED));
                break;
        }
    }

    private static Notification buildNotification(Context context, String pkg, String cls) {
        NotificationCompat.Action copyPkg = new NotificationCompat.Action(R.drawable.ic_package, "Package",
                getCopyPendingIntent(context, 3429872, pkg, "Package copied"));
        NotificationCompat.Action copyClass = new NotificationCompat.Action(R.drawable.ic_class, "Class",
                getCopyPendingIntent(context, 3429873, cls, "Class copied"));
        NotificationCompat.Action stop = new NotificationCompat.Action(
                R.drawable.ic_cancel, "Stop", getStopPendingIntent(context));

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
            Context context, int requestCode, String text, String message) {
        Intent intent = new Intent(context, NotificationReceiver.class)
                .putExtra(EXTRA_NOTIFICATION_ACTION, ACTION_COPY)
                .putExtra(Intent.EXTRA_TEXT, text)
                .putExtra(Intent.EXTRA_ASSIST_CONTEXT, message);
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
