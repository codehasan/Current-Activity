package com.willme.topactivity;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

/**
 * Created by Wen on 16/02/2017.
 * Refactored by Ratul on 04/05/2022.
 */
public class WatchingAccessibilityService extends AccessibilityService {
    private static WatchingAccessibilityService sInstance;

    public static WatchingAccessibilityService getInstance() {
        return sInstance;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (SPHelper.isShowWindow(this) && SPHelper.hasAccess(this)) {
            String act1 = event.getClassName().toString();
            String act2 = event.getPackageName().toString();
            
            if (act1 == null || act1.trim().isEmpty())
                return;
            TasksWindow.show(this, act2, act1);
        } else if (!SPHelper.hasAccess(this)) {
            stopSelf();
        }
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    protected void onServiceConnected() {
        sInstance = this;
        if (!getResources().getBoolean(R.bool.use_watching_service)) {
            if (SPHelper.isShowWindow(this)) {
                NotificationActionReceiver.showNotification(this, false);
            }
            sendBroadcast(new Intent(QuickSettingTileService.ACTION_UPDATE_TITLE));
        }
        super.onServiceConnected();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        sInstance = null;
        TasksWindow.dismiss(this);
        NotificationActionReceiver.cancelNotification(this);
        sendBroadcast(new Intent(QuickSettingTileService.ACTION_UPDATE_TITLE));
        return super.onUnbind(intent);
    }
}
