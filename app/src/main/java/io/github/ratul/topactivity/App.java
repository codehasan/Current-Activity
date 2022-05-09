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
package io.github.ratul.topactivity;

import android.app.Application;
import io.github.ratul.topactivity.R;
import io.github.ratul.topactivity.model.CrashHandler;
import android.content.Context;
import java.io.File;
import android.app.Activity;
import io.github.ratul.topactivity.ui.MainActivity;
import android.content.Intent;
import io.github.ratul.topactivity.ui.CrashActivity;
import android.widget.Toast;
import android.os.Environment;

public class App extends Application {

	private static App sApp;

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		sApp = this;
		CrashHandler.getInstance(getApp()).init();
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	public static String getCrashLogDir() {
		return getCrashLogFolder().getAbsolutePath();
	}

	public static File getCrashLogFolder() {
		return sApp.getFilesDir();
	}

	public static App getApp() {
		return sApp;
	}

	public static void showToast(String str, int length) {
		Toast.makeText(getApp(), str, length).show();
	}

}
