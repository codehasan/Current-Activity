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

import android.content.*;
import android.app.*;
import android.view.*;
import android.widget.*;
import android.graphics.*;
import android.view.View.OnClickListener;
import android.graphics.drawable.*;
import android.content.res.*;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import io.github.ratul.topactivity.R;

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

	public MaterialTextView title;
	public MaterialTextView message;
	public MaterialTextView positiveButton;
	public MaterialTextView negativeButton;
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
		dialog = new MaterialAlertDialogBuilder(new ContextThemeWrapper(thiz, R.style.FancyDialogThemeV1)).create();

		DialogTheme.setupColors(thiz, theme);

		initializeInterface();
	}

	private void initializeInterface() {
		setCancelable(false);
		inflater = LayoutInflater.from(thiz);
		inflate = inflater.inflate(R.layout.abc_alert_dialog, null);
		dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
		dialog.setView(inflate);

		title = inflate.findViewById(R.id.t1);
		message = inflate.findViewById(R.id.t2);
		positiveButton = inflate.findViewById(R.id.b2);
		negativeButton = inflate.findViewById(R.id.b1);
		main = (LinearLayout) inflate.findViewById(R.id.bg);
		middle = (LinearLayout) inflate.findViewById(R.id.middle);

		middle.setVisibility(View.GONE);
		window = dialog.getWindow();
		wlp = window.getAttributes();

		title.setTextColor(DialogTheme.titleColor);
		message.setTextColor(DialogTheme.messageColor);
		positiveButton.setTextColor(DialogTheme.positiveTextColor);
		negativeButton.setTextColor(DialogTheme.negativeTextColor);

		rippleRoundStroke(main, DialogTheme.background, DialogTheme.pressedColor, DialogTheme.round, 0,
				DialogTheme.strokeColor);
		rippleRoundStroke(negativeButton, DialogTheme.negativeButtonColor, 0xFFE0E0E0, DialogTheme.round, 1,
				DialogTheme.strokeColor);
		rippleRoundStroke(positiveButton, DialogTheme.positiveButtonColor, 0x40FFFFFF, DialogTheme.round, 0,
				DialogTheme.strokeColor);
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

	public MaterialTextView getTitle() {
		return title;
	}

	public MaterialTextView getMessage() {
		return message;
	}

	public MaterialTextView getPositiveButton() {
		return positiveButton;
	}

	public MaterialTextView getNegativeButton() {
		return negativeButton;
	}

	public void showAnimation(boolean bool) {
		if (bool)
			wlp.windowAnimations = R.style.FancyDialogAnimationV1;
		else
			wlp.windowAnimations = 0;
	}

	public LinearLayout getBaseView() {
		return (LinearLayout) inflate.findViewById(R.id.bg);
	}

	public void setBackgroundColor(int color) {
		rippleRoundStroke(main, color, DialogTheme.pressedColor, DialogTheme.round, 0, DialogTheme.strokeColor);
	}

	public void setTitleColor(int color) {
		title.setTextColor(color);
	}

	public void setMessageColor(int color) {
		message.setTextColor(color);
	}

	public void setPositiveButtonDrawable(GradientDrawable backgroundColor, int textColor, int pressedColor) {
		rippleRoundStroke(positiveButton, backgroundColor, pressedColor);
		positiveButton.setTextColor(textColor);
	}

	public void setNegativeButtonDrawable(GradientDrawable backgroundColor, int textColor, int pressedColor) {
		rippleRoundStroke(negativeButton, backgroundColor, pressedColor);
		negativeButton.setTextColor(textColor);
	}

	public void setPositiveButtonColor(int backgroundColor, int textColor, int pressedColor) {
		rippleRoundStroke(positiveButton, backgroundColor, pressedColor, DialogTheme.round, 0, DialogTheme.strokeColor);
		positiveButton.setTextColor(textColor);
	}

	public void setNegativeButtonColor(int backgroundColor, int textColor, int pressedColor) {
		rippleRoundStroke(negativeButton, backgroundColor, pressedColor, DialogTheme.round, 1, DialogTheme.strokeColor);
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

		if (titleText == null)
			title.setVisibility(View.GONE);
		if (mesaageText == null)
			message.setVisibility(View.GONE);
		if (positiveButtonText == null)
			positiveButton.setVisibility(View.GONE);
		if (negativeButtonText == null)
			negativeButton.setVisibility(View.GONE);
		if (positiveButtonText == null && negativeButtonText == null)
			dialog.setCancelable(true);
		else
			dialog.setCancelable(cancelable);

		window.setAttributes(wlp);
		dialog.show();
	}

	private void rippleRoundStroke(View view, GradientDrawable gg, int pressed) {
		RippleDrawable RE = new RippleDrawable(new ColorStateList(new int[][] { new int[] {} }, new int[] { pressed }),
				gg, null);
		view.setBackground(RE);
	}

	private void rippleRoundStroke(View view, int focus, int pressed, double round, double stroke, int strokeclr) {
		GradientDrawable GG = new GradientDrawable();
		GG.setColor(focus);
		GG.setCornerRadius((float) round);
		GG.setStroke((int) stroke, strokeclr);
		RippleDrawable RE = new RippleDrawable(new ColorStateList(new int[][] { new int[] {} }, new int[] { pressed }),
				GG, null);
		view.setBackground(RE);
	}
}
