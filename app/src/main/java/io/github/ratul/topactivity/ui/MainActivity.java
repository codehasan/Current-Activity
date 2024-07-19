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

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Insets;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowMetrics;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

import io.github.ratul.topactivity.App;
import io.github.ratul.topactivity.R;
import io.github.ratul.topactivity.model.NotificationMonitor;
import io.github.ratul.topactivity.model.TypefaceSpan;
import io.github.ratul.topactivity.service.AccessibilityMonitoringService;
import io.github.ratul.topactivity.service.MonitoringService;
import io.github.ratul.topactivity.utils.DatabaseUtil;
import io.github.ratul.topactivity.utils.WindowUtil;

/**
 * Created by Wen on 16/02/2017.
 * Refactored by Ratul on 04/05/2022.
 */
public class MainActivity extends AppCompatActivity {
	public static final int REQUEST_CODE_NOTIFICATION = 100;
	public static final String EXTRA_FROM_QS_TILE = "from_qs_tile";
	public static final String ACTION_STATE_CHANGED = "io.github.ratul.topactivity.ACTION_STATE_CHANGED";
	private SwitchMaterial mWindowSwitch, mNotificationSwitch, mAccessibilitySwitch;
	private BroadcastReceiver mReceiver;
	private MaterialAlertDialogBuilder fancy;
	public static MainActivity INSTANCE;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		INSTANCE = this;
		if (AccessibilityMonitoringService.getInstance() == null && DatabaseUtil.hasAccess())
			startService(new Intent().setClass(this, AccessibilityMonitoringService.class));

