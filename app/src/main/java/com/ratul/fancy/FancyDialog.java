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
package com.ratul.fancy;

import android.content.*;
import android.app.*;
import android.view.*;
import android.widget.*;
import android.graphics.*;
import android.view.View.OnClickListener;
import android.graphics.drawable.*;
import android.content.res.*;
import com.ratul.topactivity.R;

/**
 * Created by Ratul on 04/05/2022.
 */
public class FancyDialog {
	private Context thiz;
	private AlertDialog dialog;
	private String titleText;
	private String mesaageText;
	private LayoutInflater inflater;
	private View inflate;
    private Window window;
    private WindowManager.LayoutParams wlp;

	private String positiveButtonText;
	private String negativeButtonText;
	private boolean cancelable;
	
	public TextView title;
	public TextView message;
	public TextView positiveButton;
	public TextView negativeButton;
    public LinearLayout main;
    public LinearLayout middle;
    
    public static final int DARK_THEME = 1;
    public static final int LIGHT_THEME = 0;
    public static final int DEVICE_THEME = -1;
    public static final int DRACULA_THEME = 2;
    public static final int SUCCESS_THEME = 3;
    public static final int WARNING_THEME = 4;
    public static final int INFO_THEME = 5;
    public static final int ERROR_THMEE = 6;
    public static final int HOLO_THEME = 7;
	
    public FancyDialog(Context context) {
		this(context, DEVICE_THEME);
	}
	
	public FancyDialog(Context context, int theme) {
		thiz = context;
		dialog = new AlertDialog.Builder(new ContextThemeWrapper(thiz, R.style.FancyDialogThemeV1)).create();
        
        ColorSetup.setupColors(thiz, theme);
        
		initializeInterface();
	}
	
	private void initializeInterface() {
		setCancelable(false);
		inflater = LayoutInflater.from(thiz);
		inflate = inflater.inflate(R.layout.abc_alert_dialog_fancy, null); 
		dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
		dialog.setView(inflate);

		title = (TextView) inflate.findViewById(R.id.t1);
		message = (TextView) inflate.findViewById(R.id.t2);
		positiveButton = (TextView) inflate.findViewById(R.id.b2);
		negativeButton = (TextView) inflate.findViewById(R.id.b1);
        main = (LinearLayout) inflate.findViewById(R.id.bg);
        middle = (LinearLayout) inflate.findViewById(R.id.middle);
        
        middle.setVisibility(View.GONE);
        window = dialog.getWindow();
        wlp = window.getAttributes();
        
        title.setTypeface(Typeface.createFromAsset(thiz.getAssets(),"fonts/google_sans_bold.ttf"), 0);
        message.setTypeface(Typeface.createFromAsset(thiz.getAssets(),"fonts/google_sans_regular.ttf"), 0);
        positiveButton.setTypeface(Typeface.createFromAsset(thiz.getAssets(),"fonts/google_sans_regular.ttf"), 1);
        negativeButton.setTypeface(Typeface.createFromAsset(thiz.getAssets(),"fonts/google_sans_regular.ttf"), 1);
        
        title.setTextColor(ColorSetup.titleColor);
        message.setTextColor(ColorSetup.messageColor);
        positiveButton.setTextColor(ColorSetup.positiveTextColor);
        negativeButton.setTextColor(ColorSetup.negativeTextColor);
        
        rippleRoundStroke(main, ColorSetup.background, ColorSetup.pressedColor, ColorSetup.round, 0, ColorSetup.strokeColor);
        rippleRoundStroke(negativeButton, ColorSetup.negativeButtonColor, 0xFFE0E0E0, ColorSetup.round, 1, ColorSetup.strokeColor);
        rippleRoundStroke(positiveButton, ColorSetup.positiveButtonColor, 0x40FFFFFF, ColorSetup.round, 0, ColorSetup.strokeColor);
	}
    
    public void showAsBottomSheet(boolean bool) {  
        if (bool) {
            wlp.windowAnimations = R.style.FancyDialogAnimationV2;
            wlp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        } else {
            wlp.windowAnimations = 0;
            wlp.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;
        }
    }
    
    public void setView(View view) {
        middle.addView(view);
        middle.setVisibility(View.VISIBLE);
    }
    
    public TextView getTitle() {
        return title;
    }

    public TextView getMessage() {
        return message;
    }

    public TextView getPositiveButton() {
        return positiveButton;
    }

    public TextView getNegativeButton() {
        return negativeButton;
    }
	
	public void showAnimation(boolean bool) {
		if(bool)
		    wlp.windowAnimations = R.style.FancyDialogAnimationV1;
		else
			wlp.windowAnimations = 0;
	}
	
	public LinearLayout getBaseView() {
		return (LinearLayout) inflate.findViewById(R.id.bg);
	}
	
	public void setBackgroundColor(int color) {
        rippleRoundStroke(main, color, ColorSetup.pressedColor, ColorSetup.round, 0, ColorSetup.strokeColor);
    }
	
	public void setTitleColor(int color) {
		title.setTextColor(color);
	}
	
	public void setMessageColor(int color) {
		message.setTextColor(color);
	}
	
	public void setPositiveButtonColor(int backgroundColor, int textColor, int pressedColor) {
        rippleRoundStroke(positiveButton, backgroundColor, pressedColor, ColorSetup.round, 0, ColorSetup.strokeColor);
        positiveButton.setTextColor(textColor);
    }

    public void setNegativeButtonColor(int backgroundColor, int textColor, int pressedColor) {
        rippleRoundStroke(negativeButton, backgroundColor, pressedColor, ColorSetup.round, 1, ColorSetup.strokeColor);
        negativeButton.setTextColor(textColor);
	}
	
	public AlertDialog getDialog() {
		return dialog;
	}
	
	public void setTitle(String str) {
		titleText = str;
	}
	
	public void setMessage(String str) {
		mesaageText = str;
	}
	
	public void setPositiveButton(String str, View.OnClickListener onClick) {
		positiveButtonText = str;
		positiveButton.setOnClickListener(onClick);
	}
	
	public void setNegativeButton(String str, View.OnClickListener onClick) {
		negativeButtonText = str;
		negativeButton.setOnClickListener(onClick);
	}
	
	public void setCancelable(boolean bool) {
		cancelable = bool;
	}
	
	public void dismiss() {
		dialog.dismiss();
	}
	
	public void show() {
        title.setText(titleText);
        message.setText(mesaageText);
        positiveButton.setText(positiveButtonText);
		negativeButton.setText(negativeButtonText);
        
		if(titleText == null)
			title.setVisibility(View.GONE);
		if(mesaageText == null)
			message.setVisibility(View.GONE);
		if(positiveButtonText == null)
			positiveButton.setVisibility(View.GONE);
		if(negativeButtonText == null)
			negativeButton.setVisibility(View.GONE);
		if(positiveButtonText == null && negativeButtonText == null)
			dialog.setCancelable(true);
		else
			dialog.setCancelable(cancelable);
		
        window.setAttributes(wlp);
		dialog.show();
	}
	
	private void rippleRoundStroke(View view, int focus, int pressed, double round, double stroke, int strokeclr) {
		GradientDrawable GG = new GradientDrawable();
		GG.setColor(focus);
		GG.setCornerRadius((float)round);
		GG.setStroke((int) stroke, strokeclr);
		RippleDrawable RE = new RippleDrawable(new ColorStateList(new int[][]{new int[]{}}, new int[]{pressed}), GG, null);
		view.setBackground(RE);
	}
}
