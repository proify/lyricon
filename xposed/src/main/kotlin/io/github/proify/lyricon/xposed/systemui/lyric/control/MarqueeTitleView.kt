/*
 * Copyright 2026 Proify, Tomakino
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.github.proify.lyricon.xposed.systemui.lyric.control

import android.content.Context
import android.graphics.Canvas
import android.os.SystemClock
import android.widget.TextView

/**
 * 跑马灯标题视图（自驱动滚动，不依赖 TextView 原生跑马灯）。
 *
 * 原生 [TextView] 跑马灯的启动依赖焦点/选中状态（startMarquee 要求 isFocused()
 * 或 isSelected()），并且窗口焦点回调与文本重排（makeNewLayout 每次都先
 * stopMarquee 再 startMarquee、mScroll 清零）随时会让它停下重来。本控件运行在
 * [LyricControlPopup] 的非焦点 PopupWindow 内（isFocusable = false），窗口拿不到
 * 焦点，滚动状态完全不可控，原生跑马灯表现为"不滚动"或"滚动不断被重置"。
 *
 * 因此这里完全关闭系统跑马灯（ellipsize = null，整行文本按完整宽度布局），
 * 由本控件用 Choreographer 帧回调自行驱动滚动，并在 onDraw 中绘制主副本 + 幽灵
 * 副本实现无缝循环：
 * - 不依赖焦点/选中/窗口焦点，任何环境下的行为都一致；
 * - 文本换行时自动重新测量并从头开始；
 * - 静止时不再投递帧回调，零后台开销。
 *
 * @author Tomakino
 * @since 2026
 */
internal class MarqueeTitleView(context: Context) : TextView(context) {

    /** 当前文本的单行宽度（px），超过视图宽度时启动滚动。 */
    private var textWidth = 0f

    /** 当前滚动偏移（px），在 [0, textWidth + 视口宽) 内循环。 */
    private var scrollOffset = 0f

    /** 上一帧时间戳，用于计算帧间隔。 */
    private var frameAt = 0L

    /** 是否处于跑马灯滚动状态。 */
    private var isScrolling = false

    private val marqueeSpeedPx = MARQUEE_DP_PER_SECOND * resources.displayMetrics.density

    init {
        // 关闭系统跑马灯与渐隐边：布局按整行文本宽度构建，滚动完全由本控件控制
        setSingleLine(true)
        ellipsize = null
        // 关键：让 TextView 用 VERY_WIDE 宽度构建布局（一行完整文本，不按视口换行截断），
        // 否则超宽文本会被 StaticLayout 截到第一处换行点，getLineWidth(0) 只有视口宽，
        // 跑马灯将永远判定"文本未超宽"而不滚动
        setHorizontallyScrolling(true)
        isHorizontalFadingEdgeEnabled = false
    }

    override fun onTextChanged(
        text: CharSequence?,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        textWidth = paint.measureText(text?.toString() ?: "")
        scrollOffset = 0f
        ensureScrolling()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // 布局完成后以真实 mLayout 校准文本宽度（布局宽度才是实际绘制宽度）
        textWidth = layout?.getLineWidth(0) ?: textWidth
        ensureScrolling()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // 与系统跑马灯一致：入场后延迟片刻再开始滚动
        postDelayed({ ensureScrolling() }, START_DELAY_MS)
    }

    override fun onDetachedFromWindow() {
        isScrolling = false
        scrollOffset = 0f
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        val viewWidth = innerWidth()
        if (scrollOffset <= 0.01f || textWidth <= viewWidth) {
            super.onDraw(canvas)
            return
        }
        // 主副本：向左滚动出视口
        canvas.save()
        canvas.translate(-scrollOffset, 0f)
        super.onDraw(canvas)
        canvas.restore()
        // 幽灵副本：主副本尾部离开视口后从右侧无缝进入，循环无"跳回起点"的瞬间
        if (scrollOffset > textWidth) {
            canvas.save()
            canvas.translate(textWidth + viewWidth - scrollOffset, 0f)
            super.onDraw(canvas)
            canvas.restore()
        }
    }

    /** 视口内可用宽度（px）。 */
    private fun innerWidth(): Float = (width - compoundPaddingLeft - compoundPaddingRight).toFloat()

    /** 文本超宽且已挂载时启动滚动帧循环（已滚动则不重复）。 */
    private fun ensureScrolling() {
        if (isScrolling || !isAttachedToWindow) return
        if (innerWidth() < 1f || textWidth <= innerWidth() + 0.5f) return
        isScrolling = true
        frameAt = SystemClock.uptimeMillis()
        postOnAnimationDelayed(tick, FRAME_DELAY_MS)
        invalidate()
    }

    /** 每帧推进滚动偏移；文本不再超宽时退出帧循环。 */
    private val tick = object : Runnable {
        override fun run() {
            if (!isAttachedToWindow) {
                isScrolling = false
                return
            }
            val viewWidth = innerWidth()
            if (viewWidth < 1f || textWidth <= viewWidth + 0.5f) {
                isScrolling = false
                scrollOffset = 0f
                invalidate()
                return
            }
            val now = SystemClock.uptimeMillis()
            val dt = (now - frameAt).coerceIn(0L, MAX_FRAME_DELTA_MS) / 1000f
            frameAt = now
            scrollOffset += marqueeSpeedPx * dt
            val cycle = textWidth + viewWidth
            if (scrollOffset >= cycle) scrollOffset -= cycle
            invalidate()
            postOnAnimationDelayed(this, FRAME_DELAY_MS)
        }
    }

    private companion object {
        /** 滚动速度（dp/s），与系统跑马灯 MARQUEE_DP_PER_SECOND 一致。 */
        const val MARQUEE_DP_PER_SECOND = 30f

        /** 面板显示后延迟开始滚动（ms），与系统跑马灯节奏接近。 */
        const val START_DELAY_MS = 500L

        /** 帧间隔（ms）。 */
        const val FRAME_DELAY_MS = 16L

        /** 单帧最大补偿时间（ms），防止卡顿后瞬间跳变。 */
        const val MAX_FRAME_DELTA_MS = 50L
    }
}
