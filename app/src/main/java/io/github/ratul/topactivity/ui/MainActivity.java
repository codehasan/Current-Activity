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

import static android.Manifest.permission.PACKAGE_USAGE_STATS;
import static android.Manifest.permission.POST_NOTIFICATIONS;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.android.volley.Request.Method.GET;
import static java.lang.Integer.parseInt;
import static io.github.ratul.topactivity.utils.AutostartUtil.isAutoStartPermissionAvailable;
import static io.github.ratul.topactivity.utils.AutostartUtil.requestAutoStartPermission;

import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowMetrics;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.divider.MaterialDivider;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import io.github.ratul.topactivity.BuildConfig;
import io.github.ratul.topactivity.R;
import io.github.ratul.topactivity.receivers.NotificationReceiver;
import io.github.ratul.topactivity.services.AccessibilityMonitoringService;
import io.github.ratul.topactivity.services.PackageMonitoringService;
import io.github.ratul.topactivity.utils.DatabaseUtil;
import io.github.ratul.topactivity.utils.WindowUtil;

/**
 * Created by Wen on 16/02/2017.
 * Refactored by Ratul on 04/05/2022.
 */
public class MainActivity extends AppCompatActivity {
    public static final String ACTION_STATE_CHANGED = "io.github.ratul.topactivity.ACTION_STATE_CHANGED";
    public static final String EXTRA_FROM_QS_TILE = "from_qs_tile";
    private ActivityResultLauncher<String> notificationPermissionLauncher;
    private CoordinatorLayout baseView;
    private MaterialSwitch showWindow, showNotification, useAccessibility;
    private BroadcastReceiver updateReceiver;
    private PackageMonitoringService monitoringService;
    private boolean isServiceBound = false;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PackageMonitoringService.LocalBinder binder =
                    (PackageMonitoringService.LocalBinder) service;
            monitoringService = binder.getService();
            isServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceBound = false;
            monitoringService = null;
        }
    };

    class UpdateSwitchReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshWindowSwitch();
            refreshNotificationSwitch();
            refreshAccessibilitySwitch();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        baseView = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(baseView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        checkForUpdate(true);
        startAccessibilityService();
        DatabaseUtil.setDisplayWidth(getScreenWidth());

        boolean isWindowActuallyShowing = WindowUtil.isViewVisible();
        if (DatabaseUtil.isShowingWindow() != isWindowActuallyShowing) {
            DatabaseUtil.setShowingWindow(isWindowActuallyShowing);
        }

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        showWindow = findViewById(R.id.show_window);
        showNotification = findViewById(R.id.show_notification);
        useAccessibility = findViewById(R.id.use_accessibility);
        Button downloadAccessibility = findViewById(R.id.download_accessibility);
        Button configureWidth = findViewById(R.id.configure_width);

        LinearLayout autostartLayout = findViewById(R.id.autostart_layout);
        MaterialDivider autostartDivider = findViewById(R.id.autostart_divider);
        Button allowAutostart = findViewById(R.id.allow_autostart);

        updateReceiver = new UpdateSwitchReceiver();
        ContextCompat.registerReceiver(this, updateReceiver,
                new IntentFilter(ACTION_STATE_CHANGED), ContextCompat.RECEIVER_EXPORTED);

        notificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    DatabaseUtil.setShowNotification(isGranted);
                    showNotification.setChecked(isGranted);
                });

        useAccessibility.setOnCheckedChangeListener((button, isChecked) -> {
            DatabaseUtil.setUseAccessibility(isChecked);
            startAccessibilityService();
        });

        showNotification.setOnCheckedChangeListener((button, isChecked) -> {
            DatabaseUtil.setShowNotification(isChecked);

            if (isChecked && !isNotificationGranted()) {
                requestNotificationPermission();
            }
        });

        showWindow.setOnClickListener(v -> {
            boolean isChecked = showWindow.isChecked();

            if (!isChecked) {
                DatabaseUtil.setShowingWindow(false);
                NotificationReceiver.cancelNotification();
                WindowUtil.dismiss(this);
            }

            if (isSystemOverlayGranted() && isCommonPermissionsGranted()) {
                DatabaseUtil.setShowingWindow(true);
                WindowUtil.show(this, getPackageName(), getClass().getName());
                startAccessibilityService();
                startPackageMonitoringService();
            } else {
                showWindow.setChecked(false);
                requestSystemOverlayPermission();
                requestCommonPermissions();
            }
        });

        downloadAccessibility.setOnClickListener(v -> {
            openLink("https://github.com/codehasan/Current-Activity/releases/tag/v"
                    + BuildConfig.VERSION_NAME);
        });

        configureWidth.setOnClickListener(v -> configureWidth());

        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.github) {
                openLink("https://github.com/codehasan/Current-Activity");
                return true;
            } else if (item.getItemId() == R.id.check_update) {
                showToast("Checking for update");
                checkForUpdate(false);
                return true;
            }
            return false;
        });

        if (isAutoStartPermissionAvailable(this)) {
            autostartLayout.setVisibility(View.VISIBLE);
            autostartDivider.setVisibility(View.VISIBLE);
            allowAutostart.setOnClickListener(v -> requestAutoStartPermission(this));
        } else {
            autostartLayout.setVisibility(View.GONE);
            autostartDivider.setVisibility(View.GONE);
        }

        if (handleQsTileIntent(getIntent())) {
            moveTaskToBack(true);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (handleQsTileIntent(intent)) {
            moveTaskToBack(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startAccessibilityService();
        refreshWindowSwitch();
        refreshNotificationSwitch();
        refreshAccessibilitySwitch();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(updateReceiver);
        super.onDestroy();
    }

    private int getScreenWidth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics windowMetrics = getWindowManager().getCurrentWindowMetrics();
            android.graphics.Insets insets = windowMetrics.getWindowInsets()
                    .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars());
            return windowMetrics.getBounds().width() - insets.left - insets.right;
        } else {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            return displayMetrics.widthPixels;
        }
    }

    private boolean handleQsTileIntent(Intent intent) {
        if (intent.getBooleanExtra(EXTRA_FROM_QS_TILE, false)) {
            showWindow.setChecked(true);
            showWindow.callOnClick();
            return DatabaseUtil.isShowingWindow();
        }
        return false;
    }

    private boolean isCommonPermissionsGranted() {
        return !isAccessibilityNotStarted() && isUsageStatsGranted();
    }

    private boolean isSystemOverlayGranted() {
        return Settings.canDrawOverlays(this);
    }

    private boolean isNotificationGranted() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true;
        }
        return checkSelfPermission(POST_NOTIFICATIONS) == PERMISSION_GRANTED;
    }

    private boolean isUsageStatsGranted() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(), getPackageName());
        if (mode == AppOpsManager.MODE_DEFAULT) {
            return checkCallingOrSelfPermission(PACKAGE_USAGE_STATS) == PERMISSION_GRANTED;
        }
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    @SuppressWarnings("ConstantConditions")
    private boolean isAccessibilityNotStarted() {
        return BuildConfig.FLAVOR.equals("global") &&
                DatabaseUtil.useAccessibility() &&
                AccessibilityMonitoringService.getInstance() == null;
    }

    private void checkForUpdate(boolean silent) {
        try {
            Volley.newRequestQueue(this)
                    .add(getVersionCheckRequest(silent));
        } catch (Throwable ignored) {
            handleErrorResponse(silent);
        }
    }

    private JsonObjectRequest getVersionCheckRequest(boolean silent) {
        JsonObjectRequest request = new JsonObjectRequest(GET,
                "https://api.github.com/repos/codehasan/Current-Activity/releases/latest",
                null,
                response -> {
                    try {
                        processUpdateResponse(response, silent);
                    } catch (Throwable ignored) {
                        handleErrorResponse(silent);
                    }
                },
                error -> handleErrorResponse(silent));
        request.setShouldRetryConnectionErrors(true);
        request.setShouldCache(false);
        return request;
    }

    private void handleErrorResponse(boolean silent) {
        if (!silent) {
            showToast("Failed to check for update");
            openLink("https://github.com/codehasan/Current-Activity/releases");
        }
    }

    private void processUpdateResponse(JSONObject response, boolean silent) throws Throwable {
        String tag = response.getString("tag_name");
        String serverVersion = tag.replaceAll("[^0-9]", "");
        String currentVersion = BuildConfig.VERSION_NAME.replaceAll("[^0-9]", "");

        if (parseInt(serverVersion) > parseInt(currentVersion)) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Update Available")
                    .setMessage("A new version (" + tag + ") is available. Do you want to download it?")
                    .setPositiveButton("Download", (dialog, which) -> {
                        openLink("https://github.com/codehasan/Current-Activity/releases/tag/" + tag);
                        dialog.dismiss();
                    })
                    .setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        } else if (!silent) {
            showToast("Already on the latest version");
        }
    }

    private void configureWidth() {
        View dialogView = getLayoutInflater().inflate(R.layout.content_configure_width, null);
        TextInputLayout widthInput = dialogView.findViewById(R.id.width);
        TextView helperText = dialogView.findViewById(R.id.helper);

        int screenWidth = getScreenWidth();
        int userWidth = DatabaseUtil.getUserWidth();

        if (userWidth != -1) {
            widthInput.getEditText().setText(String.valueOf(userWidth));
        }
        helperText.append("enter a width between 500 and " + screenWidth + ".");

        AlertDialog alertDialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Configure Width")
                .setView(dialogView)
                .setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Save", null)
                .create();

        alertDialog.setOnShowListener(dialog -> {
            Button saveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveButton.setOnClickListener(v -> {
                String input = widthInput.getEditText().getText().toString();

                if (input.trim().isEmpty()) {
                    DatabaseUtil.setUserWidth(-1);
                    dialog.dismiss();
                    showToast("Saved");
                    return;
                }

                int width = parseInt(input);
                if (width < 500) {
                    widthInput.setError("Width should be greater than 500");
                    return;
                } else if (width > screenWidth) {
                    widthInput.setError("Width should be less than screen width (" + screenWidth + ")");
                    return;
                }

                DatabaseUtil.setUserWidth(width);
                dialog.dismiss();
                showToast("Saved");
            });
        });

        alertDialog.show();
    }

    private void startAccessibilityService() {
        // Start Accessibility Monitoring Service if accessibility is enabled
        if (isAccessibilityNotStarted()) {
            Intent intent = new Intent(
                    this, AccessibilityMonitoringService.class);
            getApplicationContext().startService(intent);
        }
    }

    private void startPackageMonitoringService() {
        Intent intent = new Intent(this, PackageMonitoringService.class);
        getApplicationContext().startService(intent);
        getApplicationContext().bindService(
                intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void refreshWindowSwitch() {
        showWindow.setChecked(DatabaseUtil.isShowingWindow());
    }

    private void refreshNotificationSwitch() {
        if (!isNotificationGranted()) {
            DatabaseUtil.setShowNotification(false);
            showNotification.setChecked(false);
            return;
        }
        showNotification.setChecked(DatabaseUtil.isShowNotification());
    }

    private void refreshAccessibilitySwitch() {
        useAccessibility.setChecked(DatabaseUtil.useAccessibility());
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }
        if (checkSelfPermission(POST_NOTIFICATIONS) != PERMISSION_GRANTED) {
            notificationPermissionLauncher.launch(POST_NOTIFICATIONS);
        }
    }

    private void requestSystemOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("System Overlay")
                    .setMessage("Please allow draw over other apps permission for 'Current Activity'")
                    .setPositiveButton("Settings", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                                .setData(Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                        dialog.dismiss();
                    })
                    .setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }

    private void requestCommonPermissions() {
        if (isAccessibilityNotStarted()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Accessibility")
                    .setMessage("Please enable Accessibility Service for 'Current Activity'")
                    .setPositiveButton("Settings", (dialog, button) -> {
                        startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                        dialog.dismiss();
                    })
                    .setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        }

        if (!isUsageStatsGranted()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Usage Access")
                    .setMessage("Please allow Usage Access permission for 'Current Activity'")
                    .setPositiveButton("Settings", (di, btn) -> {
                        startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                        di.dismiss();
                    })
                    .setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }

    private void openLink(String link) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
    }

    private void showToast(String message) {
        Snackbar.make(baseView, message, Snackbar.LENGTH_SHORT).show();
    }
}
