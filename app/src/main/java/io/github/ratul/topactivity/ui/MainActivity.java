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
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.util.List;
import io.github.ratul.topactivity.*;
import io.github.ratul.topactivity.dialog.*;
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
	private FancyDialog fancy;
	public static MainActivity INSTANCE;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		INSTANCE = this;
		if (AccessibilityMonitoringService.getInstance() == null && DatabaseUtil.hasAccess())
			startService(new Intent().setClass(this, AccessibilityMonitoringService.class));

		DatabaseUtil.setDisplayWidth(getScreenWidth(this));
		fancy = new FancyDialog(this, FancyDialog.DARK_THEME);
		fancy.setNegativeButton("Close", new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				fancy.dismiss();
			}
		});
		fancy.setCancelable(false);

		SpannableString s = new SpannableString(getString(R.string.app_name));
		s.setSpan(new TypefaceSpan(this, "fonts/google_sans_bold.ttf"), 0, s.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		ActionBar actionBar = getActionBar();
		actionBar.setTitle(s);

		mWindowSwitch = findViewById(R.id.sw_window);
		mNotificationSwitch = findViewById(R.id.sw_notification);
		mAccessibilitySwitch = findViewById(R.id.sw_accessibility);

		if (Build.VERSION.SDK_INT < 24) {
			mNotificationSwitch.setVisibility(View.INVISIBLE);
			findViewById(R.id.divider_useNotificationPref).setVisibility(View.INVISIBLE);
		}

		mReceiver = new UpdateSwitchReceiver();
		registerReceiver(mReceiver, new IntentFilter(ACTION_STATE_CHANGED));

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
				if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(MainActivity.this)) {
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
					mWindowSwitch.setChecked(false);
				} else if (DatabaseUtil.hasAccess() && AccessibilityMonitoringService.getInstance() == null) {
					fancy.setTitle("Accessibility Permission");
					fancy.setMessage(
							"As per your choice, please grant permission to use Accessibility Service for Current Activity app in order to get current activity info");
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
					mWindowSwitch.setChecked(false);
				} else if (!usageStats(MainActivity.this)) {
					fancy.setTitle("Usage Access");
					fancy.setMessage(
							"In order to monitor current task, please grant Usage Access permission for Current Activity app");
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
			fancy.setTitle("About App");
			fancy.setMessage(
					"An useful open source tool for Android Developers, which shows the package name and class name of current activity\n\nHere are the main features of this app!\n● It provides a freely moveable popup window to view current activity info\n● It supports text copying from popup window\n● It supports quick settings and app shortcut for easy access to the popup window. Meaning you can get the popup window in your screen from anywhere");
			fancy.show();
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
			fancy.setTitle("GitHub Repo");
			fancy.setMessage(
					"It is an open source project. Would you like to visit the official github repo of this app");
			fancy.setPositiveButton("Yes", new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					fancy.dismiss();
					startActivity(new Intent().setAction(Intent.ACTION_VIEW)
							.setData(Uri.parse("https://github.com/ratulhasanrahat/Current-Activity")));
				}
			});
			fancy.show();
		} else if (title.equals("Bug Report")) {
			fancy.setTitle("Bug Report");
			fancy.setMessage(
					"If you found a bug while using this app, please take a screenshot of it if possible. If it's a crash then you can find the crash log in this directory: "
							+ new File(App.getCrashLogDir(), "crash.txt").getAbsolutePath()
							+ "\n\nAfter you get all necessary things related to the bug, open an issue in github repo of this app with your bug report details");
			fancy.setPositiveButton("Create", new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					fancy.dismiss();
					startActivity(new Intent().setAction(Intent.ACTION_VIEW)
							.setData(Uri.parse("https://github.com/ratulhasanrahat/Current-Activity/issues/new")));
				}
			});
			fancy.show();
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
		fancy.setTitle("Battery Optimizations");
		fancy.setMessage(
				"Please remove battery optimization/restriction from this app in order to run in background with full functionality");
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
		fancy.show();

	}
}
