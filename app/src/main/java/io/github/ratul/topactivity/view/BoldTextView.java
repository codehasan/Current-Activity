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
package io.github.ratul.topactivity.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.util.AttributeSet;
import com.google.android.material.textview.MaterialTextView;

public class BoldTextView extends MaterialTextView {
	public void setBoldFont(Context context) {
		Typeface face = Typeface.createFromAsset(context.getAssets(), "fonts/google_sans_bold.ttf");
		super.setTypeface(face);
	}

	public BoldTextView(Context context) {
		super(context);
		setBoldFont(context);
	}

	public BoldTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setBoldFont(context);
	}

	public BoldTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setBoldFont(context);
	}
	
	public BoldTextView(Context context, AttributeSet attrs, int defStyle, int res) {
		super(context, attrs, defStyle, res);
		setBoldFont(context);
	}

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}
}
