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
import io.github.ratul.topactivity.ui.MainActivity;

public class NotificationMonitor extends BroadcastReceiver {
    public static final int NOTIFICATION_ID = 696969691;
    private static final int ACTION_STOP = 2;
    private static final String EXTRA_NOTIFICATION_ACTION = "command";
    public static Notification.Builder builder;
    public static NotificationManager notifManager;

    public static void showNotification(Context context, boolean isPaused) {
        notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(intent);
        
        PendingIntent pIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String CHANNEL_ID = context.getPackageName() + "_channel_007";
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                CHANNEL_ID,
                "Activity Info",
                NotificationManager.IMPORTANCE_LOW // Lower importance to avoid sound/vibration
            );
            mChannel.setDescription("Shows current activity info");
            mChannel.enableLights(false);
            mChannel.enableVibration(false);
            mChannel.setSound(null, null); // Disable sound
            notifManager.createNotificationChannel(mChannel);

            builder = new Notification.Builder(context, CHANNEL_ID)
                    .setVibrate(null)  // Ensure vibration is null
                    .setSound(null);  // Ensure sound is null
        } else {
            builder = new Notification.Builder(context)
                    .setSound(null)  // Explicitly set sound to null
                    .setVibrate(new long[]{0});  // Disable vibration
        }

        builder.setContentTitle(context.getString(R.string.is_running, context.getString(R.string.app_name)))
                .setSmallIcon(R.drawable.ic_shortcut)
                .setContentText(context.getString(R.string.touch_to_open))
                .setColor(context.getColor(R.color.layerColor))
                .setVisibility(Notification.VISIBILITY_SECRET)
                .setOngoing(!isPaused)
                .setAutoCancel(true)
                .setContentIntent(pIntent);

        builder.addAction(
                R.drawable.ic_launcher_foreground,
                context.getString(R.string.noti_action_stop),
                getPendingIntent(context, ACTION_STOP)
        );

        notifManager.notify(NOTIFICATION_ID, builder.build());
    }

    public static PendingIntent getPendingIntent(Context context, int command) {
        Intent intent = new Intent("io.github.ratul.topactivity.ACTION_NOTIFICATION_RECEIVER");
        intent.putExtra(EXTRA_NOTIFICATION_ACTION, command);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }

    public static void cancelNotification(Context context) {
        if (notifManager != null) {
            notifManager.cancel(NOTIFICATION_ID);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int command = intent.getIntExtra(EXTRA_NOTIFICATION_ACTION, -1);
        if (command == ACTION_STOP) {
            cancelNotification(context);
            context.sendBroadcast(new Intent(MainActivity.ACTION_STATE_CHANGED));
        }
    }
}
