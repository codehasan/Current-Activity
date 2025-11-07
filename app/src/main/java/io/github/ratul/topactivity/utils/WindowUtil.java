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
package io.github.ratul.topactivity.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import io.github.ratul.topactivity.App;
import io.github.ratul.topactivity.R;
import io.github.ratul.topactivity.receivers.NotificationReceiver;
import io.github.ratul.topactivity.services.QuickSettingsTileService;
import io.github.ratul.topactivity.ui.MainActivity;

/**
 * Created by Ratul on 04/05/2022.
 */
public class WindowUtil {
    private static WindowManager.LayoutParams layoutParams;
    private static WindowManager windowManager;
    private static PackageManager packageManager;
    private static View baseView;
    private static int xInitCord = 0;
    private static int yInitCord = 0;
    private static int xInitMargin = 0;
    private static int yInitMargin = 0;
    private static TextView appName, packageName, className;

    public static void show(
            @NonNull Context context, @NonNull String pkg, @NonNull String cls) {
        if (windowManager == null || baseView == null) {
            init(context.getApplicationContext());
        }

        if (!isViewVisible()) {
            int userWidth = DatabaseUtil.getUserWidth();
            if (userWidth != -1) {
                layoutParams.width = userWidth;
            } else {
                double displaySize = Math.min(1100, DatabaseUtil.getDisplayWidth());
                layoutParams.width = (int) (displaySize * 0.65);
            }
            windowManager.addView(baseView, layoutParams);
            QuickSettingsTileService.updateTile(context);
        }

        boolean isPackageChanged = !packageName.getText().toString().equals(pkg);
        boolean isClassChanged = !className.getText().toString().equals(cls);

        if (isPackageChanged) {
            appName.setText(getAppName(pkg));
            packageName.setText(pkg);
        }

        if (isClassChanged) {
            className.setText(cls);
        }

        if (isPackageChanged || isClassChanged) {
            NotificationReceiver.showNotification(context, pkg, cls);
        }
    }

    public static void dismiss(@NonNull Context context) {
        if (windowManager != null) {
            windowManager.removeView(baseView);
        }
        QuickSettingsTileService.updateTile(context);
    }

    public static boolean isViewVisible() {
        return baseView != null && baseView.isAttachedToWindow();
    }

    @SuppressLint("ClickableViewAccessibility")
    private static void init(@NonNull Context context) {
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        packageManager = context.getPackageManager();

        layoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);

        layoutParams.gravity = Gravity.CENTER;
        layoutParams.windowAnimations = android.R.style.Animation_Toast;

        baseView = LayoutInflater.from(context).inflate(R.layout.content_activity_info, null);
        appName = baseView.findViewById(R.id.app_name);
        packageName = baseView.findViewById(R.id.package_name);
        className = baseView.findViewById(R.id.class_name);
        ImageView closeBtn = baseView.findViewById(R.id.closeBtn);

        View.OnLongClickListener copyListener = v -> {
            TextView textView = (TextView) v;
            String label = "";

            if (v.getId() == R.id.app_name) label = "App name";
            else if (v.getId() == R.id.package_name) label = "Package";
            else if (v.getId() == R.id.class_name) label = "Class";

            App.copyString(context, textView.getText().toString(), label + " copied");
            return true;
        };

        closeBtn.setOnClickListener(v -> {
            DatabaseUtil.setShowingWindow(false);
            NotificationReceiver.cancelNotification();
            dismiss(context);
            context.sendBroadcast(new Intent(MainActivity.ACTION_STATE_CHANGED));
        });

        packageName.setOnLongClickListener(copyListener);
        className.setOnLongClickListener(copyListener);

        baseView.setOnTouchListener((view, event) -> {
            int xCord = (int) event.getRawX();
            int yCord = (int) event.getRawY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    xInitCord = xCord;
                    yInitCord = yCord;
                    xInitMargin = layoutParams.x;
                    yInitMargin = layoutParams.y;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    int xDiffMove = xCord - xInitCord;
                    int yDiffMove = yCord - yInitCord;
                    layoutParams.x = xInitMargin + xDiffMove;
                    layoutParams.y = yInitMargin + yDiffMove;
                    windowManager.updateViewLayout(view, layoutParams);
                    return true;
            }
            return false;
        });
    }

    private static String getAppName(@NonNull String pkg) {
        try {
            return packageManager.getApplicationLabel(
                    packageManager.getApplicationInfo(pkg, 0)).toString();
        } catch (PackageManager.NameNotFoundException ignored) {
            return "Unknown";
        }
    }
}
