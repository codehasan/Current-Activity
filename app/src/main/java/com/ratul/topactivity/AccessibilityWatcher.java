package com.ratul.topactivity;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

/**
 * Created by Wen on 16/02/2017.
 * Refactored by Ratul on 04/05/2022.
 */
public class AccessibilityWatcher extends AccessibilityService {
    private static AccessibilityWatcher sInstance;
    
    public static AccessibilityWatcher getInstance() {
        return sInstance;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (WindowUtility.viewAdded && SharedPrefsUtil.isShowWindow(this) && SharedPrefsUtil.hasAccess(this)) {
            String act1 = event.getClassName().toString();
            String act2 = event.getPackageName().toString();
            
            if (act1 == null || act1.trim().isEmpty())
                return;
            WindowUtility.show(this, act2, act1);
        }
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    protected void onServiceConnected() {
        sInstance = this;
        super.onServiceConnected();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        sInstance = null;
        WindowUtility.dismiss(this);
        NotificationMonitor.cancelNotification(this);
        sendBroadcast(new Intent(QuickSettingsService.ACTION_UPDATE_TITLE));
        return super.onUnbind(intent);
    }
}
