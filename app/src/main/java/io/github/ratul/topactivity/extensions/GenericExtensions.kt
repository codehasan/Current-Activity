package io.github.ratul.topactivity.extensions

import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.github.ratul.topactivity.R

fun TextView.value() = text.toString()

fun FloatingActionButton.setStatus(active: Boolean) {
    setImageResource(if (active) R.drawable.ic_stop else R.drawable.ic_start)
}

fun WindowManager.getScreenSize(): Pair<Int, Int> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val windowMetrics = currentWindowMetrics
        val insets = windowMetrics.windowInsets.getInsets(
            WindowInsets.Type.systemBars() or
                    WindowInsets.Type.displayCutout()
        )

        val usableWidth = windowMetrics.bounds.width() - insets.left - insets.right
        val usableHeight = windowMetrics.bounds.height() - insets.top - insets.bottom

        Pair(usableWidth, usableHeight)
    } else {
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        defaultDisplay.getMetrics(displayMetrics)
        Pair(displayMetrics.widthPixels, displayMetrics.heightPixels)
    }
}