		DatabaseUtil.setDisplayWidth(getScreenWidth(this));
		fancy = new MaterialAlertDialogBuilder(this).setNegativeButton("Close", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface di, int btn) {
				di.dismiss();
			}
		}).setCancelable(false);

		SpannableString s = new SpannableString(getString(R.string.app_name));
		s.setSpan(new TypefaceSpan(this, "fonts/google_sans_bold.ttf"), 0, s.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle(s);

		mWindowSwitch = findViewById(R.id.sw_window);
		mNotificationSwitch = findViewById(R.id.sw_notification);
		mAccessibilitySwitch = findViewById(R.id.sw_accessibility);

		if (Build.VERSION.SDK_INT < 24) {
			mNotificationSwitch.setVisibility(View.INVISIBLE);
			findViewById(R.id.divider_useNotificationPref).setVisibility(View.INVISIBLE);
		}

		mReceiver = new UpdateSwitchReceiver();
		ContextCompat.registerReceiver(this, mReceiver, new IntentFilter(ACTION_STATE_CHANGED), ContextCompat.RECEIVER_NOT_EXPORTED);

		mNotificationSwitch.setOnCheckedChangeListener(new SwitchMaterial.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton button, boolean isChecked) {
				DatabaseUtil.setNotificationToggleEnabled(!isChecked);
			}
		});
		mAccessibilitySwitch.setOnCheckedChangeListener(new SwitchMaterial.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton button, boolean isChecked) {
				DatabaseUtil.setHasAccess(isChecked);
				if (isChecked && AccessibilityMonitoringService.getInstance() == null)
					startService(new Intent().setClass(MainActivity.this, AccessibilityMonitoringService.class));
			}
		});
		mWindowSwitch.setOnCheckedChangeListener(new SwitchMaterial.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton button, boolean isChecked) {
				if (!Settings.canDrawOverlays(MainActivity.this)) {
					fancy.setTitle("Overlay Permission")
							.setMessage("Please enable overlay permission to show window over other apps")
							.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface di, int btn) {
									Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
									intent.setData(Uri.parse("package:" + getPackageName()));
									startActivity(intent);
									di.dismiss();
								}
							}).show();
					mWindowSwitch.setChecked(false);
				} else if (DatabaseUtil.hasAccess() && AccessibilityMonitoringService.getInstance() == null) {
					fancy.setTitle("Accessibility Permission").setMessage(
							"As per your choice, please grant permission to use Accessibility Service for Current Activity app in order to get current activity info")
							.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface di, int btn) {
									Intent intent = new Intent();
									intent.setAction("android.settings.ACCESSIBILITY_SETTINGS");
									startActivity(intent);
									di.dismiss();
								}
							}).show();
					mWindowSwitch.setChecked(false);
				} else if (!usageStats(MainActivity.this)) {
					fancy.setTitle("Usage Access").setMessage(
							"In order to monitor current task, please grant Usage Access permission for Current Activity app")
							.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface di, int btn) {
									Intent intent = new Intent();
									intent.setAction("android.settings.USAGE_ACCESS_SETTINGS");
									startActivity(intent);
									di.dismiss();
								}
							}).show();
					mWindowSwitch.setChecked(false);
				} else {
					DatabaseUtil.setAppInitiated(true);
					DatabaseUtil.setIsShowWindow(isChecked);
					if (!isChecked) {
						WindowUtil.dismiss(MainActivity.this);
					} else {
						WindowUtil.show(MainActivity.this, getPackageName(), getClass().getName());
						startService(new Intent(MainActivity.this, MonitoringService.class));
					}
				}
			}
		});

		if (getIntent().getBooleanExtra(EXTRA_FROM_QS_TILE, false))
			mWindowSwitch.setChecked(true);
	}

	public static int getScreenWidth(Activity activity) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			WindowMetrics windowMetrics = activity.getWindowManager().getCurrentWindowMetrics();
			Insets insets = windowMetrics.getWindowInsets().getInsetsIgnoringVisibility(WindowInsets.Type.systemBars());
			return windowMetrics.getBounds().width() - insets.left - insets.right;
		} else {
			DisplayMetrics displayMetrics = new DisplayMetrics();
			activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
			return displayMetrics.widthPixels;
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (getIntent().getBooleanExtra(EXTRA_FROM_QS_TILE, false)) {
			mWindowSwitch.setChecked(true);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !mNotificationSwitch.isChecked()) {
			if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
				requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_NOTIFICATION);
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == REQUEST_CODE_NOTIFICATION) {
			if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
						&& shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
					requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_NOTIFICATION);
				} else {
					showToast("POST_NOTIFICATIONS Permission Denied", Toast.LENGTH_SHORT);
				}
			} else {
				showToast("POST_NOTIFICATIONS Permission Granted", Toast.LENGTH_SHORT);
			}
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
		SpannableString span = new SpannableString("About App");
		span.setSpan(new TypefaceSpan(this, "fonts/google_sans_regular.ttf"), 0, span.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		menu.add(span);
		span = new SpannableString("Crash Log");
		span.setSpan(new TypefaceSpan(this, "fonts/google_sans_regular.ttf"), 0, span.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		menu.add(span);
		span = new SpannableString("Bug Report");
		span.setSpan(new TypefaceSpan(this, "fonts/google_sans_regular.ttf"), 0, span.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		menu.add(span);
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
		String title = item.getTitle().toString();
		if (title.equals("About App")) {
			fancy.setTitle("About App").setMessage(
					"An useful open source tool for Android Developers, which shows the package name and class name of current activity\n\nHere are the main features of this app!\n● It provides a freely moveable popup window to view current activity info\n● It supports text copying from popup window\n● It supports quick settings and app shortcut for easy access to the popup window. Meaning you can get the popup window in your screen from anywhere")
					.show();
		} else if (title.equals("Crash Log")) {
			String errorLog = readFile(new File(App.getCrashLogDir(), "crash.txt"));
			if (errorLog.isEmpty())
				showToast("No log was found", 0);
			else {
				Intent intent = new Intent(this, CrashActivity.class);
				intent.putExtra(CrashActivity.EXTRA_CRASH_INFO, errorLog);
				intent.putExtra("Restart", false);
				startActivity(intent);
			}
		} else if (title.equals("GitHub Repo")) {
			fancy.setTitle("GitHub Repo").setMessage(
					"It is an open source project. Would you like to visit the official github repo of this app")
					.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface di, int btn) {
							di.dismiss();
							startActivity(new Intent().setAction(Intent.ACTION_VIEW)
									.setData(Uri.parse("https://github.com/ratulhasanrahat/Current-Activity")));
						}
					}).show();
		} else if (title.equals("Bug Report")) {
			fancy.setTitle("Bug Report").setMessage(
					"If you found a bug while using this app, please take a screenshot of it if possible. If it's a crash then you can find the crash log in this directory: "
							+ new File(App.getCrashLogDir(), "crash.txt").getAbsolutePath()
							+ "\n\nAfter you get all necessary things related to the bug, open an issue in github repo of this app with your bug report details")
					.setPositiveButton("Create", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface di, int btn) {
							di.dismiss();
							startActivity(new Intent().setAction(Intent.ACTION_VIEW).setData(
									Uri.parse("https://github.com/ratulhasanrahat/Current-Activity/issues/new")));
						}
					}).show();
		}
		return super.onOptionsItemSelected(item);
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

	public void setupBattery() {
		fancy.setTitle("Battery Optimizations").setMessage(
				"Please remove battery optimization/restriction from this app in order to run in background with full functionality")
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface di, int btn) {
						di.dismiss();
						Intent intent = new Intent();
						intent.setAction("android.settings.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS");
						intent.setData(Uri.parse("package:" + getPackageName()));
						startActivity(intent);
					}
				}).show();

	}
}
