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
package com.ratul.topactivity.ui;

import android.app.Activity;
import android.os.Bundle;
import com.ratul.topactivity.R;
import android.widget.TextView;
import android.view.MenuItem;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.view.Menu;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.Toast;
import android.text.SpannableString;
import android.app.ActionBar;
import android.text.Spannable;
import com.ratul.topactivity.dialog.DialogTheme;
import com.ratul.topactivity.dialog.FancyDialog;
import com.ratul.topactivity.model.TypefaceSpan;

public class CrashActivity extends Activity {
    public static String EXTRA_CRASH_INFO = "crash";
    private String crashInfo;
    private boolean restart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crash_view);

        SpannableString s = new SpannableString(getString(R.string.app_name));
        s.setSpan(new TypefaceSpan(this, "fonts/google_sans_bold.ttf"), 0, s.length(),
                  Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(s);

        restart = getIntent().getBooleanExtra("Restart", true);
        String mLog = getIntent().getStringExtra(EXTRA_CRASH_INFO);
        crashInfo = mLog;
        TextView crashed = findViewById(R.id.crashed);
        crashed.setText(mLog);
    }

    @Override
    public void onBackPressed() {
        if (!restart) {
            finish();
            return;
        }
        final FancyDialog fancy = new FancyDialog(this, FancyDialog.DARK_THEME);
        fancy.setTitle("Exit");
        fancy.setMessage("App will restart, are you sure to exit");
        fancy.setPositiveButton("Yes", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fancy.dismiss();
                    restart();
                }
            });
        fancy.setNegativeButton("No", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fancy.dismiss();
                }
            });
        fancy.setCancelable(false);
        fancy.show();
    }

    private void restart() {
        PackageManager pm = getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(getPackageName());
        if (intent != null) {
            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
            );
            startActivity(intent);
        }
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.copy: 
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setPrimaryClip(ClipData.newPlainText(getPackageName(), crashInfo));
                Toast.makeText(this, "Copied", 0).show();
                break;
            case android.R.id.redo: 
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        SpannableString s = new SpannableString("Copy Log");
        s.setSpan(new TypefaceSpan(this, "fonts/google_sans_regular.ttf"), 0, s.length(),
                  Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        menu.add(0, android.R.id.copy, 0, s);
        if (restart) {
            s = new SpannableString("Restart App");
            s.setSpan(new TypefaceSpan(this, "fonts/google_sans_regular.ttf"), 0, s.length(),
                      Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            menu.add(1, android.R.id.redo, 1, s);
        }
        return super.onCreateOptionsMenu(menu);
    }

}
