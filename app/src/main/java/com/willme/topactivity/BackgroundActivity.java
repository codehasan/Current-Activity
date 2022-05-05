package com.willme.topactivity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.content.ClipboardManager;
import android.content.ClipData;

/**
 * Created by Ratul on 04/05/2022.
 */
 @TargetApi(29)
public class BackgroundActivity extends Activity {
    public static String STRING_COPY = "com.willme.topactivity.COPY_STRING";
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String str = getIntent().getStringExtra(STRING_COPY);
        
        if (str != null) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = new ClipData(ClipData.newPlainText("", str));
            clipboard.setPrimaryClip(clip);
        }
        
        if (SPHelper.hasAccess(this) && WatchingAccessibilityService.getInstance() == null)
            startService(new Intent().setClass(this, WatchingAccessibilityService.class));
        finish();
    }
}
