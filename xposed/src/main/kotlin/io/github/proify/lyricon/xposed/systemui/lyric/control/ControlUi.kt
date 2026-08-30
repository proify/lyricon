/*
 * Copyright 2026 Proify, Tomakino
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.github.proify.lyricon.xposed.systemui.lyric.control

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.GradientDrawable
import android.view.MotionEvent
import android.view.View
import io.github.proify.android.extensions.dp
import io.github.proify.lyricon.xposed.BuildConfig

/**
 * 控制窗口的通用 UI 工具：圆角 Drawable、按钮按压反馈、模块资源访问。
 *
 * @author Tomakino
 * @since 2026
 */
internal object ControlUi {

    /** 按压时缩放到的比例。 */
    private const val PRESS_DOWN_SCALE = 0.86f
    private const val PRESS_DOWN_DURATION_MS = 90L
    private const val PRESS_UP_DURATION_MS = 140L

    /** 构造纯色圆角矩形 Drawable，可选描边。 */
    fun roundedDrawable(
        color: Int,
        radius: Float,
        strokeColor: Int? = null
    ): GradientDrawable = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = radius
        setColor(color)
        if (strokeColor != null) {
            setStroke(1.dp, strokeColor)
        }
    }

    /**
     * 轻微按压缩放反馈。不消费触摸事件（返回 false），不影响点击派发。
     */
    fun pressFeedbackListener(): View.OnTouchListener {
        return object : View.OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        v.animate().scaleX(PRESS_DOWN_SCALE).scaleY(PRESS_DOWN_SCALE)
                            .setDuration(PRESS_DOWN_DURATION_MS)
                            .start()
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        v.animate().scaleX(1f).scaleY(1f)
                            .setDuration(PRESS_UP_DURATION_MS)
                            .start()
                    }
                }
                return false
            }
        }
    }

    /**
     * 获取宿主应用（模块 App）的 Resources，用于加载模块自带的矢量图标。
     */
    fun moduleResources(context: Context): Resources {
        return context.createPackageContext(
            BuildConfig.APP_PACKAGE_NAME,
            Context.CONTEXT_INCLUDE_CODE or Context.CONTEXT_IGNORE_SECURITY
        ).resources
    }
}
