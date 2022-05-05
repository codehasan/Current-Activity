package com.willme.topactivity;

import android.support.v4.util.LruCache;
import android.graphics.Typeface;
import android.text.style.MetricAffectingSpan;
import android.content.Context;
import android.text.TextPaint;
import android.graphics.Paint;

public class TypefaceSpan extends MetricAffectingSpan {
    private static LruCache<String, Typeface> sTypefaceCache = new LruCache<String, Typeface>(12);
    private Typeface mTypeface;

    public TypefaceSpan(Context context, String typefaceName) {
        mTypeface = sTypefaceCache.get(typefaceName);
        if (mTypeface == null) {
            mTypeface = Typeface.createFromAsset(context.getApplicationContext()
                                                 .getAssets(), typefaceName);
            sTypefaceCache.put(typefaceName, mTypeface);
        }
    }

    @Override
    public void updateMeasureState(TextPaint p) {
        p.setTypeface(mTypeface);
        // Note: This flag is required for proper typeface rendering
        p.setFlags(p.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        tp.setTypeface(mTypeface);
        // Note: This flag is required for proper typeface rendering
        tp.setFlags(tp.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
    }
}
