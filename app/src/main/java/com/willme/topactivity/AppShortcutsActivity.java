package com.willme.topactivity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;

/**
 * Created by Wen on 16/02/2017.
 * Refactored by Ratul on 04/05/2022.
 */
@TargetApi(Build.VERSION_CODES.N)
public class AppShortcutsActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (SPHelper.hasAccess(this) && WatchingAccessibilityService.getInstance() == null)
            startService(new Intent().setClass(this, WatchingAccessibilityService.class));
        if (!MainActivity.usageStats(this) || !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(MainActivity.EXTRA_FROM_QS_TILE, true);
            startActivity(intent);
        }

        boolean isShow = !SPHelper.isShowWindow(this);
        SPHelper.setIsShowWindow(this, isShow);
        if (!isShow) {
            TasksWindow.dismiss(this);
            NotificationActionReceiver.showNotification(this, true);
        } else {
            String act1 = getClass().getName();
            
            TasksWindow.show(this, getPackageName(), act1);
            NotificationActionReceiver.showNotification(this, false);
            startService(new Intent(this, WatchingService.class));
        }
        sendBroadcast(new Intent(MainActivity.ACTION_STATE_CHANGED));
        finish();
    }
}
