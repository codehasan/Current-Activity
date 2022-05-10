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
package io.github.ratul.topactivity.dialog;

import android.widget.TextView;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by Ratul on 09/05/2022.
 */
public class DialogBeautifier {
    private String regular_font = "fonts/google_sans_regular.ttf";
    private String bold_font = "fonts/google_sans_bold.ttf";
    private AlertDialog dialog;
    private Context dialogContext;
    
    public DialogBeautifier(AlertDialog alert) {
        dialog = alert;
        dialogContext = alert.getContext();
    }
    
    public boolean beautifyTitle() {
        try{
            int titleId = dialogContext.getResources().getIdentifier( "alertTitle", "id", "android" );
            TextView title = dialog.findViewById(titleId);
            Typeface typeface = Typeface.createFromAsset(dialogContext.getAssets(), bold_font);
            title.setTypeface(typeface);
            return true;
        } catch(Exception e) {
            return false;
        }
    }
    
    public void beautifyMessage() {
        TextView message = dialog.findViewById(android.R.id.message);
        message.setTypeface(Typeface.createFromAsset(dialogContext.getAssets(), regular_font), 0);
    }
    
    public void beautifyPositiveButton() {
        TextView positiveBtn = dialog.findViewById(android.R.id.button1);
        positiveBtn.setTypeface(Typeface.createFromAsset(dialogContext.getAssets(), regular_font), 1);
        positiveBtn.setAllCaps(false);
    }
    
    public void beautifyNegativeButton() {
        TextView negativeBtn = dialog.findViewById(android.R.id.button2);
        negativeBtn.setTypeface(Typeface.createFromAsset(dialogContext.getAssets(), regular_font), 1);
        negativeBtn.setAllCaps(false);
    }
    
    public void beautifyNeutralButton() {
        TextView neutralBtn = dialog.findViewById(android.R.id.button3);
        neutralBtn.setTypeface(Typeface.createFromAsset(dialogContext.getAssets(), regular_font), 1);
        neutralBtn.setAllCaps(false);
    }
    
    public void beautify() throws Exception {
        beautifyTitle();
        beautifyMessage();
        beautifyPositiveButton();
        beautifyNegativeButton();
        beautifyNeutralButton();
    } 
}
