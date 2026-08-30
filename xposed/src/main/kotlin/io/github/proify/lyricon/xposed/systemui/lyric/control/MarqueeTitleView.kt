/*
 * Copyright 2026 Proify, Tomakino
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.github.proify.lyricon.xposed.systemui.lyric.control

import android.content.Context
import android.graphics.Canvas
import android.graphics.Typeface
import android.os.SystemClock
import android.text.TextPaint
import android.view.View
import io.github.proify.android.extensions.dp
import io.github.proify.lyricon.xposed.systemui.lyric.control.MarqueeTitleView.Companion.GHOST_SPACING_DP
import io.github.proify.lyricon.xposed.systemui.lyric.control.MarqueeTitleView.Companion.LOOP_DELAY_MS

/**
 * 跑马灯标题视图。
 *
 * 与 [LyricLineView] 一样继承 [View]，自成一套文本绘制与滚动：
 * - 滚动模型参照同仓库 [io.github.proify.lyricon.lyric.view.line.ScrollTextRenderer]：
 *   完全绕过 TextView 的文本布局与系统跑马灯（startMarquee 依赖焦点/选中状态、窗口
 *   焦点回调与重排时会 stop/重开），改由本控件用帧回调自行推进偏移，并在 onDraw 里用
 *   [textPaint] 直接绘制"主副本 + 幽灵副本"，副本间留 [GHOST_SPACING_DP] 空隙，
 *   循环间延迟 [LOOP_DELAY_MS]。行为不依赖焦点/选中/窗口焦点，任何环境下都一致。
 * - 渐隐边复用系统机制（同 [LyricLineView]）：[setHorizontalFadingEdgeEnabled] +
 *   重写 [getLeftFadingEdgeStrength] / [getRightFadingEdgeStrength]，把强度绑定到本控件的
 *   [unitOffset]，由系统按滚动状态绘制左右渐隐。
 *
 * @author Tomakino
 * @since 2026
 */
internal class MarqueeTitleView(context: Context) : View(context) {

    /** 文本画笔（字号/颜色/字体由面板通过 [textSize]/[textColor]/[textTypeface] 驱动）。 */
    val textPaint: TextPaint = TextPaint().apply {
        isAntiAlias = true
    }

    /** 当前文本。 */
    var text: String = ""
        set(value) {
            if (field == value) return
            field = value
            textWidth = textPaint.measureText(value)
            unitOffset = 0f
            resetState()
            start()
            invalidate()
        }

    /** 字号（px）。 */
    var textSize: Float
        get() = textPaint.textSize
        set(value) {
            if (textPaint.textSize == value) return
            textPaint.textSize = value
            textWidth = textPaint.measureText(text)
            invalidate()
        }

    /** 文本颜色。 */
    var textColor: Int
        get() = textPaint.color
        set(value) {
            if (textPaint.color == value) return
            textPaint.color = value
            invalidate()
        }

    /** 字体。 */
    var textTypeface: Typeface?
        get() = textPaint.typeface
        set(value) {
            if (textPaint.typeface == value) return
            textPaint.typeface = value
            textWidth = textPaint.measureText(text)
            invalidate()
        }

    /** 当前文本的单行宽度（px），超过视图宽度时启动滚动。 */
    private var textWidth = 0f

    /** 单次滚动周期内的偏移（px），单位 = textWidth + ghostSpacing。 */
    private var unitOffset = 0f

    /** 帧推进行程是否在跑。 */
    private var isRunning = false

    /** 是否处于循环间/入场延迟。 */
    private var isPendingDelay = false

    private var delayRemainingMs = 0L
    private var frameAt = 0L

    private val ghostSpacing = GHOST_SPACING_DP.dp.toFloat()
    private val scrollSpeedPxPerMs =
        (DEFAULT_SPEED_DP * resources.displayMetrics.density) / 1000f

    init {
        // 渐隐边交给系统绘制，强度由 getLeft/RightFadingEdgeStrength 绑定滚动状态
        setFadingEdgeLength(FADING_EDGE_LENGTH_DP.dp)
        isHorizontalFadingEdgeEnabled = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 宽度撑满可用（视口 = 列宽），高度按文本行高
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val textHeight = (textPaint.descent() - textPaint.ascent()).toInt()
        setMeasuredDimension(w, resolveSize(textHeight, heightMeasureSpec))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        start()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // 入场延迟后开始滚动
        postDelayed({ start() }, START_DELAY_MS)
    }

    override fun onDetachedFromWindow() {
        resetState()
        super.onDetachedFromWindow()
    }

