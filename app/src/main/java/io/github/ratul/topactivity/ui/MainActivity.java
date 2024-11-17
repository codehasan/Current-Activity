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
import androidx.appcompat.app.ActionBar;
import android.content.pm.*;
import android.graphics.drawable.*;
import android.graphics.*;
import android.text.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.util.List;
import io.github.ratul.topactivity.*;
import io.github.ratul.topactivity.utils.*;
import io.github.ratul.topactivity.model.NotificationMonitor;
import io.github.ratul.topactivity.service.*;
import io.github.ratul.topactivity.model.TypefaceSpan;
import java.io.*;
import android.util.DisplayMetrics;

/**
 * Created by Wen on 16/02/2017.
 * Refactored by Ratul on 04/05/2022.
 */
public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_FROM_QS_TILE = "from_qs_tile";
    public static final String ACTION_STATE_CHANGED = "io.github.ratul.topactivity.ACTION_STATE_CHANGED";

    private SwitchMaterial mWindowSwitch, mNotificationSwitch, mAccessibilitySwitch;
    private BroadcastReceiver mReceiver;
    private MaterialAlertDialogBuilder dialogBuilder;

    public static MainActivity INSTANCE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        INSTANCE = this;

        initServices();
        initViews();
        setupActionBar();
        registerReceiver();
        handleQuickSettingsTile();

        DatabaseUtil.setDisplayWidth(getScreenWidth(this));
    }

    private void initServices() {
        if (AccessibilityMonitoringService.getInstance() == null && DatabaseUtil.hasAccess()) {
            startService(new Intent(this, AccessibilityMonitoringService.class));
        }
    }

    private void initViews() {
        mWindowSwitch = findViewById(R.id.sw_window);
        mNotificationSwitch = findViewById(R.id.sw_notification);
        mAccessibilitySwitch = findViewById(R.id.sw_accessibility);

        dialogBuilder = new MaterialAlertDialogBuilder(this)
                .setNegativeButton("Close", (dialog, which) -> dialog.dismiss())
                .setCancelable(false);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mNotificationSwitch.setVisibility(View.INVISIBLE);
            findViewById(R.id.divider_useNotificationPref).setVisibility(View.INVISIBLE);
        }

        setupSwitchListeners();
    }

    private void setupActionBar() {
        SpannableString title = new SpannableString(getString(R.string.app_name));
        title.setSpan(new TypefaceSpan(this, "fonts/google_sans_bold.ttf"), 0, title.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setTitle(title);
    }

    private void registerReceiver() {
        mReceiver = new UpdateSwitchReceiver();
        registerReceiver(mReceiver, new IntentFilter(ACTION_STATE_CHANGED));
    }

    private void handleQuickSettingsTile() {
        if (getIntent().getBooleanExtra(EXTRA_FROM_QS_TILE, false)) {
            mWindowSwitch.setChecked(true);
        }
    }

    private void setupSwitchListeners() {
        mNotificationSwitch.setOnCheckedChangeListener((button, isChecked) -> 
            DatabaseUtil.setNotificationToggleEnabled(!isChecked));

        mAccessibilitySwitch.setOnCheckedChangeListener((button, isChecked) -> {
            DatabaseUtil.setHasAccess(isChecked);
            if (isChecked && AccessibilityMonitoringService.getInstance() == null) {
                startService(new Intent(this, AccessibilityMonitoringService.class));
            }
        });

        mWindowSwitch.setOnCheckedChangeListener((button, isChecked) -> handleWindowSwitch(isChecked));
    }

    private void handleWindowSwitch(boolean isChecked) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            showPermissionDialog("Overlay Permission",
                    "Please enable overlay permission to show window over other apps",
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            mWindowSwitch.setChecked(false);
        } else if (requiresAdditionalPermissions()) {
            mWindowSwitch.setChecked(false);
        } else {
            toggleWindowService(isChecked);
        }
    }

    private boolean requiresAdditionalPermissions() {
        if (DatabaseUtil.hasAccess() && AccessibilityMonitoringService.getInstance() == null) {
            showPermissionDialog("Accessibility Permission",
                    "Please grant Accessibility Service permission to get current activity info",
                    Settings.ACTION_ACCESSIBILITY_SETTINGS);
            return true;
        } else if (!hasUsageStatsPermission(this)) {
            showPermissionDialog("Usage Access",
                    "Grant Usage Access permission to monitor the current task",
                    Settings.ACTION_USAGE_ACCESS_SETTINGS);
            return true;
        }
        return false;
    }

    private void toggleWindowService(boolean isChecked) {
        DatabaseUtil.setAppInitiated(true);
        DatabaseUtil.setIsShowWindow(isChecked);

        if (!isChecked) {
            WindowUtil.dismiss(this);
        } else {
            WindowUtil.show(this, getPackageName(), getClass().getName());
            startService(new Intent(this, MonitoringService.class));
        }
    }

    private void showPermissionDialog(String title, String message, String action) {
        dialogBuilder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("Settings", (dialog, which) -> {
                    Intent intent = new Intent(action);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                })
                .show();
    }

    public static int getScreenWidth(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics metrics = activity.getWindowManager().getCurrentWindowMetrics();
            Insets insets = metrics.getWindowInsets().getInsetsIgnoringVisibility(WindowInsets.Type.systemBars());
            return metrics.getBounds().width() - insets.left - insets.right;
        } else {
            DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            return metrics.widthPixels;
        }
    }
    
    public static boolean usageStats(Context context) {
        boolean granted = false;
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(),
            context.getPackageName());

        if (mode == AppOpsManager.MODE_DEFAULT) {
          granted = (context.checkCallingOrSelfPermission(
                  android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
        } else {
        granted = (mode == AppOpsManager.MODE_ALLOWED);
        }
            return granted;
    }


    public static boolean hasUsageStatsPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(),
                context.getPackageName());

        return mode == AppOpsManager.MODE_ALLOWED ||
                (mode == AppOpsManager.MODE_DEFAULT && context.checkCallingOrSelfPermission(
                        android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshSwitchStates();
        NotificationMonitor.cancelNotification(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (DatabaseUtil.isShowWindow()) {
            NotificationMonitor.showNotification(this, false);
        }
    }

    private void refreshSwitchStates() {
        refreshSwitch(mWindowSwitch, DatabaseUtil.isShowWindow());
        refreshSwitch(mAccessibilitySwitch, DatabaseUtil.hasAccess());
        refreshSwitch(mNotificationSwitch, !DatabaseUtil.isNotificationToggleEnabled());
    }

    private void refreshSwitch(SwitchMaterial switchMaterial, boolean state) {
        switchMaterial.setChecked(state);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    class UpdateSwitchReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshSwitchStates();
        }
    }
}
