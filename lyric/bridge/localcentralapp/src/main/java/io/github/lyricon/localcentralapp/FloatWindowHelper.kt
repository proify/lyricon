/*
 * Copyright 2026 Proify, Tomakino
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.github.lyricon.localcentralapp

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import android.view.WindowManager
import androidx.core.view.setPadding
import kotlin.math.abs

class FloatWindowHelper(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val layoutParams = WindowManager.LayoutParams()

    var isShowing: Boolean = false
        private set

    val floatingView: MyLyricPlayerView = MyLyricPlayerView(context).apply {
        setOnTouchListener(FloatingTouchListener())
        setBackgroundColor(Color.WHITE)
        setPadding(50)

        clipToOutline = true
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View?, outline: android.graphics.Outline?) {
                outline?.setRoundRect(0, 0, view?.width ?: 0, view?.height ?: 0, 20f)
            }
        }

        val font = Typeface.create(Typeface.DEFAULT, 600, false)

        setStyle(
            getStyle().copy(

                secondary = getStyle().secondary.copy(
                    typeface = font
                ),
                primary = getStyle().primary.copy(
                    typeface = font
                )

            )
        )
    }

    init {
        setupLayoutParams()
    }

    private fun setupLayoutParams() {
        layoutParams.apply {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            format = PixelFormat.RGBA_8888
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            gravity = Gravity.TOP or Gravity.START
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            x = 100
            y = 300
        }
    }

    fun toggle() {
        if (isShowing) {
            dismiss()
        } else {
            show()
        }
    }

    fun show() {
        if (isShowing) return

        try {
            windowManager.addView(floatingView, layoutParams)
            isShowing = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun dismiss() {
        if (isShowing) {
            try {
                windowManager.removeView(floatingView)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isShowing = false
            }
        }
    }

    private inner class FloatingTouchListener : View.OnTouchListener {
        private var initialX = 0
        private var initialY = 0
        private var initialTouchX = 0f
        private var initialTouchY = 0f

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    layoutParams.x = initialX + (event.rawX - initialTouchX).toInt()
                    layoutParams.y = initialY + (event.rawY - initialTouchY).toInt()
                    if (isShowing) windowManager.updateViewLayout(floatingView, layoutParams)
                    return true
                }

                MotionEvent.ACTION_UP -> {
                    if (abs(event.rawX - initialTouchX) < 10 && abs(event.rawY - initialTouchY) < 10) {
                        v.performClick()
                    }
                    animateToSide()
                    return true
                }
            }
            return false
        }
    }

    private fun animateToSide() {
        val dm = context.resources.displayMetrics
        val screenWidth = dm.widthPixels
        val viewWidth = floatingView.width
        val targetX =
            if (layoutParams.x + viewWidth / 2 > screenWidth / 2) screenWidth - viewWidth else 0

        ValueAnimator.ofInt(layoutParams.x, targetX).apply {
            duration = 300
            addUpdateListener {
                if (isShowing) {
                    layoutParams.x = it.animatedValue as Int
                    windowManager.updateViewLayout(floatingView, layoutParams)
                }
            }
            start()
        }
    }
}