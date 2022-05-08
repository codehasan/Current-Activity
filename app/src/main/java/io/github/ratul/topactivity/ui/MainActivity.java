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
package io.github.ratul.topactivity.ui;

import android.app.*;
import android.content.*;
import android.net.Uri;
import android.os.*;
import android.provider.*;
import android.view.*;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.app.*;
import android.content.pm.*;
import android.graphics.drawable.*;
import android.graphics.*;
import android.text.*;
import java.util.List;
import io.github.ratul.topactivity.R;
import io.github.ratul.topactivity.dialog.*;
import io.github.ratul.topactivity.utils.*;
import io.github.ratul.topactivity.model.NotificationMonitor;
import io.github.ratul.topactivity.service.*;
import io.github.ratul.topactivity.model.TypefaceSpan;
import java.io.*;

/**
 * Created by Wen on 16/02/2017.
 * Refactored by Ratul on 04/05/2022.
 */
public class MainActivity extends Activity implements OnCheckedChangeListener {
    public static final String EXTRA_FROM_QS_TILE = "from_qs_tile";
    public static final String ACTION_STATE_CHANGED = "io.github.ratul.topactivity.ACTION_STATE_CHANGED";
    CompoundButton mWindowSwitch, mNotificationSwitch, mAccessibilitySwitch;
    private BroadcastReceiver mReceiver;
    private int theme;
    public static MainActivity INSTANCE;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        INSTANCE = this;
        if (AccessibilityMonitoringService.getInstance() == null && DatabaseUtil.hasAccess())
            startService(new Intent().setClass(this, AccessibilityMonitoringService.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        theme = FancyDialog.DARK_THEME;
        DialogTheme.setupColors(this, theme);
        
        SpannableString s = new SpannableString(getString(R.string.app_name));
        s.setSpan(new TypefaceSpan(this, "fonts/google_sans_bold.ttf"), 0, s.length(),
                  Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(s);

        mWindowSwitch = findViewById(R.id.sw_window);
        mWindowSwitch.setOnCheckedChangeListener(this);
        mNotificationSwitch = findViewById(R.id.sw_notification);
        mNotificationSwitch.setOnCheckedChangeListener(this);
        if (Build.VERSION.SDK_INT < 24) {
            mNotificationSwitch.setVisibility(View.INVISIBLE);
            findViewById(R.id.divider_useNotificationPref).setVisibility(View.INVISIBLE);
        }
        
        mAccessibilitySwitch = findViewById(R.id.sw_accessibility);
        mAccessibilitySwitch.setOnCheckedChangeListener(this);
        if (getIntent().getBooleanExtra(EXTRA_FROM_QS_TILE, false)) {
            mWindowSwitch.setChecked(true);
        }
        NotificationMonitor.cancelNotification(this);
        
        mReceiver = new UpdateSwitchReceiver();
        registerReceiver(mReceiver, new IntentFilter(ACTION_STATE_CHANGED));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (getIntent().getBooleanExtra(EXTRA_FROM_QS_TILE, false)) {
            mWindowSwitch.setChecked(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshWindowSwitch();
        refreshNotificationSwitch();
        refreshAccessibilitySwitch();
        NotificationMonitor.cancelNotification(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (DatabaseUtil.isShowWindow()) {
            NotificationMonitor.showNotification(this, false);
        }
    }

    private void refreshWindowSwitch() {
        mWindowSwitch.setChecked(DatabaseUtil.isShowWindow());
        if (DatabaseUtil.hasAccess() && AccessibilityMonitoringService.getInstance() == null) {
            mWindowSwitch.setChecked(false);
        }
    }

    private void refreshAccessibilitySwitch() {
        mAccessibilitySwitch.setChecked(DatabaseUtil.hasAccess());
    }

    private void refreshNotificationSwitch() {
        mNotificationSwitch.setChecked(!DatabaseUtil.isNotificationToggleEnabled());
    }

    public void showToast(String str, int length) {
        Toast.makeText(this, str, length).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("GitHub Repo").setIcon(R.drawable.ic_github).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        SpannableString s = new SpannableString("About App");
        s.setSpan(new TypefaceSpan(this, "fonts/google_sans_regular.ttf"), 0, s.length(),
                  Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        menu.add(s);
        s = new SpannableString("Crash Log");
        s.setSpan(new TypefaceSpan(this, "fonts/google_sans_regular.ttf"), 0, s.length(),
                  Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        menu.add(s);
        return super.onCreateOptionsMenu(menu);
    }
    
    public String readFile(File file) {
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = br.readLine();
            while (line != null) {
                text.append(line);
                text.append("\n");
                line = br.readLine();
            }
            
            new FileOutputStream(file).write(text.toString().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return text.toString();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final FancyDialog fancy = new FancyDialog(this, theme);
        fancy.setNegativeButton("Close", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fancy.dismiss();
                }
            });
        fancy.setCancelable(false);
        String title = item.getTitle().toString();
        if (title.equals("About App")) {
            fancy.setTitle("About App");
            fancy.setMessage("* Current Activity\nAn useful tool for Android Developers & Reversers, which shows the package name and class name of current activity which you are in.\n\n* Features:\n1. Show current activity info\n2. Copy texts from popup window (Supports android 10 and higher devices as well)\n3. Move the popup window in your screen freely");
            fancy.show();
        } else if (title.equals("Crash Log")) {
            String errorLog = readFile(new File(getFilesDir(), "crash.txt"));
            if (errorLog.isEmpty())
                showToast("No log was found", 0);
            else {
                Intent intent = new Intent(this, CrashActivity.class);
                intent.putExtra(CrashActivity.EXTRA_CRASH_INFO, errorLog);
                intent.putExtra("Restart", false);
                startActivity(intent);
            }
        } else if (title.equals("GitHub Repo")) {
            fancy.setTitle("GitHub Repo");
            fancy.setMessage("It is an open source project. Would you like to visit the official github repo of this app");
            fancy.setPositiveButton("Yes", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fancy.dismiss();
                        startActivity(new Intent().setAction(Intent.ACTION_VIEW).setData(Uri.parse("https://github.com/ratulhasanrahat/Current-Activity")));
                    }
                });
            fancy.show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == mNotificationSwitch) {
            DatabaseUtil.setNotificationToggleEnabled(!isChecked);
            buttonView.setChecked(isChecked);
            return;
        }
        if (buttonView == mAccessibilitySwitch) {
            DatabaseUtil.setHasAccess(isChecked);
            buttonView.setChecked(isChecked);
            if(isChecked && AccessibilityMonitoringService.getInstance() == null)
                startService(new Intent().setClass(this, AccessibilityMonitoringService.class));
            return;
        }
        if (isChecked && buttonView == mWindowSwitch) {
            if (Build.VERSION.SDK_INT >= 24 && !DatabaseUtil.hasBattery() && !((PowerManager) getSystemService("power")).isIgnoringBatteryOptimizations(getPackageName())) {
                setupBattery();
                DatabaseUtil.setHasBattery(true);
                return;
            }
            final FancyDialog fancy = new FancyDialog(MainActivity.this, theme);
            fancy.setCancelable(false);
            fancy.setNegativeButton("Close", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        fancy.dismiss();
                    }
                });
            fancy.getDialog().setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        refreshWindowSwitch();
                        refreshAccessibilitySwitch();
                    }
                });
            if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
                fancy.setTitle("Overlay Permission");
                fancy.setMessage("Please enable overlay permission to show window over other apps");
                fancy.setPositiveButton("Settings", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                            startActivity(intent);
                            fancy.dismiss();
                        }
                    });
                fancy.show();
                return;
            }
            if (DatabaseUtil.hasAccess() && AccessibilityMonitoringService.getInstance() == null) {
                fancy.setTitle("Accessibility Permission");
                fancy.setMessage("As per your choice, please grant permission to use Accessibility Service for Current Activity app in order to get current activity info");
                fancy.setPositiveButton("Settings", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent();
                            intent.setAction("android.settings.ACCESSIBILITY_SETTINGS");
                            startActivity(intent);
                            fancy.dismiss();
                        }
                    });
                fancy.show();
                return;
            }
            if (!usageStats(MainActivity.this)) {
                fancy.setTitle("Usage Access");
                fancy.setMessage("In order to monitor current task, please grant Usage Access permission for Current Activity app");
                fancy.setPositiveButton("Settings", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent();
                            intent.setAction("android.settings.USAGE_ACCESS_SETTINGS");
                            startActivity(intent);
                            fancy.dismiss();
                        }
                    });
                fancy.show();
                return;
            }
        }
        if (buttonView == mWindowSwitch) {
            DatabaseUtil.setAppInitiated(true);
            DatabaseUtil.setIsShowWindow(isChecked);
            if (!isChecked) {
                WindowUtil.dismiss(this);
            } else {
                WindowUtil.show(this, getPackageName(), getClass().getName());
                startService(new Intent(this, MonitoringService.class));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    class UpdateSwitchReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshWindowSwitch();
            refreshNotificationSwitch();
            refreshAccessibilitySwitch();
        }
    }

    public static boolean usageStats(Context context) {
        boolean granted = false;
        AppOpsManager appOps = (AppOpsManager) context.
            getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, 
                                         android.os.Process.myUid(), context.getPackageName());

        if (mode == AppOpsManager.MODE_DEFAULT) {
            granted = (context.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
        } else {
            granted = (mode == AppOpsManager.MODE_ALLOWED);
        }
        return granted;
    }

    public void setupBattery() {
        final FancyDialog fancy = new FancyDialog(this, theme);
        fancy.setTitle("Battery Optimizations");
        fancy.setMessage("Please remove battery optimization/restriction from this app in order to run in background with full functionality");
        fancy.setPositiveButton("Ok", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fancy.dismiss();
                    Intent intent = new Intent();
                    intent.setAction("android.settings.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS");
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                }
            });
        fancy.setNegativeButton("Close", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fancy.dismiss();
                }
            });
        fancy.getDialog().setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    refreshWindowSwitch();
                    refreshAccessibilitySwitch();
                }
            });
        fancy.setCancelable(false);
        fancy.show();

    }
}
