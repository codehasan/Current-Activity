package io.github.ratul.topactivity.manager

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager

class DragTouchManager(
    private val windowManager: WindowManager,
    private val params: WindowManager.LayoutParams
) :
    View.OnTouchListener {
    private var xInitCord = 0
    private var yInitCord = 0
    private var xInitMargin = 0
    private var yInitMargin = 0

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        val xCord = event.rawX.toInt()
        val yCord = event.rawY.toInt()

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                xInitCord = xCord
                yInitCord = yCord
                xInitMargin = params.x
                yInitMargin = params.y
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                params.x = xInitMargin + (xCord - xInitCord)
                params.y = yInitMargin + (yCord - yInitCord)
                windowManager.updateViewLayout(view, params)
                return true
            }
        }
        return false
    }
}