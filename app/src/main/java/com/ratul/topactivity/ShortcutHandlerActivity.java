package com.ratul.topactivity;

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
public class ShortcutHandlerActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (SharedPrefsUtil.hasAccess(this) && AccessibilityWatcher.getInstance() == null)
            startService(new Intent().setClass(this, AccessibilityWatcher.class));
        if (!MainActivity.usageStats(this) || !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(MainActivity.EXTRA_FROM_QS_TILE, true);
            startActivity(intent);
        }

        boolean isShow = !SharedPrefsUtil.isShowWindow(this);
        SharedPrefsUtil.setIsShowWindow(this, isShow);
        if (!isShow) {
            WindowUtility.dismiss(this);
            NotificationMonitor.showNotification(this, true);
        } else {
            String act1 = getClass().getName();
            
            WindowUtility.show(this, getPackageName(), act1);
            NotificationMonitor.showNotification(this, false);
            startService(new Intent(this, MonitoringService.class));
        }
        sendBroadcast(new Intent(MainActivity.ACTION_STATE_CHANGED));
        finish();
    }
}
