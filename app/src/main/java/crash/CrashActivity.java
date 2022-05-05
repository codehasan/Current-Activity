package crash;

import android.app.Activity;
import android.os.Bundle;
import com.willme.topactivity.R;
import android.widget.TextView;
import android.view.MenuItem;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.view.Menu;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.content.Context;
import android.graphics.Typeface;
import com.ratul.fancy.FancyDialog;
import android.view.View;
import android.widget.Toast;
import com.ratul.fancy.ColorSetup;
import android.text.SpannableString;
import android.app.ActionBar;
import com.willme.topactivity.TypefaceSpan;
import android.text.Spannable;

public class CrashActivity extends Activity {
    public static String EXTRA_CRASH_INFO = "crash";
    private String crashInfo;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crash_view);
        ColorSetup.setupColors(this, FancyDialog.DARK_THEME);
        
        SpannableString s = new SpannableString(getString(R.string.app_name));
        s.setSpan(new TypefaceSpan(this, "fonts/google_sans_bold.ttf"), 0, s.length(),
                  Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(s);
        
        String mLog = getIntent().getStringExtra(EXTRA_CRASH_INFO);
        crashInfo = mLog;
        TextView crashed = findViewById(R.id.crashed);
        crashed.setText(mLog);
        crashed.setTextColor(getColor(R.color.colorAccent));
        crashed.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/google_sans_regular.ttf"));
    }
    
    @Override
    public void onBackPressed() {
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
        menu.add(0, android.R.id.copy, 0, "Copy Log");
        menu.add(1, android.R.id.redo, 1, "Restart App");
        return super.onCreateOptionsMenu(menu);
    }
    
}