    /** 主副本 + 幽灵副本自绘。 */
    override fun onDraw(canvas: Canvas) {
        val vw = width.toFloat()
        if (text.isEmpty() || textWidth <= 0f) return

        val fm = textPaint.fontMetrics
        val baseline = (height - (fm.descent - fm.ascent)) / 2f - fm.ascent
        val unit = textWidth + ghostSpacing
        val offset = -unitOffset
        val right = offset + textWidth

        // 主副本：向左滚动出视口
        if (offset < vw && right > 0) {
            canvas.save()
            canvas.translate(offset, 0f)
            canvas.drawText(text, 0f, baseline, textPaint)
            canvas.restore()
        }

        // 幽灵副本：主副本尾部离开视口后，从右侧以 ghostSpacing 间距进入
        if (textWidth > vw && right < vw) {
            val ghostX = right + ghostSpacing
            if (ghostX < vw) {
                canvas.save()
                canvas.translate(ghostX, 0f)
                canvas.drawText(text, 0f, baseline, textPaint)
                canvas.restore()
            }
        }
    }

    /** 左缘渐隐强度：滚动偏移在 (0, textWidth] 内随偏移渐显。 */
    override fun getLeftFadingEdgeStrength(): Float {
        if (textWidth <= width + 0.5f || horizontalFadingEdgeLength <= 0) return 0f
        val edgeL = horizontalFadingEdgeLength.toFloat()
        val offsetInUnit = unitOffset
        if (offsetInUnit <= 0f) return 0f
        if (offsetInUnit > textWidth) return 0f
        return (offsetInUnit / edgeL).coerceIn(0f, 1f)
    }

    /** 右缘渐隐强度：主文本尾部与幽灵之间跨越视口右缘的空隙处为零，否则常驻。 */
    override fun getRightFadingEdgeStrength(): Float {
        if (textWidth <= width + 0.5f || horizontalFadingEdgeLength <= 0) return 0f
        val viewW = width.toFloat()
        val primaryRightEdge = textWidth - unitOffset
        val ghostLeftEdge = primaryRightEdge + ghostSpacing
        return if (primaryRightEdge < viewW && ghostLeftEdge > viewW) 0f else 1.0f
    }

    /** 文本超宽且已挂载时启动滚动帧循环（已在跑或宽度未知则不重复）。 */
    private fun start() {
        if (!isAttachedToWindow) return
        if (width < 1) return
        if (textWidth <= width + 0.5f) {
            resetState()
            invalidate()
            return
        }
        if (isRunning || isPendingDelay) return
        scheduleDelay(START_DELAY_MS)
        frameAt = SystemClock.uptimeMillis()
        postOnAnimationDelayed(tick, FRAME_DELAY_MS)
        invalidate()
    }

    private fun scheduleDelay(delayMs: Long) {
        if (delayMs <= 0L) {
            isRunning = true
            isPendingDelay = false
            delayRemainingMs = 0L
        } else {
            isRunning = false
            isPendingDelay = true
            delayRemainingMs = delayMs
        }
    }

    private fun resetState() {
        isRunning = false
        isPendingDelay = false
        delayRemainingMs = 0L
    }

    /** 每帧推进滚动偏移；文本不再超宽时退出帧循环（零后台开销）。 */
    private val tick = object : Runnable {
        override fun run() {
            if (!isAttachedToWindow) {
                resetState()
                return
            }
            if (textWidth <= width + 0.5f) {
                resetState()
                unitOffset = 0f
                invalidate()
                return
            }
            val now = SystemClock.uptimeMillis()
            val deltaMs = (now - frameAt).coerceIn(0L, MAX_FRAME_DELTA_MS)
            frameAt = now

            if (isPendingDelay) {
                delayRemainingMs -= deltaMs
                if (delayRemainingMs <= 0) {
                    isPendingDelay = false
                    isRunning = true
                }
            } else if (isRunning) {
                unitOffset += scrollSpeedPxPerMs * deltaMs
                val unit = textWidth + ghostSpacing
                if (unitOffset >= unit) {
                    unitOffset -= unit
                    // 循环间停顿，与系统跑马灯节奏一致
                    scheduleDelay(LOOP_DELAY_MS)
                }
                invalidate()
            }
            postOnAnimationDelayed(this, FRAME_DELAY_MS)
        }
    }

    private companion object {
        /** 滚动速度（dp/s）。 */
        const val DEFAULT_SPEED_DP = 40f

        /** 相邻副本之间的空隙（dp），详见 ScrollTextRenderer.ghostSpacing。 */
        const val GHOST_SPACING_DP = 40

        /** 渐隐边长度（dp）。 */
        const val FADING_EDGE_LENGTH_DP = 14

        /** 入场后延迟开始滚动（ms）。 */
        const val START_DELAY_MS = 400L

        /** 一次循环结束后的停顿（ms）。 */
        const val LOOP_DELAY_MS = 800L

        /** 帧间隔（ms）。 */
        const val FRAME_DELAY_MS = 16L

        /** 单帧最大补偿时间（ms），防止卡顿后瞬间跳变。 */
        const val MAX_FRAME_DELTA_MS = 50L
    }
}
