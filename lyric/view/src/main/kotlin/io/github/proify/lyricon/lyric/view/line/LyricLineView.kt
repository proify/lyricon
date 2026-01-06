/*
 * Copyright 2026 Proify
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.proify.lyricon.lyric.view.line

import android.content.Context
import android.graphics.Canvas
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.Choreographer
import android.view.View
import io.github.proify.lyricon.lyric.model.LyricLine
import io.github.proify.lyricon.lyric.view.LyricLineConfig
import io.github.proify.lyricon.lyric.view.util.dp
import io.github.proify.lyricon.lyric.view.util.sp
import java.lang.ref.WeakReference

@Suppress("unused")
class LyricLineView(context: Context, attrs: AttributeSet? = null) :
    View(context, attrs) {

    companion object {
        private const val TAG = "LyricLineView"
    }

    init {
        isHorizontalFadingEdgeEnabled = true
        setFadingEdgeLength(15.dp)
    }

    internal val textPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG).apply {
        textSize = 24f.sp
    }

    internal var lyricModel: LyricModel = emptyLyricModel()
    internal var scrollXOffset = 0f
    internal var isScrollFinished = false
    internal val marquee = Marquee(WeakReference(this))
    internal var syllable = Syllable(this)
    private val animationDriver = AnimationDriver()

    fun reset() {
        animationDriver.stop()
        marquee.reset()
        syllable.reset()
        scrollXOffset = 0f
        isScrollFinished = false
        lyricModel = emptyLyricModel()
        refreshModelSizes()
        invalidate()
    }

    fun setTextSize(size: Float) {
        textPaint.textSize = size

        syllable.backgroundPaint.textSize = size
        syllable.highlightPaint.textSize = size
        refreshModelSizes()
    }

    fun setStyle(configs: LyricLineConfig) {
        val textConfig = configs.text
        val marqueeConfig = configs.marquee
        val syllableConfig = configs.syllable

        textPaint.run {
            textSize = textConfig.textSize
            typeface = textConfig.typeface
            color = textConfig.textColor
        }

        syllable.backgroundPaint.run {
            textSize = textConfig.textSize
            typeface = textConfig.typeface
            color = syllableConfig.backgroundColor
        }
        syllable.highlightPaint.run {
            textSize = textConfig.textSize
            typeface = textConfig.typeface
            color = syllableConfig.highlightColor
        }

        marquee.run {
            ghostSpacing = marqueeConfig.ghostSpacing
            scrollSpeed = marqueeConfig.scrollSpeed
            initialDelayMs = marqueeConfig.initialDelay
            loopDelayMs = marqueeConfig.loopDelay
            repeatCount = marqueeConfig.repeatCount
            stopAtEnd = marqueeConfig.stopAtEnd
        }
        refreshModelSizes()

        invalidate()
    }

    fun isSyllableMode() = !isMarqueeMode()

    fun setPosition(position: Long) {
        if (isSyllableMode()) {
            syllable.updateProgress(position)
            animationDriver.startIfNoRuning() // 确保驱动器运行以推进动画
        }
    }

    fun refreshModelSizes() {
        lyricModel.updateSizes(textPaint)
    }

    // 可调参数：渐隐的“感知范围”占控件宽度的比例（0.0 ~ 1.0）
    private val fadingRangeFraction = 0.18f

    private fun computeFadingRange(): Float {
        val fromWidth = width * fadingRangeFraction
        val maxByLyric = if (lyricWidth > 0f) lyricWidth / 3f else fromWidth
        return fromWidth.coerceAtMost(maxByLyric).coerceAtLeast(1f)
    }

    private fun isFinalLoopAndFinished(): Boolean {
        return marquee.stopAtEnd &&
                marquee.repeatCount > 0 &&
                marquee.currentRepeat >= marquee.repeatCount - 1 &&
                scrollXOffset <= -lyricWidth
    }

    override fun getLeftFadingEdgeStrength(): Float {
        if (!isOverflow()) return 0f
        if (isFinalLoopAndFinished()) return 0f

        // Marquee 模式采用可见文本的左隐量
        if (isMarqueeMode()) {
            val unit = lyricWidth + marquee.ghostSpacing
            if (unit <= 0f) return 0f

            val hiddenLeftPrimary = marquee.getEasedHiddenLeft()
            val primaryRight = -hiddenLeftPrimary + lyricWidth

            val fadeRange = computeFadingRange()

            return if (primaryRight > 0f) {
                // ① primary 仍在视口内 → 按 primary 左隐量渐隐
                (hiddenLeftPrimary / fadeRange).coerceIn(0f, 1f)
            } else {
                // ② primary 完全滚出视口，ghost 出现 → 使用 ghost 的左隐量
                val ghostLeft = primaryRight + marquee.ghostSpacing
                val ghostHiddenLeft = -ghostLeft // ghostLeft < 0 → 被左侧遮挡的像素
                (ghostHiddenLeft / fadeRange).coerceIn(0f, 1f)
            }
        }

        // 非 marquee 常规逻辑
        val hiddenLeft = (-scrollXOffset).coerceAtLeast(0f)
        val fadeRange = computeFadingRange()
        return (hiddenLeft / fadeRange).coerceIn(0f, 1f)
    }

    override fun getRightFadingEdgeStrength(): Float {
        if (!isOverflow()) return 0f
        if (isScrollFinished) return 0f

        if (isMarqueeMode() && isFinalLoopAndFinished()) return 0f

        val unit = lyricWidth + marquee.ghostSpacing
        if (unit <= 0f) return 0f

        val hiddenLeft = marquee.getEasedHiddenLeft().coerceIn(0f, unit)

        val firstRight = -hiddenLeft + lyricWidth
        val ghostLeft = firstRight + marquee.ghostSpacing
        val ghostRight = ghostLeft + lyricWidth

        val rightMost = maxOf(firstRight, ghostRight)
        val hiddenRight = (rightMost - width).coerceAtLeast(0f)

        val fadeRange = computeFadingRange()
        return (hiddenRight / fadeRange).coerceIn(0f, 1f)
    }

    override fun onMeasure(wSpec: Int, hSpec: Int) {
        val w = MeasureSpec.getSize(wSpec)
        val textHeight = (textPaint.descent() - textPaint.ascent()).toInt()
        setMeasuredDimension(w, resolveSize(textHeight, hSpec))
    }

    fun setLyric(line: LyricLine?) {
        reset()
        lyricModel = if (line == null) emptyLyricModel() else line.normalize().createModel()
        Log.d("LyricLineView", "setLyric: $lyricModel")

        refreshModelSizes()
        requestLayout()
        invalidate()
    }

    fun startMarquee() {
        if (isMarqueeMode()) {
            scrollXOffset = 0f
            marquee.start()
            animationDriver.startIfNoRuning()
        }
    }

    fun pauseMarquee() {
        if (isMarqueeMode()) {
            marquee.pause()
        }
    }

    fun isMarqueeMode() = lyricModel.isPlainText()

    fun isOverflow() = lyricWidth > width

    val lyricWidth get() = lyricModel.width

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isMarqueeMode()) {
            marquee.draw(canvas)
        } else {
            syllable.draw(canvas)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        reset()
    }

    /**
     * ------------------------
     *  统一动画驱动器（Choreographer）
     * ------------------------
     */
    internal inner class AnimationDriver : Choreographer.FrameCallback {
        private var running = false
        private var lastFrameNanos = 0L

        fun startIfNoRuning() {
            if (!running) {
                running = true
                lastFrameNanos = System.nanoTime()
                Choreographer.getInstance().postFrameCallback(this)
            }
        }

        fun stop() {
            if (!running) return
            running = false
            Choreographer.getInstance().removeFrameCallback(this)
        }

        override fun doFrame(frameTimeNanos: Long) {
            if (!running) return

            val deltaNanos = if (lastFrameNanos == 0L) 0L else (frameTimeNanos - lastFrameNanos)
            lastFrameNanos = frameTimeNanos

            var needInvalidate: Boolean

            if (isMarqueeMode()) {
                // 驱动 Marquee（被动 step，保留完整行为）
                marquee.step(deltaNanos)
                needInvalidate = true
            } else {
                // 非跑马灯：推进高亮 & 字偏移
                val changed = syllable.updateFrame(frameTimeNanos)
                needInvalidate = changed
            }

            if (needInvalidate) postInvalidateOnAnimation()

            if (running) Choreographer.getInstance().postFrameCallback(this)
        }
    }
}